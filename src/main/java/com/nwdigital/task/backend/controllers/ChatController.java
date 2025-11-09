package com.nwdigital.task.backend.controllers;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

import com.nwdigital.task.backend.models.Message;
import com.nwdigital.task.backend.services.ChatBotService;

@Controller
public class ChatController {

    private final ChatBotService chatBot;
    private final SimpMessagingTemplate messaginTemplate;

    private final Map<String, String> sessionToUsername = new ConcurrentHashMap<>();

    public ChatController(ChatBotService chatBot, SimpMessagingTemplate messaginTemplate) {
        this.chatBot = chatBot;
        this.messaginTemplate = messaginTemplate;
    }

    @EventListener
    public void handleWebSocketConnectListener(SessionSubscribeEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap((event.getMessage()));
        String destination = headerAccessor.getDestination();
        if(destination != null && destination.equals("/topic/message")) {
            String sessionId = headerAccessor.getSessionId();
            Map<String, Object> sessionAttributes = headerAccessor.getSessionAttributes();
            String username = null;
            if(sessionAttributes != null) {
                username = (String) sessionAttributes.get("username");
            }
            if(username == null || username.trim().isEmpty()) {
                username = sessionId;
            }
            sessionToUsername.put(sessionId, username);
            String greeting = this.chatBot.processMessage(sessionId, "");
            messaginTemplate.convertAndSend("/topic/message", new Message(greeting, "bot"));
        }
    }

    @MessageMapping("/chat")
    @SendTo("/topic/message")
    public Message handleChat(Message msg, SimpMessageHeaderAccessor headerAccessor) {
        String sessionId = headerAccessor.getSessionId();
        String botReply = this.chatBot.processMessage(sessionId, msg.getContent());
        return new Message(botReply, "bot");
    }

}
