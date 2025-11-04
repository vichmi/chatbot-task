package com.nwdigital.task.backend.controllers;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class MainController {
    @MessageMapping("/hello")
    @SendTo("/topic/greetings")
    public String hello() {
        return "Hello world";
    }
}