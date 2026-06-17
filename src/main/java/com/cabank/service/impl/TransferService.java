package com.cabank.service.impl;

import com.cabank.dto.request.TransferRequest;
import com.cabank.dto.response.TransferResponse;
import com.cabank.entity.Account;
import com.cabank.entity.Card;
import com.cabank.entity.Message;
import com.cabank.entity.Transaction;
import com.cabank.entity.Transfer;
import com.cabank.entity.User;
import com.cabank.exception.BadRequestException;
import com.cabank.exception.ResourceNotFoundException;
import com.cabank.repository.AccountRepository;
import com.cabank.repository.CardRepository;
import com.cabank.repository.TransactionRepository;
import com.cabank.repository.TransferRepository;
import com.cabank.repository.UserRepository;
import com.cabank.websocket.WebSocketEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransferService {

    private final TransferRepository transferRepository;
    private final UserRepository userRepository;
    private final CardRepository cardRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final MessageService messageService;
    private final OtpService otpService;
    private final WebSocketEventPublisher eventPublisher;

    @Transactional
    public TransferResponse createTransfer(String email, TransferRequest req) {
        User sender = getUser(email);

        // 1. Verify OTP before processing
        otpService.verifyTransactionOtp(email, req.getOtpCode());

        // 2. Find the sender's card by last4
        List<Card> senderCards = cardRepository.findByUserIdAndActiveTrue(sender.getId());
        Card senderCard = senderCards.stream()
                .filter(c -> c.getLast4().equals(req.getFromCardLast4()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Card", "last4", req.getFromCardLast4()));

        BigDecimal fee = new BigDecimal("10.00");
        BigDecimal totalDebit = req.getAmount().add(fee);

        // 3. Check sender has enough balance
        if (senderCard.getBalance().compareTo(totalDebit) < 0) {
            throw new BadRequestException(
                    "Insufficient balance. Required: " + totalDebit +
                            ", Available: " + senderCard.getBalance());
        }

        // 4. Deduct from sender's card (amount + fee)
        senderCard.setBalance(senderCard.getBalance().subtract(totalDebit));
        cardRepository.save(senderCard);

        // 5. Credit the recipient's account (if it exists in this system)
        accountRepository.findByAccountNumber(req.getToAccountNumber())
                .ifPresent(recipientAccount -> {
                    recipientAccount.setBalance(
                            recipientAccount.getBalance().add(req.getAmount()));
                    accountRepository.save(recipientAccount);

                    // Notify the recipient
                    messageService.createMessage(
                            recipientAccount.getUser(),
                            "CaBank",
                            "You received " + req.getAmount() + " from " +
                                    sender.getName() + ". Your new account balance is " +
                                    recipientAccount.getBalance() + ".",
                            "Incoming transfer of " + req.getAmount(),
                            Message.MessageType.NOTIFICATION
                    );

                    // Record a CREDIT transaction for the recipient
                    transactionRepository.save(Transaction.builder()
                            .title("Transfer from " + sender.getName())
                            .category("transfer")
                            .amount(req.getAmount())
                            .type(Transaction.TransactionType.CREDIT)
                            .status(Transaction.TransactionStatus.COMPLETED)
                            .emoji("💸")
                            .description("Received from " + sender.getName())
                            .user(recipientAccount.getUser())
                            .toAccount(recipientAccount)
                            .build());
                });

        // 6. Save the Transfer record
        Transfer transfer = Transfer.builder()
                .amount(req.getAmount())
                .fromCardLast4(req.getFromCardLast4())
                .toAccountNumber(req.getToAccountNumber())
                .beneficiaryName(req.getBeneficiaryName())
                .note(req.getNote())
                .fee(fee)
                .status(Transfer.TransferStatus.COMPLETED)
                .user(sender)
                .build();

        Transfer saved = transferRepository.save(transfer);

        // 7. Consume OTP so it cannot be reused
        otpService.consumeTransactionOtp(email);

        // 8. Record a DEBIT transaction for the sender
        transactionRepository.save(Transaction.builder()
                .title("Transfer to " + req.getBeneficiaryName())
                .category("transfer")
                .amount(totalDebit)
                .type(Transaction.TransactionType.DEBIT)
                .status(Transaction.TransactionStatus.COMPLETED)
                .emoji("💸")
                .description(req.getNote() != null ? req.getNote() : "Transfer")
                .user(sender)
                .build());

        // 9. Notify the sender
        String notificationText = "You sent " + saved.getAmount() + " to " + saved.getBeneficiaryName() +
                " (" + saved.getToAccountNumber() + "). Fee: " + fee +
                ". New card balance: " + senderCard.getBalance() + ".";
        messageService.createMessage(sender, "CaBank", notificationText,
                "Transfer of " + saved.getAmount() + " sent", Message.MessageType.ALERT);

        // 10. Push real-time balance + transaction updates to the sender
        eventPublisher.cardBalanceUpdated(email, senderCard.getId(), senderCard.getLast4(), senderCard.getBalance());
        eventPublisher.newMessage(email, "Transfer of " + saved.getAmount() + " sent", notificationText, "ALERT");

        return toResponse(saved, senderCard.getBalance());
    }

    public List<TransferResponse> getTransfers(String email) {
        User user = getUser(email);
        return transferRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream().map(t -> toResponse(t, null)).collect(Collectors.toList());
    }

    private TransferResponse toResponse(Transfer t, BigDecimal newCardBalance) {
        return TransferResponse.builder()
                .id(t.getId())
                .amount(t.getAmount())
                .fromCardLast4(t.getFromCardLast4())
                .toAccountNumber(t.getToAccountNumber())
                .beneficiaryName(t.getBeneficiaryName())
                .note(t.getNote())
                .fee(t.getFee())
                .status(t.getStatus().name())
                .createdAt(t.getCreatedAt())
                .newCardBalance(newCardBalance)
                .build();
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }
}