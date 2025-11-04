package com.nwdigital.task.backend.controllers;

import java.util.ArrayList;

public class Chat {
    private ArrayList<Message> messages;
    public Chat() {
    }

    public ArrayList<Message> getMessages() {
        return this.messages;
    }

    public void setMessages(ArrayList<Message> messages) {
        this.messages = messages;
    }

    public void addMessage(Message msg) {
        this.messages.add(msg);
    }    
}
