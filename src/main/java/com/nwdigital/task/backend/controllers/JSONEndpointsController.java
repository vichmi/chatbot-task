package com.nwdigital.task.backend.controllers;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nwdigital.task.backend.models.ChatBotFlow;
import com.nwdigital.task.backend.services.JSONEnpointsService;

@RestController
@RequestMapping("/api/v1")
public class JSONEndpointsController {
   
    @Autowired
    private JSONEnpointsService jsonService;

    @PostMapping("/createConfig")
    public ChatBotFlow createConfig(@RequestBody ChatBotFlow chatBotFlow) {
        return jsonService.createConfig(chatBotFlow);
    }
    @GetMapping("/getConfig")
    public ChatBotFlow getConfig() {
        return jsonService.getConfig();
    }
}
