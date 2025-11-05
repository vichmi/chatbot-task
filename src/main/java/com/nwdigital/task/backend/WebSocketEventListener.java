package com.nwdigital.task.backend;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;

@Component
public class WebSocketEventListener {
    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketEventListener(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectEvent event) {
        System.out.println("A client is connected");

        new Thread(() -> {
            try {
                Thread.sleep(100);
            }catch(InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            this.messagingTemplate.convertAndSend("/topic/welcome", "A new user has connected!");
        }).start();
    }
}
