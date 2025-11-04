package com.nwdigital.task.backend.controllers;

public class ChatBot {
    private String name;
    public ChatBot() {
        this.name = "ChatBot";
    }

    public void sendMessage(String content) {
        Message msg = new Message(content, this.getName());
    }

    public String getName() {return this.name;}
}
