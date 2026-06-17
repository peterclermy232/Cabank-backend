package com.cabank.service.impl;

import com.cabank.entity.Message;
import com.cabank.entity.Otp;
import com.cabank.entity.User;
import com.cabank.exception.BadRequestException;
import com.cabank.exception.ResourceNotFoundException;
import com.cabank.repository.OtpRepository;
import com.cabank.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class OtpService {

    private final OtpRepository otpRepository;
    private final UserRepository userRepository;
    private final MessageService messageService;
    private final JavaMailSender mailSender;

    @Value("${app.mail.enabled:false}")
    private boolean mailEnabled;

    @Value("${app.mail.from:noreply@cabank.app}")
    private String mailFrom;

    private static final int OTP_EXPIRY_MINUTES = 10;
    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * Generates a secure OTP, persists it, delivers it via email (if configured)
     * AND via the in-app Messages feed (so users always see it in the app).
     * Returns the code only for logging — never expose it in API responses.
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

        // Deliver via email if configured
        if (mailEnabled) {
            sendOtpEmail(email, user.getName(), code);
        }

        // Always deliver via in-app message (so the OTP shows in Messages screen)
        messageService.createMessage(
                user,
                "CaBank",
                "Your verification code is " + code
                        + ". It expires in " + OTP_EXPIRY_MINUTES + " minutes. "
                        + "Do not share this code with anyone.",
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
                .orElseThrow(() -> new BadRequestException(
                        "No verification code found, please request a new one"));

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

    private void sendOtpEmail(String toEmail, String name, String code) {
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(mailFrom);
            msg.setTo(toEmail);
            msg.setSubject("CaBank — Your verification code");
            msg.setText(
                "Hi " + name + ",\n\n" +
                "Your CaBank verification code is:\n\n" +
                "  " + code + "\n\n" +
                "This code expires in " + OTP_EXPIRY_MINUTES + " minutes.\n" +
                "Do not share this code with anyone — CaBank will never ask for it.\n\n" +
                "If you did not request this, please contact support immediately.\n\n" +
                "— The CaBank Team"
            );
            mailSender.send(msg);
        } catch (Exception e) {
            log.error("Failed to send OTP email to {}: {}", toEmail, e.getMessage());
            // Don't rethrow — in-app message is the fallback delivery channel
        }
    }

    /** Cryptographically secure 6-digit OTP */
    private String generateOtpCode() {
        return String.format("%06d", RANDOM.nextInt(1_000_000));
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }
}
