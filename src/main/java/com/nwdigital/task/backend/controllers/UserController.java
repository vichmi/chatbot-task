package com.nwdigital.task.backend.controllers;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.nwdigital.task.backend.models.User;
import com.nwdigital.task.backend.models.UserRepository;

@RestController
public class UserController {

    private final BCryptPasswordEncoder passEncoder = new BCryptPasswordEncoder();

    @Autowired
    private UserRepository userRepo;

    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody Map<String, String> credentials) {
        String username = credentials.get("username");
        String password = credentials.get("password");
        if(userRepo.findByUsername(username).orElse(null) != null) {
            return Map.of("sucess", false, "message", "username already exists");
        }

        String hashedPassowrd = passEncoder.encode(password);
        userRepo.save(new User(username, hashedPassowrd));

        return Map.of("sucess", true, "message", "Sucessfuly created user");
    }
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> credentials) {
        String username = credentials.get("username");
        String password = credentials.get("password");
        User user = userRepo.findByUsername(username).orElse(null);
        if(user == null) {
            return Map.of("sucess", false, "message", "User not found");
        }
        if(passEncoder.matches(password, user.getPassword())) {
            return Map.of("sucess", true, "message", "Login sucessfully");
        }else{
            return Map.of("sucess", false, "message", "Invalid password");
        }
    }
}
