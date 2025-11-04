package com.nwdigital.task.backend.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.nwdigital.task.backend.models.User;
import com.nwdigital.task.backend.models.UserRepository;

@RestController
public class UserController {
    @Autowired
    private UserRepository userRepo;

    @CrossOrigin
    @PostMapping("/addUser")
    public User addUser(@RequestBody User user ) {
        return userRepo.save(user);
    }

    @GetMapping("/getAllUser")
    public List<User> getAllUser() {
        return userRepo.findAll();
    }
}
