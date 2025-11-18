package com.nwdigital.task.backend.models;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@NoArgsConstructor
public class Message {
    private String content;
    
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime date;
    
    private String sender;
    private String blockId;

    public Message(String content, String sender, String blockId) {
        this.content = content;
        this.date = LocalDateTime.now();
        this.sender = sender;
        this.blockId = blockId;
    }

    @JsonCreator
    public Message(
        @JsonProperty("content") String content,
        @JsonProperty("sender") String sender) {
        this.content = content;
        this.date = LocalDateTime.now();
        this.sender = sender;
    }
}