package com.cabank.service.impl;

import com.cabank.dto.request.TransferRequest;
import com.cabank.dto.response.TransferResponse;
import com.cabank.entity.Message;
import com.cabank.entity.Transfer;
import com.cabank.entity.User;
import com.cabank.exception.ResourceNotFoundException;
import com.cabank.repository.TransferRepository;
import com.cabank.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransferService {

    private final TransferRepository transferRepository;
    private final UserRepository userRepository;
    private final MessageService messageService;
    // Fix #6: inject OtpService so transfers are OTP-verified
    private final OtpService otpService;

    @Transactional
    public TransferResponse createTransfer(String email, TransferRequest req) {
        User user = getUser(email);

        // Fix #6: verify OTP before processing transfer
        otpService.verifyTransactionOtp(email, req.getOtpCode());

        Transfer transfer = Transfer.builder()
                .amount(req.getAmount())
                .fromCardLast4(req.getFromCardLast4())
                .toAccountNumber(req.getToAccountNumber())
                .beneficiaryName(req.getBeneficiaryName())
                .note(req.getNote())
                .status(Transfer.TransferStatus.COMPLETED)
                .user(user)
                .build();

        Transfer saved = transferRepository.save(transfer);

        // Consume OTP so it cannot be reused
        otpService.consumeTransactionOtp(email);

        messageService.createMessage(
                user,
                "CaBank",
                "You sent " + saved.getAmount() + " to " + saved.getBeneficiaryName()
                        + " (" + saved.getToAccountNumber() + "). Status: " + saved.getStatus().name() + ".",
                "Transfer of " + saved.getAmount() + " sent",
                Message.MessageType.ALERT
        );

        return toResponse(saved);
    }

    public List<TransferResponse> getTransfers(String email) {
        User user = getUser(email);
        return transferRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    private TransferResponse toResponse(Transfer t) {
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
                .build();
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }
}