package com.cabank.service.impl;

import com.cabank.dto.response.AccountResponse;
import com.cabank.entity.Account;
import com.cabank.entity.Message;
import com.cabank.entity.User;
import com.cabank.exception.ResourceNotFoundException;
import com.cabank.repository.AccountRepository;
import com.cabank.repository.UserRepository;
import com.cabank.dto.request.CreateAccountRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final MessageService messageService;

    public List<AccountResponse> getAccounts(String email) {
        User user = getUser(email);
        return accountRepository.findByUserId(user.getId()).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public AccountResponse getAccount(String accountId, String email) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Account", "id", accountId));
        return toResponse(account);
    }

    public AccountResponse createAccount(
            String email,
            CreateAccountRequest req
    ) {
        User user = getUser(email);

        Account account = Account.builder()
                .user(user)
                .accountNumber(generateAccountNumber())
                .balance(
                        req.getInitialDeposit() != null
                                ? req.getInitialDeposit()
                                : BigDecimal.ZERO
                )
                .branch(req.getBranch())
                .type(req.getType())
                .active(true)
                .build();

        Account saved = accountRepository.save(account);

        messageService.createMessage(
                user,
                "CaBank",
                "Your new " + saved.getType().name() + " account " + saved.getAccountNumber()
                        + " at " + saved.getBranch() + " branch has been opened successfully.",
                "New account opened",
                Message.MessageType.NOTIFICATION
        );

        return toResponse(saved);
    }

    private AccountResponse toResponse(Account a) {
        return AccountResponse.builder()
                .id(a.getId())
                .accountNumber(a.getAccountNumber())
                .balance(a.getBalance())
                .branch(a.getBranch())
                .type(a.getType().name())
                .active(a.isActive())
                .build();
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User", "email", email));
    }

    private String generateAccountNumber() {
        return "ACC" + System.currentTimeMillis();
    }
}