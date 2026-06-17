package com.cabank.service.impl;

import com.cabank.dto.request.CurrencyExchangeRequest;
import com.cabank.dto.response.CurrencyExchangeResponse;
import com.cabank.entity.Card;
import com.cabank.entity.CurrencyExchange;
import com.cabank.entity.ExchangeRate;
import com.cabank.entity.Transaction;
import com.cabank.entity.User;
import com.cabank.exception.BadRequestException;
import com.cabank.exception.ResourceNotFoundException;
import com.cabank.repository.CardRepository;
import com.cabank.repository.CurrencyExchangeRepository;
import com.cabank.repository.ExchangeRateRepository;
import com.cabank.repository.TransactionRepository;
import com.cabank.repository.UserRepository;
import com.cabank.websocket.WebSocketEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CurrencyExchangeService {

    private final CurrencyExchangeRepository exchangeRepository;
    private final ExchangeRateRepository exchangeRateRepository;
    private final UserRepository userRepository;
    private final CardRepository cardRepository;
    private final TransactionRepository transactionRepository;
    private final WebSocketEventPublisher eventPublisher;

    @Transactional
    public CurrencyExchangeResponse createExchange(String email, CurrencyExchangeRequest req) {
        if (req.getFromCurrencyCode().equalsIgnoreCase(req.getToCurrencyCode())) {
            throw new BadRequestException("From and To currencies must be different");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        ExchangeRate fromRate = exchangeRateRepository.findByCurrencyCode(req.getFromCurrencyCode())
                .orElseThrow(() -> new ResourceNotFoundException("Currency not found: " + req.getFromCurrencyCode()));

        ExchangeRate toRate = exchangeRateRepository.findByCurrencyCode(req.getToCurrencyCode())
                .orElseThrow(() -> new ResourceNotFoundException("Currency not found: " + req.getToCurrencyCode()));

        BigDecimal rate = toRate.getBuyRate()
                .divide(fromRate.getBuyRate(), new MathContext(10, RoundingMode.HALF_UP));

        BigDecimal toAmount = req.getFromAmount()
                .multiply(rate)
                .setScale(4, RoundingMode.HALF_UP);

        // Deduct fromAmount from the user's primary (first active) card
        List<Card> cards = cardRepository.findByUserIdAndActiveTrue(user.getId());
        if (cards.isEmpty()) {
            throw new BadRequestException("No active card found to debit the exchange amount");
        }
        Card card = cards.get(0);

        if (card.getBalance().compareTo(req.getFromAmount()) < 0) {
            throw new BadRequestException(
                "Insufficient card balance. Required: " + req.getFromAmount() +
                ", Available: " + card.getBalance());
        }

        card.setBalance(card.getBalance().subtract(req.getFromAmount()));
        cardRepository.save(card);

        CurrencyExchange exchange = CurrencyExchange.builder()
                .user(user)
                .fromCurrencyCode(req.getFromCurrencyCode())
                .toCurrencyCode(req.getToCurrencyCode())
                .fromAmount(req.getFromAmount())
                .toAmount(toAmount)
                .exchangeRate(rate.setScale(6, RoundingMode.HALF_UP))
                .build();

        CurrencyExchange saved = exchangeRepository.save(exchange);

        // Record as a transaction
        Transaction tx = transactionRepository.save(Transaction.builder()
                .title("Exchange " + req.getFromCurrencyCode() + " → " + req.getToCurrencyCode())
                .category("exchange")
                .amount(req.getFromAmount())
                .type(Transaction.TransactionType.DEBIT)
                .status(Transaction.TransactionStatus.COMPLETED)
                .emoji("💱")
                .description(req.getFromAmount() + " " + req.getFromCurrencyCode()
                        + " → " + toAmount.setScale(2, RoundingMode.HALF_UP) + " " + req.getToCurrencyCode())
                .user(user)
                .build());

        // Push real-time updates
        eventPublisher.cardBalanceUpdated(email, card.getId(), card.getLast4(), card.getBalance());
        eventPublisher.newTransaction(email, buildTxPayload(tx));

        return toResponse(saved);
    }

    public List<CurrencyExchangeResponse> getHistory(String email) {
        return exchangeRepository.findByUserEmailOrderByCreatedAtDesc(email)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    private CurrencyExchangeResponse toResponse(CurrencyExchange r) {
        return CurrencyExchangeResponse.builder()
                .id(r.getId())
                .fromCurrencyCode(r.getFromCurrencyCode())
                .toCurrencyCode(r.getToCurrencyCode())
                .fromAmount(r.getFromAmount())
                .toAmount(r.getToAmount())
                .exchangeRate(r.getExchangeRate())
                .status(r.getStatus().name())
                .createdAt(r.getCreatedAt())
                .build();
    }

    private Map<String, Object> buildTxPayload(Transaction tx) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", tx.getId());
        map.put("title", tx.getTitle());
        map.put("amount", tx.getAmount());
        map.put("type", tx.getType().name());
        map.put("category", tx.getCategory());
        map.put("emoji", tx.getEmoji());
        map.put("status", tx.getStatus().name());
        return map;
    }
}
