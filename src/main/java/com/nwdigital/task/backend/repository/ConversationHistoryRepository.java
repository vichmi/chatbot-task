package com.nwdigital.task.backend.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.nwdigital.task.backend.models.ConversationHistory;

@Repository
public interface ConversationHistoryRepository extends MongoRepository<ConversationHistory, String> {
    
}