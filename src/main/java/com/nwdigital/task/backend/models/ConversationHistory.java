package com.nwdigital.task.backend.models;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Document(collection = "conversations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConversationHistory {
    @Id
    private String id;

    private String userId;
    private List<Message> messages = new ArrayList<>();

    // public ConversationHistory(Message message, String blockId) {
    //     this.message = message;
    //     this.blockId = blockId;
    // }

    // public Message getMessage() {return this.message;}
    // public String getBlockId() {return this.blockId;}
    // public void setMessage(Message newMessage) {this.message = newMessage;}
    // public void setBlockId(String blockId) {this.blockId = blockId;}
}
