package com.nwdigital.task.backend.models;


import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "users")
public class User {
    @Id
    private String id;

    private String username;
    private String email;
    private String password;

    public User() {}

    public User(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }

    public String getUsername() {return this.username;}
    public void setUsername(String username) {this.username = username;}
    public String getId() {return this.id;}
    public void setId(String id) {this.id = id;}

    public String getEmail() {return this.email;}
    public void setEmail(String email) {this.email = email;}

    public String getPassword() {return this.password;}
    public void setPassword(String password) {this.password = password;}
}
