package com.nwdigital.task.backend.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

import com.nwdigital.task.backend.client.OpenAIClient;

@TestConfiguration
public class WebSocketConfigTest {
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
    
    @Bean
    public OpenAIClient openAIClient(RestTemplate restTemplate) {
        return new OpenAIClient(restTemplate);
    }
}
