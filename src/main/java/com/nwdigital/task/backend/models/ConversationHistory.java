package com.nwdigital.task.backend.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "conversations")
public class ConversationHistory {
    @Id
    private String id;
    
}
