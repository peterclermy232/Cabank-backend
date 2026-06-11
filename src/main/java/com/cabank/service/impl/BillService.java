package com.cabank.service.impl;

import com.cabank.dto.request.BillPaymentRequest;
import com.cabank.dto.response.BillPaymentResponse;
import com.cabank.entity.BillPayment;
import com.cabank.entity.Message;
import com.cabank.entity.User;
import com.cabank.exception.ResourceNotFoundException;
import com.cabank.repository.BillPaymentRepository;
import com.cabank.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BillService {

    private final BillPaymentRepository billPaymentRepository;
    private final UserRepository userRepository;
    private final MessageService messageService;
    private final OtpService otpService;

    @Transactional
    public BillPaymentResponse payBill(String email, BillPaymentRequest req) {
        User user = getUser(email);

        // Verify OTP before processing payment
        otpService.verifyTransactionOtp(email, req.getOtpCode());

        BillPayment bill = BillPayment.builder()
                .billType(req.getBillType())
                .billCode(req.getBillCode())
                .customerName(req.getCustomerName())
                .customerAddress(req.getCustomerAddress())
                .amount(req.getAmount())
                .status(BillPayment.BillStatus.PAID)
                .user(user)
                .build();

        BillPayment saved = billPaymentRepository.save(bill);

        // Prevent OTP reuse for another transaction
        otpService.consumeTransactionOtp(email);

        messageService.createMessage(
                user,
                "CaBank",
                "Your " + saved.getBillType() + " bill payment of " + saved.getAmount()
                        + " for " + saved.getCustomerName() + " was successful.",
                saved.getBillType() + " bill paid",
                Message.MessageType.NOTIFICATION
        );

        return toResponse(saved);
    }

    public List<BillPaymentResponse> getBillHistory(String email) {
        User user = getUser(email);
        return billPaymentRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    private BillPaymentResponse toResponse(BillPayment b) {
        return BillPaymentResponse.builder()
                .id(b.getId())
                .billType(b.getBillType())
                .billCode(b.getBillCode())
                .customerName(b.getCustomerName())
                .amount(b.getAmount())
                .tax(b.getTax())
                .status(b.getStatus().name())
                .createdAt(b.getCreatedAt())
                .build();
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }
}