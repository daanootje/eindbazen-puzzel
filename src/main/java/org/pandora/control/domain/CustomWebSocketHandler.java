package org.pandora.control.domain;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Component
public class CustomWebSocketHandler implements WebSocketHandler {

    private static final Map<String,WebSocketSession> users = new HashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        users.put(Objects.requireNonNull(session.getUri()).toString().substring(1),session);
        log.info(String.format("Connection established %s on uri: %s", session.getId(), session.getUri()));
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) {
        log.info(String.format("Receiving message with session %s on uri: %s. Doing nothing with message: %s", session.getId(), session.getUri(), message.<String>getPayload()));
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        log.info(String.format("Error with session %s on uri: %s - %s", session.getId(), session.getUri(), exception.getMessage()));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) {
        log.info(String.format("Closing session %s on uri: %s", session.getId(), session.getUri()));
    }

    public void sendMessageToUsers(String user, TextMessage message) {
        for (Map.Entry<String,WebSocketSession> entry : users.entrySet()) {
            try {
                String key = entry.getKey();
                WebSocketSession session = entry.getValue();
                if (key.equals(user) && session.isOpen()) {
                    session.sendMessage(message);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }
}
