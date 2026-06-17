package com.cabank.websocket;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Thin façade over BankingWebSocketHandler.
 * Services call these methods after committing a money operation.
 */
@Service
@RequiredArgsConstructor
public class WebSocketEventPublisher {

    private final BankingWebSocketHandler handler;

    public void cardBalanceUpdated(String email, String cardId, String last4, BigDecimal newBalance) {
        Map<String, Object> event = new HashMap<>();
        event.put("type", "BALANCE_UPDATE");
        event.put("cardId", cardId);
        event.put("cardLast4", last4);
        event.put("balance", newBalance);
        handler.sendEvent(email, event);
    }

    public void accountBalanceUpdated(String email, String accountId, BigDecimal newBalance) {
        Map<String, Object> event = new HashMap<>();
        event.put("type", "ACCOUNT_UPDATE");
        event.put("accountId", accountId);
        event.put("balance", newBalance);
        handler.sendEvent(email, event);
    }

    public void newTransaction(String email, Object transactionPayload) {
        Map<String, Object> event = new HashMap<>();
        event.put("type", "NEW_TRANSACTION");
        event.put("transaction", transactionPayload);
        handler.sendEvent(email, event);
    }

    public void newMessage(String email, String title, String body, String msgType) {
        Map<String, Object> event = new HashMap<>();
        event.put("type", "NEW_MESSAGE");
        event.put("title", title);
        event.put("message", body);
        event.put("msgType", msgType);
        handler.sendEvent(email, event);
    }
}
