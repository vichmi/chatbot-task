package com.nwdigital.task.backend.controllers;

public class HelloMessage {
    private String name;
    
    public HelloMessage() {}

    public HelloMessage(String name) {
        this.name = name;
    }

    public String getName() {return this.name;}

    public void setName(String name) {this.name = name;}
}
