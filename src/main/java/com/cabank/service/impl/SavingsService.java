package com.cabank.service.impl;

import com.cabank.dto.request.SavingsRequest;
import com.cabank.dto.response.SavingsResponse;
import com.cabank.entity.Message;
import com.cabank.entity.Savings;
import com.cabank.entity.User;
import com.cabank.exception.ResourceNotFoundException;
import com.cabank.repository.SavingsRepository;
import com.cabank.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SavingsService {

    private final SavingsRepository savingsRepository;
    private final UserRepository userRepository;
    private final MessageService messageService;
    private final OtpService otpService;

    @Transactional
    public SavingsResponse createSavings(String email, SavingsRequest req) {
        User user = getUser(email);

        // Verify OTP before creating the savings plan
        otpService.verifyTransactionOtp(email, req.getOtpCode());

        Savings savings = Savings.builder()
                .accountNumber(req.getAccountNumber())
                .amount(req.getAmount())
                .fromDate(req.getFromDate())
                .toDate(req.getToDate())
                .period(req.getPeriod())
                .interestRate(req.getInterestRate())
                .user(user)
                .build();

        Savings saved = savingsRepository.save(savings);

        // Prevent OTP reuse for another transaction
        otpService.consumeTransactionOtp(email);

        messageService.createMessage(
                user,
                "CaBank",
                "Your savings plan of " + saved.getAmount() + " for " + saved.getPeriod()
                        + " at " + saved.getInterestRate() + "% interest has been created.",
                "New savings plan created",
                Message.MessageType.NOTIFICATION
        );

        return toResponse(saved);
    }

    public List<SavingsResponse> getSavings(String email) {
        User user = getUser(email);
        return savingsRepository.findByUserId(user.getId())
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    private SavingsResponse toResponse(Savings s) {
        return SavingsResponse.builder()
                .id(s.getId())
                .accountNumber(s.getAccountNumber())
                .amount(s.getAmount())
                .fromDate(s.getFromDate())
                .toDate(s.getToDate())
                .period(s.getPeriod())
                .interestRate(s.getInterestRate())
                .status(s.getStatus().name())
                .build();
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }
}