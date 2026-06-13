package com.cabank.service.impl;

import com.cabank.dto.request.BillPaymentRequest;
import com.cabank.dto.response.BillPaymentResponse;
import com.cabank.entity.BillPayment;
import com.cabank.entity.Card;
import com.cabank.entity.Message;
import com.cabank.entity.Transaction;
import com.cabank.entity.User;
import com.cabank.exception.BadRequestException;
import com.cabank.exception.ResourceNotFoundException;
import com.cabank.repository.BillPaymentRepository;
import com.cabank.repository.CardRepository;
import com.cabank.repository.TransactionRepository;
import com.cabank.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BillService {

    private final BillPaymentRepository billPaymentRepository;
    private final CardRepository cardRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final MessageService messageService;
    private final OtpService otpService;

    // Tax rate: 10% of the bill amount
    private static final BigDecimal TAX_RATE = new BigDecimal("0.10");

    @Transactional
    public BillPaymentResponse payBill(String email, BillPaymentRequest req) {
        User user = getUser(email);

        // 1. Verify OTP first
        otpService.verifyTransactionOtp(email, req.getOtpCode());

        // 2. Find the card and verify ownership
        Card card = cardRepository.findById(req.getCardId())
                .orElseThrow(() -> new ResourceNotFoundException("Card", "id", req.getCardId()));

        if (!card.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("This card does not belong to you");
        }

        // 3. Calculate tax and total
        BigDecimal tax = req.getAmount().multiply(TAX_RATE).setScale(2, java.math.RoundingMode.HALF_UP);
        BigDecimal total = req.getAmount().add(tax);

        // 4. Check card has enough balance
        if (card.getBalance().compareTo(total) < 0) {
            throw new BadRequestException(
                    "Insufficient card balance. Required: $" + total +
                            ", Available: $" + card.getBalance()
            );
        }

        // 5. Deduct total (amount + tax) from card
        card.setBalance(card.getBalance().subtract(total));
        cardRepository.save(card);

        // 6. Save the bill payment record
        BillPayment bill = BillPayment.builder()
                .billType(req.getBillType())
                .billCode(req.getBillCode())
                .customerName(req.getCustomerName())
                .customerAddress(req.getCustomerAddress())
                .amount(req.getAmount())
                .tax(tax)
                .status(BillPayment.BillStatus.PAID)
                .user(user)
                .build();

        BillPayment saved = billPaymentRepository.save(bill);

        // 7. Consume OTP so it can't be reused
        otpService.consumeTransactionOtp(email);

        // 8. Record transaction in history
        transactionRepository.save(Transaction.builder()
                .title(formatBillTitle(req.getBillType()) + " Bill")
                .category("bill")
                .amount(total)
                .type(Transaction.TransactionType.DEBIT)
                .status(Transaction.TransactionStatus.COMPLETED)
                .emoji(getBillEmoji(req.getBillType()))
                .description("Bill code: " + req.getBillCode() +
                        " | Customer: " + req.getCustomerName())
                .user(user)
                .build());

        // 9. Send notification
        messageService.createMessage(
                user,
                "CaBank",
                "Your " + formatBillTitle(saved.getBillType()) + " bill of $" + saved.getAmount() +
                        " (+ $" + tax + " tax) for " + saved.getCustomerName() +
                        " was paid from card ending in " + card.getLast4() +
                        ". New card balance: $" + card.getBalance() + ".",
                formatBillTitle(saved.getBillType()) + " bill paid — $" + total,
                Message.MessageType.NOTIFICATION
        );

        return toResponse(saved, card.getLast4(), card.getBalance(), tax);
    }

    public List<BillPaymentResponse> getBillHistory(String email) {
        User user = getUser(email);
        return billPaymentRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(b -> toResponse(b, null, null, b.getTax()))
                .collect(Collectors.toList());
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private String formatBillTitle(String billType) {
        if (billType == null) return "Bill";
        return switch (billType.toLowerCase()) {
            case "electric", "electricity" -> "Electricity";
            case "water"                   -> "Water";
            case "mobile", "phone"         -> "Mobile";
            case "internet"                -> "Internet";
            case "gas"                     -> "Gas";
            case "tv", "cable"             -> "TV/Cable";
            default -> billType.substring(0, 1).toUpperCase() + billType.substring(1).toLowerCase();
        };
    }

    private String getBillEmoji(String billType) {
        if (billType == null) return "📄";
        return switch (billType.toLowerCase()) {
            case "electric", "electricity" -> "⚡";
            case "water"                   -> "💧";
            case "mobile", "phone"         -> "📱";
            case "internet"                -> "🌐";
            case "gas"                     -> "🔥";
            case "tv", "cable"             -> "📺";
            default                        -> "📄";
        };
    }

    private BillPaymentResponse toResponse(BillPayment b,
                                           String cardLast4,
                                           BigDecimal newCardBalance,
                                           BigDecimal tax) {
        return BillPaymentResponse.builder()
                .id(b.getId())
                .billType(b.getBillType())
                .billCode(b.getBillCode())
                .customerName(b.getCustomerName())
                .amount(b.getAmount())
                .tax(tax != null ? tax : b.getTax())
                .status(b.getStatus().name())
                .cardLast4(cardLast4)
                .newCardBalance(newCardBalance)
                .createdAt(b.getCreatedAt())
                .build();
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }
}