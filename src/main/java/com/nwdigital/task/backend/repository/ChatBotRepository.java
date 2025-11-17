package com.nwdigital.task.backend.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.nwdigital.task.backend.models.ChatBotFlow;

@Repository
public interface ChatBotRepository extends MongoRepository<ChatBotFlow, String> {
    
}