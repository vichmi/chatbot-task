package com.nwdigital.task.backend.controllers;

public class Message {
    private String content;
    private String date;
    private String sender;
    public Message() {}
    public Message(String content, String date, String sender) {
        this.content = content;
        this.date = date;
        this.sender = sender;
    }

    public String getContent() {return this.content;}
    public String getDate() {return this.date;}
    public String getSender() {return this.sender;}

    public void setContent(String content) {this.content = content;}
    public void setDate(String date) {this.date = date;}
    public void setSender(String sender) {this.sender = sender;}
}