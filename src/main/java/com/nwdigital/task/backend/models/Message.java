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
@AllArgsConstructor
public class Message {
    private String content;
    
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime date;
    
    private String sender;
    
    // public Message() {
    //     this.date = LocalDateTime.now();
    // }

    @JsonCreator
    public Message(
        @JsonProperty("content") String content,
        @JsonProperty("sender") String sender) {
        this.content = content;
        this.date = LocalDateTime.now();
        this.sender = sender;
    }

    // public String getContent() {
    //     return this.content;
    // }

    // public LocalDateTime getDate() {
    //     return this.date;
    // }

    // public String getSender() {
    //     return this.sender;
    // }

    // public void setContent(String content) {
    //     this.content = content;
    // }

    // public void setDate(LocalDateTime date) {
    //     this.date = date;
    // }

    // public void setSender(String sender) {
    //     this.sender = sender;
    // }
}