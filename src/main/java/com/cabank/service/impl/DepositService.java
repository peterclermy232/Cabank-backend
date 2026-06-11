package com.cabank.service.impl;

import com.cabank.dto.request.DepositRequest;
import com.cabank.dto.response.DepositResponse;
import com.cabank.entity.Account;
import com.cabank.entity.Message;
import com.cabank.entity.Transaction;
import com.cabank.entity.User;
import com.cabank.exception.ResourceNotFoundException;
import com.cabank.repository.AccountRepository;
import com.cabank.repository.TransactionRepository;
import com.cabank.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class DepositService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final MessageService messageService;
    private final OtpService otpService;

    @Transactional
    public DepositResponse deposit(String email, DepositRequest req) {
        User user = getUser(email);

        // Verify OTP
        otpService.verifyTransactionOtp(email, req.getOtpCode());

        // Find and update account balance
        Account account = accountRepository
                .findByAccountNumber(req.getAccountNumber())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Account", "accountNumber", req.getAccountNumber()));

        account.setBalance(account.getBalance().add(req.getAmount()));
        accountRepository.save(account);

        // Consume OTP so it can't be reused
        otpService.consumeTransactionOtp(email);

        // Record as a Transaction so it appears in history
        Transaction tx = Transaction.builder()
                .title("Deposit")
                .category("Deposit")
                .amount(req.getAmount())
                .type(Transaction.TransactionType.CREDIT)
                .status(Transaction.TransactionStatus.COMPLETED)
                .emoji("💰")
                .description(req.getNote() != null ? req.getNote() : "Account deposit")
                .user(user)
                .build();

        Transaction saved = transactionRepository.save(tx);

        // Notification message
        messageService.createMessage(
                user,
                "CaBank",
                "Your deposit of " + req.getAmount() + " to account "
                        + req.getAccountNumber() + " was successful. "
                        + "New balance: " + account.getBalance() + ".",
                "Deposit of " + req.getAmount() + " received",
                Message.MessageType.NOTIFICATION
        );

        return DepositResponse.builder()
                .id(saved.getId())
                .accountNumber(req.getAccountNumber())
                .amount(req.getAmount())
                .newBalance(account.getBalance())
                .note(req.getNote())
                .status("COMPLETED")
                .createdAt(saved.getCreatedAt())
                .build();
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }
}