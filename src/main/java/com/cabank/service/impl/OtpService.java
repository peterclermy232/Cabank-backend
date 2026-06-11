package com.cabank.service.impl;

import com.cabank.entity.Message;
import com.cabank.entity.Otp;
import com.cabank.entity.User;
import com.cabank.exception.BadRequestException;
import com.cabank.exception.ResourceNotFoundException;
import com.cabank.repository.OtpRepository;
import com.cabank.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class OtpService {

    private final OtpRepository otpRepository;
    private final UserRepository userRepository;
    private final MessageService messageService;

    private static final int OTP_EXPIRY_MINUTES = 10;

    /**
     * Generates an OTP for an authenticated action (e.g. paying a bill, making a transfer),
     * delivers it via the in-app Messages system, and returns the code so the
     * frontend can auto-fill it (demo/dev convenience).
     */
    @Transactional
    public String requestTransactionOtp(String email) {
        User user = getUser(email);

        String code = generateOtpCode();

        Otp otp = Otp.builder()
                .userId(user.getId())
                .code(code)
                .expiresAt(LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES))
                .purpose(Otp.OtpPurpose.TRANSACTION)
                .verified(false)
                .used(false)
                .build();

        otpRepository.save(otp);

        messageService.createMessage(
                user,
                "CaBank",
                "Your verification code is " + code
                        + ". It expires in " + OTP_EXPIRY_MINUTES + " minutes. Do not share this code with anyone.",
                "Your CaBank verification code",
                Message.MessageType.OTP
        );

        return code;
    }

    @Transactional
    public void verifyTransactionOtp(String email, String code) {
        User user = getUser(email);

        Otp otp = otpRepository
                .findTopByUserIdAndPurposeAndUsedFalseOrderByCreatedAtDesc(
                        user.getId(), Otp.OtpPurpose.TRANSACTION)
                .orElseThrow(() -> new BadRequestException("No verification code found, please request a new one"));

        if (otp.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Verification code has expired, please request a new one");
        }

        if (!otp.getCode().equals(code)) {
            throw new BadRequestException("Invalid verification code");
        }

        otp.setVerified(true);
        otpRepository.save(otp);
    }

    @Transactional
    public void consumeTransactionOtp(String email) {
        User user = getUser(email);

        otpRepository
                .findTopByUserIdAndPurposeAndUsedFalseOrderByCreatedAtDesc(
                        user.getId(), Otp.OtpPurpose.TRANSACTION)
                .filter(Otp::isVerified)
                .ifPresent(otp -> {
                    otp.setUsed(true);
                    otpRepository.save(otp);
                });
    }

    private String generateOtpCode() {
        return String.format("%05d", (int) (Math.random() * 100000));
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }
}