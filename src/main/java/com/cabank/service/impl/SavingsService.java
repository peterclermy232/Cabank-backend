package com.cabank.service.impl;

import com.cabank.dto.request.SavingsRequest;
import com.cabank.dto.response.SavingsResponse;
import com.cabank.entity.Account;
import com.cabank.entity.Message;
import com.cabank.entity.Savings;
import com.cabank.entity.Transaction;
import com.cabank.entity.User;
import com.cabank.exception.BadRequestException;
import com.cabank.exception.ResourceNotFoundException;
import com.cabank.repository.AccountRepository;
import com.cabank.repository.SavingsRepository;
import com.cabank.repository.TransactionRepository;
import com.cabank.repository.UserRepository;
import com.cabank.websocket.WebSocketEventPublisher;
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
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final MessageService messageService;
    private final OtpService otpService;
    private final WebSocketEventPublisher eventPublisher;

    @Transactional
    public SavingsResponse createSavings(String email, SavingsRequest req) {
        User user = getUser(email);

        otpService.verifyTransactionOtp(email, req.getOtpCode());

        Account account = accountRepository.findByAccountNumber(req.getAccountNumber())
                .orElseThrow(() -> new ResourceNotFoundException("Account", "accountNumber", req.getAccountNumber()));

        if (!account.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("This account does not belong to the current user");
        }

        if (account.getBalance().compareTo(req.getAmount()) < 0) {
            throw new BadRequestException(
                "Insufficient balance. Required: " + req.getAmount() +
                ", Available: " + account.getBalance());
        }

        // Lock money: deduct from account balance
        account.setBalance(account.getBalance().subtract(req.getAmount()));
        accountRepository.save(account);

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

        otpService.consumeTransactionOtp(email);

        // Record transaction for the balance deduction
        Transaction tx = transactionRepository.save(Transaction.builder()
                .title("Savings plan — " + saved.getPeriod())
                .category("savings")
                .amount(saved.getAmount())
                .type(Transaction.TransactionType.DEBIT)
                .status(Transaction.TransactionStatus.COMPLETED)
                .emoji("🏦")
                .description("Locked for " + saved.getPeriod() + " at " + saved.getInterestRate() + "% p.a.")
                .user(user)
                .fromAccount(account)
                .build());

        String notificationText = "Your savings plan of " + saved.getAmount() + " for " + saved.getPeriod()
                + " at " + saved.getInterestRate() + "% interest has been created. "
                + "Your new account balance is " + account.getBalance() + ".";

        messageService.createMessage(user, "CaBank", notificationText,
                "New savings plan created", Message.MessageType.NOTIFICATION);

        // Push real-time balance update and new transaction to connected client
        eventPublisher.accountBalanceUpdated(email, account.getId(), account.getBalance());
        eventPublisher.newTransaction(email, buildTxPayload(tx));
        eventPublisher.newMessage(email, "New savings plan created", notificationText, "NOTIFICATION");

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

    private java.util.Map<String, Object> buildTxPayload(Transaction tx) {
        java.util.Map<String, Object> map = new java.util.HashMap<>();
        map.put("id", tx.getId());
        map.put("title", tx.getTitle());
        map.put("amount", tx.getAmount());
        map.put("type", tx.getType().name());
        map.put("category", tx.getCategory());
        map.put("emoji", tx.getEmoji());
        map.put("status", tx.getStatus().name());
        return map;
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }
}
