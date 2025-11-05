package com.nwdigital.task.backend.controllers;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class ChatController {
    @MessageMapping("/chat")
    @SendTo("/topic/initial")
    public String hello() {
        System.out.println("bvlah");
        return "Hello there maniac";
    }
    @MessageMapping("/createChat")
    @SendTo("/topic/createChat")
    public Chat createChat(Chat ch) {
        
        return ch;
    }
}
