package com.nwdigital.task.backend.models;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface ChatBotRepository extends MongoRepository<ChatBotFlow, String> {
    
}
