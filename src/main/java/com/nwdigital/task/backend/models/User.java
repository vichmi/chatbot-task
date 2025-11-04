package com.nwdigital.task.backend.models;

import java.util.ArrayList;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.nwdigital.task.backend.controllers.Chat;

@Document(collection = "users")
public class User {
    @Id
    private String id;
    private ArrayList<Chat> chats;

    private String name;
    public User() {}

    public User(String name) {
        this.name = name;
    }

    public String getName() {return this.name;}
    public ArrayList<Chat> getChats() {return this.chats;}
    public void setName(String name) {this.name = name;}
    public String getId() {return this.id;}
}
