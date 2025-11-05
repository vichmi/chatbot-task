package com.nwdigital.task.backend.controllers;

import java.util.Map;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ChatBotEndpoints {
    
    @PostMapping("/createFlow")
    public void createFlow(@RequestBody Map<String, ?> jsonFlow) {
        
    }
}
