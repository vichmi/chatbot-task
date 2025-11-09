package com.nwdigital.task.backend.controllers;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Type;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import com.nwdigital.task.backend.models.Message;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
    "spring.mongodb.embedded.version=3.5.5",
    "OPENAI_SECRETKEY=test-key"
})
public class WebSocketIntegrationTest {
   
    @LocalServerPort
    private int port;
    
    private WebSocketStompClient stompClient;
    private String WEBSOCKET_URI;
    private final String WEBSOCKET_TOPIC = "/topic/message";

    @BeforeEach
    public void setup() {
        this.WEBSOCKET_URI = String.format("ws://localhost:%d/chat", port);
        StandardWebSocketClient client = new StandardWebSocketClient();
        this.stompClient = new WebSocketStompClient(client);
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setObjectMapper(new com.fasterxml.jackson.databind.ObjectMapper()
            .registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule()));
        this.stompClient.setMessageConverter(converter);
    }

    @Test
    public void testWebSocketConnection() throws InterruptedException, ExecutionException, TimeoutException {
        CompletableFuture<Message> completableFuture = new CompletableFuture<>();

        StompSession session = stompClient
            .connectAsync(WEBSOCKET_URI, new StompSessionHandlerAdapter() {})
            .get(1, TimeUnit.SECONDS);

        session.subscribe(WEBSOCKET_TOPIC, new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return Message.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                Message msg = (Message) payload;
                completableFuture.complete(msg);
            }
        });

        Message testMessage = new Message("Test message", "test-user");
        session.send("/app/message", testMessage);

        Message response = completableFuture.get(3, TimeUnit.SECONDS);
        assertNotNull(response);
        assertFalse(response.getContent().isEmpty());
    }

    @Test
    public void testWebSocketDisconnection() throws InterruptedException, ExecutionException, TimeoutException {
        StompSession session = stompClient
            .connectAsync(WEBSOCKET_URI, new StompSessionHandlerAdapter() {})
            .get(1, TimeUnit.SECONDS);
        assertTrue(session.isConnected());
        session.disconnect();
        assertFalse(session.isConnected());
    }
}