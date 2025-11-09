package com.nwdigital.task.backend.models;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatBotRepository extends MongoRepository<ChatBotFlow, String> {
    
}