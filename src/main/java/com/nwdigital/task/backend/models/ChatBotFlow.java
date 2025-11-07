package com.nwdigital.task.backend.models;
import java.util.List;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "chatbotflow")
public class ChatBotFlow {
    @Id
    private String id;
    private String start_block_id;
    private List<Block> blocks;

    public String getId() {return this.id;}
    public void setId(String id) {this.id = id;}
    // public String getName() {return this.name;}
    // public void setName(String name) {this.name = name;}
    public String getStart_block_id() {return this.start_block_id;}
    public void setStart_block_id(String start_block_id) {this.start_block_id = start_block_id;}
    
    public List<Block> getBlocks() {return this.blocks;}
    public void setBlocks(List<Block> blocks) {this.blocks = blocks;}
}