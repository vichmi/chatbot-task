package com.nwdigital.task.backend.models;

import java.util.List;
import java.util.Map;

import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class Block {
    private String id;
    private String type;
    private String message;
    private String nextBlock;
    private List<String> intents;
    private Map<String, String> branches;
    
    public String getId() {return this.id;}
    public String getType() {return this.type;}
    public String getMessage() {return this.message;}
    public String getNextBlock() {return this.nextBlock;}
    public List<String> getIntents() {return this.intents;}
    public Map<String, String> getBranches() {return this.branches;}
    
    public void setId(String id) {this.id = id;}
    public void setType(String type) {this.type = type;}
    public void setMessage(String message) {this.message = message;}
    public void setNextBlock(String nextBlock) {this.nextBlock = nextBlock;}
    public void setIntents(List<String> intents) {this.intents = intents;}
    public void setBranches(Map<String, String> branches) {this.branches = branches;}
}