package com.nwdigital.task.backend.controllers;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

public class ChatBot {
    private String name;
    private JSONObject flow;
    private String currentNode;

    public ChatBot(JSONObject flow) {
        this.name = "ChatBot";
        this.flow = flow;
        this.currentNode = this.flow.getString("start_block_id");
    }

    public void start() {
        JSONArray blocks = this.flow.getJSONArray("blocks");
        Map<String, JSONObject> blockMap = new HashMap<>();

        for(int i=0;i<blocks.length();i++) {
            JSONObject block = blocks.getJSONObject(i);
            blockMap.put(block.getString("id"), block);
        }

        while(!this.currentNode.equals("end")) {
            JSONObject block = blockMap.get(this.currentNode);
            String type = block.getString("type");

            if(type.equals("send_message")) {
                // ws sends here based on block.message
            }else if(type.equals("wait_response")) {
                // bot waits for the response of the user
            }else if(type.equals("recognize_intent")) {
                JSONArray intents = block.getJSONArray("intents");
                JSONObject branches = block.getJSONObject("branches");
                String usermsg = "";
                for(int i=0;i<intents.length();i++) {
                    String intent = intents.getString(i);
                    if(usermsg.contains(intent)) {
                        this.currentNode = branches.getString(intent);
                    }
                }
            }else{
                // Something went wrong
            }

        }
    }

    public JSONObject getFlow() {
        return this.flow;
    }

    public void setFlow(JSONObject flow) {
        this.flow = flow;
    }

    public String getName() {return this.name;}
}
