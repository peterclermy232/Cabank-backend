package com.cabank.service.impl;

import com.cabank.dto.request.WithdrawRequest;
import com.cabank.dto.response.WithdrawResponse;
import com.cabank.entity.Card;
import com.cabank.entity.Message;
import com.cabank.entity.User;
import com.cabank.entity.Withdrawal;
import com.cabank.exception.BadRequestException;
import com.cabank.exception.ResourceNotFoundException;
import com.cabank.repository.CardRepository;
import com.cabank.repository.UserRepository;
import com.cabank.repository.WithdrawalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WithdrawalService {

    private final WithdrawalRepository withdrawalRepository;
    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final MessageService messageService;
    private final OtpService otpService;

    @Transactional
    public WithdrawResponse withdraw(String email, WithdrawRequest req) {
        User user = getUser(email);

        // Verify OTP before touching balance
        otpService.verifyTransactionOtp(email, req.getOtpCode());

        Card card = cardRepository.findById(req.getCardId())
                .orElseThrow(() -> new ResourceNotFoundException("Card", "id", req.getCardId()));

        if (!card.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("This card does not belong to the current user");
        }

        BigDecimal amount = req.getAmount();
        if (card.getBalance().compareTo(amount) < 0) {
            throw new BadRequestException("Withdrawal amount exceeds available balance");
        }

        // Deduct balance
        card.setBalance(card.getBalance().subtract(amount));
        cardRepository.save(card);

        Withdrawal withdrawal = Withdrawal.builder()
                .user(user)
                .cardLast4(card.getLast4())
                .amount(amount)
                .phone(req.getPhone())
                .status(Withdrawal.WithdrawalStatus.COMPLETED)
                .build();

        Withdrawal saved = withdrawalRepository.save(withdrawal);

        // Consume OTP so it cannot be reused
        otpService.consumeTransactionOtp(email);

        messageService.createMessage(
                user,
                "CaBank",
                "You withdrew " + saved.getAmount() + " to " + saved.getPhone()
                        + " from card ending in " + saved.getCardLast4() + ". Status: " + saved.getStatus().name() + ".",
                "Withdrawal of " + saved.getAmount(),
                Message.MessageType.ALERT
        );

        return toResponse(saved, card.getBalance());
    }

    public List<WithdrawResponse> getWithdrawals(String email) {
        User user = getUser(email);
        return withdrawalRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream().map(w -> toResponse(w, null)).collect(Collectors.toList());
    }

    private WithdrawResponse toResponse(Withdrawal w, BigDecimal newBalance) {
        return WithdrawResponse.builder()
                .id(w.getId())
                .amount(w.getAmount())
                .cardLast4(w.getCardLast4())
                .phone(w.getPhone())
                .status(w.getStatus().name())
                .newBalance(newBalance)
                .createdAt(w.getCreatedAt())
                .build();
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }
}