package com.nwdigital.task.backend.models;

import java.time.LocalDateTime;

public class Message {
    private String content;
    private LocalDateTime date;
    private String sender;
    public Message() {}
    public Message(String content, String sender) {
        this.content = content;
        this.date = LocalDateTime.now();
        this.sender = sender;
    }

    public String getContent() {return this.content;}
    public LocalDateTime getDate() {return this.date;}
    public String getSender() {return this.sender;}

    public void setContent(String content) {this.content = content;}
    public void setSender(String sender) {this.sender = sender;}
}