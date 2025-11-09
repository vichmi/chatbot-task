package com.nwdigital.task.backend.services;

import org.springframework.http.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nwdigital.task.backend.models.Block;
import com.nwdigital.task.backend.models.ChatBotFlow;
import com.nwdigital.task.backend.models.ChatBotRepository;
import com.nwdigital.task.backend.models.ConversationHistory;
import com.nwdigital.task.backend.repository.ConversationHistoryRepository;
import com.nwdigital.task.backend.models.Message;

@Service
public class ChatBotService {
    private final ChatBotRepository flowRepo;
    private final Map<String, String> userStates = new HashMap<>();
    private String lastQuestion;

    @Value("${OPENAI_SECRETKEY:}")
    private String OPENAI_SECRETKEY;

    @Autowired
    private ConversationHistoryRepository conversationHistoryRepository;

    @Autowired
    private ResourceLoader resourceLoader;

    private void initialize_flow() {
        if (this.flowRepo.count() > 0) {
            return;
        }
        try {
            ObjectMapper mapper = new ObjectMapper();
            org.springframework.core.io.Resource resource = resourceLoader.getResource("classpath:flow.json");
            
            if (!resource.exists()) {
                throw new RuntimeException("flow.json not found in classpath");
            }
            
            ChatBotFlow flow = mapper.readValue(resource.getInputStream(), ChatBotFlow.class);
            this.flowRepo.save(flow);
        } catch (IOException e) {
            throw new RuntimeException("Could not load flow.json: " + e.getMessage(), e);
        }
    }

    public ChatBotService(ChatBotRepository flowRepo) {
        this.flowRepo = flowRepo;
        this.lastQuestion = "";
    }

    public String processMessage(String userId, String userMessage) {
        initialize_flow();
        ChatBotFlow flow = flowRepo.findAll().get(0);
        String currentBlockId = userStates.getOrDefault(userId, flow.getStart_block_id());
        Block block = flow.getBlocks().stream()
            .filter(b -> currentBlockId.equals(b.getId()))
            .findFirst()
            .orElseThrow();
        Message usrMsg = new Message(userMessage, userId);
        conversationHistoryRepository.insert(new ConversationHistory(usrMsg, block));
        return processBlock(userId, block, userMessage, flow);
    }
    
    private String processBlock(String userId, Block block, String userMessage, ChatBotFlow flow) {
        if(block.getType().equals("send_message")) {
            return handleSendMessage(userId, block, flow, userMessage);
        }
        else if(block.getType().equals("wait_response")) {
            return handleWaitForResponse(userId, block, flow, userMessage);
        }
        else if(block.getType().equals("recognize_intent")) {
            return handleRecognizeIntent(userId, block, flow, userMessage);
        }
        else if(block.getType().equals("end")) {
            return handleEnd(userId, block);
        }else if(block.getType().equals("misunderstood")) {return handleMisUnderstand(userId, block);}
        return "Unknown block" + block.getType();
    }
    
    private String handleSendMessage(String userId, Block block, ChatBotFlow flow, String userMessage) {
        String response = getDynamicMessage(block);
        if(block.getNextBlock() != null) {
            userStates.put(userId, block.getNextBlock());
            this.lastQuestion = response;
            
            Block nextBlock = getBlockById(flow, block.getNextBlock());
            conversationHistoryRepository.insert(new ConversationHistory(new Message(response, "bot"), nextBlock));
            if(nextBlock != null) {
                if(nextBlock.getType().equals("send_message")) {
                    this.lastQuestion = response;
                    return response + "\n" + processBlock(userId, nextBlock, userMessage, flow);
                }
                else if(nextBlock.getType().equals("wait_response")) {
                    this.lastQuestion = response;
                    return response;
                }
                else {
                    this.lastQuestion = response + "\n" + processBlock(userId, nextBlock, userMessage, flow); 
                    return response + "\n" + processBlock(userId, nextBlock, userMessage, flow);
                }
            }
        }
        this.lastQuestion = response;
        return response;
    }

    private String getDynamicMessage(Block block) {
        String blockId = block.getId();
        if(blockId.equals("time_response")) {
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("h:mm:ss");
            return "Time is: " + now.format(formatter);
        }else if(blockId.equals("weather_response")) {
            return block.getMessage();
        }
        return block.getMessage();
    }
    
    private String handleWaitForResponse(String userId, Block block, ChatBotFlow flow, String userMessage) {
        if(userMessage == null || userMessage.trim().isEmpty()) {
            return "";
        }
        if(block.getNextBlock() != null) {
            userStates.put(userId, block.getNextBlock());
            
            Block nextBlock = getBlockById(flow, block.getNextBlock());
            if(nextBlock != null) {
                return processBlock(userId, nextBlock, userMessage, flow);
            }
        }
        return "No next block";
    }
    
    private String handleRecognizeIntent(String userId, Block block, ChatBotFlow flow, String userMessage) {
        if(userMessage == null || userMessage.trim().isEmpty()) {
            return "expected message";
        }

        String nextBlockId = detectIntent(block, userMessage);

        if(nextBlockId == null) {
            return handleMisUnderstand(userId, block);
        }
        Block nextBlock = getBlockById(flow, nextBlockId);

        if(nextBlock == null) {
            return handleMisUnderstand(userId, block);
        }
        userStates.put(userId, nextBlockId);
        return processBlock(userId, nextBlock, userMessage, flow);
    }

    private String detectIntent(Block block, String userMessage) {
        if(block.getIntents() == null || block.getBranches() == null) {
            return block.getNextBlock() != null ? block.getNextBlock() : "end";
        }
        String lowerMessage = userMessage.toLowerCase().trim();
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new org.springframework.http.HttpHeaders();
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
            """.formatted(intentsStr, this.lastQuestion, lowerMessage);

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



    private String handleMisUnderstand(String userId, Block block) {

        userStates.put(userId, block.getId());

        conversationHistoryRepository.insert(new ConversationHistory(new Message("I didn't understand that.", "bot"), block));

        return "I didn't get that. Can you try again?";
    }

    private String handleEnd(String userId, Block block) {
        userStates.remove(userId);
        return block.getMessage() != null ? block.getMessage() : "";
    }

    private Block getBlockById(ChatBotFlow flow, String blockId) {
        return flow.getBlocks().stream()
            .filter(b -> blockId.equals(b.getId()))
            .findFirst()
            .orElse(null);
    }

    public String getCurrentBlockId(String userId) {
        return userStates.get(userId);
    }
    
    public void resetUser(String userId) {
        userStates.remove(userId);
    }

    public String getLastQuestion() {return this.lastQuestion;}
}