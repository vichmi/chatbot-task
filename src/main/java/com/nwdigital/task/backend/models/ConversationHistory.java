package com.nwdigital.task.backend.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "conversations")
public class ConversationHistory {
    @Id
    private String id;
    private Message message;
    private Block block;

    public ConversationHistory(Message message, Block block) {
        setMessage(message);
        setBlock(block);
    }

    public Message getMessage() {return this.message;}
    public Block getBlock() {return this.block;}
    public void setMessage(Message newMessage) {this.message = newMessage;}
    public void setBlock(Block block) {this.block = block;}
}
