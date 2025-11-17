package com.nwdigital.task.backend.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nwdigital.task.backend.models.ChatBotFlow;
import com.nwdigital.task.backend.repository.ChatBotRepository;

@Service
public class JSONEnpointsService {
    @Autowired
    private ChatBotRepository chatBotRepository;

    public ChatBotFlow createConfig(ChatBotFlow chatBotFlow) {
        if(chatBotRepository.count() >= 1) {chatBotRepository.deleteAll();}
        return chatBotRepository.save(chatBotFlow);
    }

    public ChatBotFlow getConfig() {
        return chatBotRepository.findAll().get(0);
    }
}
