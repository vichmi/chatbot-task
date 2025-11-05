package com.nwdigital.task.backend.controllers;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.nwdigital.task.backend.models.Block;
import com.nwdigital.task.backend.models.ChatBotFlow;
import com.nwdigital.task.backend.models.ChatBotRepository;

@Service
public class ChatBot {
    private final ChatBotRepository flowRepo;
    private final Map<String, String> userStates = new HashMap<>();

    public ChatBot(ChatBotRepository flowRepo) {
        this.flowRepo = flowRepo;
    }

    public String processMessage(String userId, String userMessage) {
        ChatBotFlow flow = flowRepo.findAll().get(0);
        String currentBlockId = userStates.getOrDefault(userId, flow.getStart_block_id());
        Block block = flow.getBlocks().stream()
            .filter(b -> currentBlockId.equals(b.getId()))
            .findFirst()
            .orElseThrow();

        System.out.println("=== Process Message ===");
        System.out.println("User: " + userId);
        System.out.println("Message: '" + userMessage + "'");
        System.out.println("Current Block: " + currentBlockId);
        System.out.println("Block Type: " + block.getType());

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
        }
        return "Unknown block type: " + block.getType();
    }
    
    private String handleSendMessage(String userId, Block block, ChatBotFlow flow, String userMessage) {
        String response = getDynamicMessage(block);
        if(block.getNextBlock() != null) {
            userStates.put(userId, block.getNextBlock());
            System.out.println("Updated state to: " + block.getNextBlock());
            
            Block nextBlock = getBlockById(flow, block.getNextBlock());
            if(nextBlock != null) {
                if(nextBlock.getType().equals("send_message")) {
                    return response + "\n" + processBlock(userId, nextBlock, userMessage, flow);
                }
                else if(nextBlock.getType().equals("wait_response")) {
                    return response;
                }
                else {
                    return response + "\n" + processBlock(userId, nextBlock, userMessage, flow);
                }
            }
        }
        
        return response;
    }

    private String getDynamicMessage(Block block) {
        String blockId = block.getId();
        if(blockId.equals("time_response")) {
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("h:mm a");
            return "Time is: " + now.format(formatter);
        }else if(blockId.equals("weather_response")) {
            return "20 degreese";
        }
        return block.getMessage();
    }
    
    private String handleWaitForResponse(String userId, Block block, ChatBotFlow flow, String userMessage) {
        if(userMessage == null || userMessage.trim().isEmpty()) {
            System.out.println("Waiting for user response...");
            return "";
        }
        System.out.println("User responded: '" + userMessage + "'");
        if(block.getNextBlock() != null) {
            userStates.put(userId, block.getNextBlock());
            System.out.println("Updated state to: " + block.getNextBlock());
            
            Block nextBlock = getBlockById(flow, block.getNextBlock());
            if(nextBlock != null) {
                return processBlock(userId, nextBlock, userMessage, flow);
            }
        }
        return "No next block";
    }
    
    private String handleRecognizeIntent(String userId, Block block, ChatBotFlow flow, String userMessage) {
        if(userMessage == null || userMessage.trim().isEmpty()) {
            return "error expected message";
        }

        System.out.println("Recognizing intent from: '" + userMessage + "'");
        String nextBlockId = detectIntent(block, userMessage);
        System.out.println("Detected next block: " + nextBlockId);

        userStates.put(userId, nextBlockId);
        Block nextBlock = getBlockById(flow, nextBlockId);
        if(nextBlock != null) {
            return processBlock(userId, nextBlock, userMessage, flow);
        }

        return "error couldnt find: " + nextBlockId;
    }

    private String detectIntent(Block block, String userMessage) {
        if(block.getIntents() == null || block.getBranches() == null) {
            return block.getNextBlock() != null ? block.getNextBlock() : "end";
        }
        String lowerMessage = userMessage.toLowerCase().trim();

        for(String intent: block.getIntents()) {
            if(lowerMessage.contains(intent.toLowerCase())) {
                String branchBlockId = block.getBranches().get(intent);
                if(branchBlockId != null) {
                    System.out.println("✓ Matched intent: " + intent + " -> " + branchBlockId);
                    return branchBlockId;
                }
            }
        }
        
        String defaultNext = block.getNextBlock() != null ? block.getNextBlock() : "end";
        System.out.println("✗ No intent matched, using default: " + defaultNext);
        return defaultNext;
    }

    private String handleEnd(String userId, Block block) {
        System.out.println("Conversation ended for: " + userId);
        userStates.remove(userId);
        return block.getMessage() != null ? block.getMessage() : "Goodbye!";
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
}