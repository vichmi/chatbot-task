package com.nwdigital.client;

import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.nwdigital.task.backend.models.Block;


@Component
public class OpenAIClient {
    private final RestTemplate restTemplate;
    @Value("${OPENAI_SECRETKEY:}")
    private String OPENAI_SECRETKEY;

    public OpenAIClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String callOpenAi(String lastQuestion, String lowerMessage, Block block) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer "+OPENAI_SECRETKEY);
            headers.set("Content-Type", "application/json");
            String intentsStr = String.join(", ", block.getIntents());
            String requestBodyRaw = """
                {
                    "model": "gpt-3.5-turbo",
                    "messages": [
                        {"role": "system", "content": "You are a text classfier. Respond with ONE WORD that is the intent the user wants. Here are the available intents: %s"},
                        {"role": "assistant", "content": "%s"},
                        {"role": "user", "content": "%s"}
                    ]
                }
            """.formatted(intentsStr, lastQuestion, lowerMessage);

            byte[] bytes = requestBodyRaw.getBytes(StandardCharsets.UTF_8);
            String requestBody = new String(bytes, StandardCharsets.UTF_8);
            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.exchange("https://api.openai.com/v1/chat/completions", HttpMethod.POST, entity, String.class);
            JSONObject resBody = new JSONObject(response.getBody());
            JSONArray choices = resBody.getJSONArray("choices");
            String openaiIntent = choices.getJSONObject(0).getJSONObject("message").getString("content");
            String branchBlockId = block.getBranches().get(openaiIntent);
            if(branchBlockId != null) {
                return branchBlockId;
            }
        }catch(HttpClientErrorException.Unauthorized e) {
            for(String intent: block.getIntents()) {
                if(lowerMessage.contains(intent.toLowerCase())) {
                    String branchBlockId = block.getBranches().get(intent);
                    if(branchBlockId != null) {
                        return branchBlockId;
                    }
                }
            }
        }
        return null;
    }
}
