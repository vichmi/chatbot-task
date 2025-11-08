package com.nwdigital.task.backend.models;

import java.util.ArrayList;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "users")

public class User {
    @Id
    private String id;

    private String username;
    private String password;
    public User() {}

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {return this.username;}
    public void setUsername(String username) {this.username = username;}
    public String getId() {return this.id;}

    public String getPassword() {return this.password;}
    public void setPassword(String passw) {this.password = passw;}
}
