package com.nwdigital.task.backend.controllers;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nwdigital.task.backend.models.ChatBotFlow;
import com.nwdigital.task.backend.models.ChatBotRepository;

@RestController
@RequestMapping("/api/v1")
public class JSONEndpoints {
    
    @Autowired
    private ChatBotRepository chatBotRepository;

    @PostMapping("/createConfig")
    public ChatBotFlow createConfig(@RequestBody ChatBotFlow chatBotFlow) {
        if(chatBotRepository.count() >= 1) {
            chatBotRepository.deleteAll();
        }
        return chatBotRepository.save(chatBotFlow);
    }

    @GetMapping("/getConfig")
    public ChatBotFlow getConfig() {
        return chatBotRepository.findAll().get(0);
    }
}
