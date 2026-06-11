package com.cabank.service.impl;

import com.cabank.dto.request.TopUpRequest;
import com.cabank.dto.response.TopUpResponse;
import com.cabank.entity.*;
import com.cabank.exception.ResourceNotFoundException;
import com.cabank.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TopUpService {

    private final CardRepository cardRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final MessageService messageService;
    private final OtpService otpService;

    @Transactional
    public TopUpResponse topUp(String email, TopUpRequest req) {
        User user = getUser(email);

        // Verify OTP
        otpService.verifyTransactionOtp(email, req.getOtpCode());

        // Find card and verify it belongs to the user
        Card card = cardRepository.findById(req.getCardId())
                .orElseThrow(() -> new ResourceNotFoundException("Card", "id", req.getCardId()));

        if (!card.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Card does not belong to this user");
        }

        // Update card balance
        card.setBalance(card.getBalance().add(req.getAmount()));
        cardRepository.save(card);

        // Consume OTP
        otpService.consumeTransactionOtp(email);

        // Record transaction
        Transaction tx = Transaction.builder()
                .title("Card Top-Up")
                .category("Top-Up")
                .amount(req.getAmount())
                .type(Transaction.TransactionType.CREDIT)
                .status(Transaction.TransactionStatus.COMPLETED)
                .emoji("💳")
                .description(req.getNote() != null ? req.getNote() : "Card top-up")
                .user(user)
                .build();

        Transaction saved = transactionRepository.save(tx);

        // Notification
        messageService.createMessage(
                user,
                "CaBank",
                "Your card ending in " + card.getLast4() + " has been topped up with "
                        + req.getAmount() + ". New balance: " + card.getBalance() + ".",
                "Card top-up of " + req.getAmount() + " successful",
                Message.MessageType.NOTIFICATION
        );

        return TopUpResponse.builder()
                .id(saved.getId())
                .cardId(card.getId())
                .amount(req.getAmount())
                .newBalance(card.getBalance())
                .status("COMPLETED")
                .createdAt(saved.getCreatedAt())
                .build();
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }
}