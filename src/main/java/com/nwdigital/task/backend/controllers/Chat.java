package com.nwdigital.task.backend.controllers;

import java.util.ArrayList;

public class Chat {
    private ArrayList<Message> messages;
    private String name;
    public Chat(String name) {
        this.name = name;
    }

    public ArrayList<Message> getMessages() {
        return this.messages;
    }

    public String getName() {return this.name;}
    public void setName(String n) {this.name = n;}

    public void setMessages(ArrayList<Message> messages) {
        this.messages = messages;
    }

    public void addMessage(Message msg) {
        this.messages.add(msg);
    }    
}
