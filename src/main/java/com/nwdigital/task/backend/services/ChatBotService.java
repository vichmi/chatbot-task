package com.nwdigital.task.backend.services;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nwdigital.task.backend.client.OpenAIClient;
import com.nwdigital.task.backend.exceptions.IntentNotRecognizedException;
import com.nwdigital.task.backend.models.Block;
import com.nwdigital.task.backend.models.ChatBotFlow;
import com.nwdigital.task.backend.models.ConversationHistory;
import com.nwdigital.task.backend.repository.ChatBotRepository;
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
    private MongoTemplate mongoTemplate;

    @Autowired
    private ResourceLoader resourceLoader;

    @Autowired
    ObjectMapper objectMapper;

    private void initializeFlow() {
        if (this.flowRepo.count() > 0) {
            return;
        }
        try {
            org.springframework.core.io.Resource resource = resourceLoader.getResource("classpath:flow.json");
            
            if (!resource.exists()) {
                throw new RuntimeException("flow.json not found in classpath");
            }
            
            ChatBotFlow flow = objectMapper.readValue(resource.getInputStream(), ChatBotFlow.class);
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
        initializeFlow();
        ChatBotFlow flow = flowRepo.findAll().get(0);
        String currentBlockId = userStates.getOrDefault(userId, flow.getStart_block_id());
        Block block = flow.getBlocks().stream()
            .filter(b -> currentBlockId.equals(b.getId()))
            .findFirst()
            .orElseThrow();
        Message usrMsg = new Message(userMessage, userId, block.getId());
        // conversationHistoryRepository.insert(ConversationHistory.builder().message(usrMsg).blockId(block.getId()).build());
        saveMessage(userId, usrMsg);
        return processBlock(userId, block, userMessage, flow);
    }
    
    private String processBlock(String userId, Block block, String userMessage, ChatBotFlow flow) {

        switch(block.getType()) {
            case "send_message":
                return handleSendMessage(userId, block, flow, userMessage);
            case "wait_response":
                return handleWaitForResponse(userId, block, flow, userMessage);
            case "recognize_intent":
                return handleRecognizeIntent(userId, block, flow, userMessage);
            case "end":
                return handleEnd(userId, block);
            case "misunderstood":
                return "Unknown block" + block.getType();
        }
        return "Unknown block" + block.getType();
    }
    
    private String handleSendMessage(String userId, Block block, ChatBotFlow flow, String userMessage) {
        String response = getDynamicMessage(block);
        if(block.getNextBlock() != null) {
            userStates.put(userId, block.getNextBlock());
            this.lastQuestion = response;
        
            Block nextBlock = getBlockById(flow, block.getNextBlock());
            saveMessage(userId, new Message(response, "bot", block.getId()));
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
        try {
            String nextBlockId = detectIntent(block, userMessage);
            Block nextBlock = getBlockById(flow, nextBlockId);

            if(nextBlock == null) {
                return handleMisUnderstand(userId, block);
            }
            userStates.put(userId, nextBlockId);
            return processBlock(userId, nextBlock, userMessage, flow);
        } catch (IntentNotRecognizedException e) {
            return handleMisUnderstand(userId, block);
        }
    }

    private String detectIntent(Block block, String userMessage) {
        if(block.getIntents() == null || block.getBranches() == null) {
            return block.getNextBlock() != null ? block.getNextBlock() : "end";
        }
        String lowerMessage = userMessage.toLowerCase().trim();
        
        OpenAIClient openAIClient = new OpenAIClient(new RestTemplate());
        String intentBlockId = openAIClient.callOpenAi(this.lastQuestion, lowerMessage, block);
        if(intentBlockId != null) {
            System.out.println(intentBlockId);
            return intentBlockId;
        }
        throw new IntentNotRecognizedException("Couldn't determine user message: " + userMessage);
    }



    private String handleMisUnderstand(String userId, Block block) {

        userStates.put(userId, block.getId());

        // conversationHistoryRepository.insert(new ConversationHistory(new Message("I didn't understand that.", "bot"), block.getId()));
        // conversationHistoryRepository.insert(ConversationHistory.builder().message(new Message("I didn't understand that.", "bot")).blockId(block.getId()).build());
        Message botMessage = new Message("I didn't get that. Can you try again?", "bot", block.getId());
        saveMessage(userId, botMessage);
        return botMessage.getContent();
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

    private void saveMessage(String userId, Message message) {
        Query query = new Query(Criteria.where("userId").is(userId));
        Update update = new Update().push("messages", message);
        mongoTemplate.upsert(query, update, ConversationHistory.class);

    }
}