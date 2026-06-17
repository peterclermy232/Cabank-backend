package com.cabank.websocket;

import com.cabank.security.JwtUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Raw WebSocket handler.
 * Client connects to:  ws://host:8080/ws?token=JWT
 *
 * Server pushes JSON events:
 *   {"type":"CONNECTED"}
 *   {"type":"BALANCE_UPDATE",  "cardId":"…", "balance":1234.56, "cardLast4":"4242"}
 *   {"type":"ACCOUNT_UPDATE",  "accountId":"…", "balance":9999.00}
 *   {"type":"NEW_TRANSACTION", "transaction":{…}}
 *   {"type":"NEW_MESSAGE",     "message":"…", "title":"…", "msgType":"ALERT"}
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BankingWebSocketHandler extends TextWebSocketHandler {

    private final JwtUtils jwtUtils;
    private final ObjectMapper objectMapper;

    // userId → WebSocketSession (one session per user; last one wins on re-connect)
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    // sessionId → userId (for cleanup on close)
    private final Map<String, String> sessionOwners = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String email = extractEmail(session);
        if (email == null) {
            closeQuietly(session);
            return;
        }

        sessions.put(email, session);
        sessionOwners.put(session.getId(), email);
        sendEvent(email, Map.of("type", "CONNECTED"));
        log.info("WS connected: {} (sessionId={})", email, session.getId());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String email = sessionOwners.remove(session.getId());
        if (email != null) {
            sessions.remove(email, session);
            log.info("WS disconnected: {} status={}", email, status);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        // Client doesn't need to send anything — connection is receive-only.
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable ex) {
        log.warn("WS transport error for session {}: {}", session.getId(), ex.getMessage());
    }

    /** Push a JSON event to a specific user. Silently ignores if user is not connected. */
    public void sendEvent(String email, Object event) {
        WebSocketSession session = sessions.get(email);
        if (session == null || !session.isOpen()) {
            return;
        }
        try {
            String json = objectMapper.writeValueAsString(event);
            synchronized (session) {
                session.sendMessage(new TextMessage(json));
            }
        } catch (IOException e) {
            log.warn("WS send failed to {}: {}", email, e.getMessage());
        }
    }

    /** Extract and validate the JWT from the query string: ?token=... */
    private String extractEmail(WebSocketSession session) {
        URI uri = session.getUri();
        if (uri == null) return null;

        String query = uri.getQuery(); // "token=eyJ..."
        if (query == null) return null;

        for (String param : query.split("&")) {
            if (param.startsWith("token=")) {
                String token = param.substring(6);
                try {
                    if (jwtUtils.validateToken(token)) {
                        return jwtUtils.getEmailFromToken(token);
                    }
                } catch (Exception e) {
                    log.warn("WS invalid JWT: {}", e.getMessage());
                }
                return null;
            }
        }
        return null;
    }

    private void closeQuietly(WebSocketSession session) {
        try {
            session.close(CloseStatus.POLICY_VIOLATION);
        } catch (IOException ignored) {
        }
    }
}
