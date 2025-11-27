package com.ApiarioSamano.MicroServiceApiarios.config;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Component
public class SensorWebSocketHandler extends TextWebSocketHandler {

    private static final Set<WebSocketSession> sessions = Collections.synchronizedSet(new HashSet<>());

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.add(session);
        System.out.println("🔌 WebSocket conectado: " + session.getId());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session);
        System.out.println("🔌 WebSocket desconectado: " + session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // Manejar mensajes entrantes si es necesario
        System.out.println("📨 Mensaje WebSocket recibido: " + message.getPayload());
    }

    // 🔥 MÉTODO PARA ENVIAR DATOS A TODOS LOS CLIENTES
    public static void enviarATodos(String mensaje) {
        synchronized (sessions) {
            for (WebSocketSession session : sessions) {
                try {
                    if (session.isOpen()) {
                        session.sendMessage(new TextMessage(mensaje));
                    }
                } catch (IOException e) {
                    System.err.println("❌ Error enviando WebSocket: " + e.getMessage());
                }
            }
        }
    }
}