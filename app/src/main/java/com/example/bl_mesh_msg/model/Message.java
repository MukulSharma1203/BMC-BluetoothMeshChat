package com.example.bl_mesh_msg.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;

public class Message {

    private String messageId;
    private String senderId;
    private String receiverId;
    private String text;
    private int ttl;

    // Constructor to create a new outbound message
    public Message(String senderId, String receiverId, String text, int ttl) {
        this.messageId = UUID.randomUUID().toString();
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.text = text;
        this.ttl = ttl;
    }

    // Constructor to reconstruct an existing message
    public Message(String messageId, String senderId, String receiverId, String text, int ttl) {
        this.messageId = messageId;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.text = text;
        this.ttl = ttl;
    }

    public String getMessageId() { return messageId; }
    public String getSenderId() { return senderId; }
    public String getReceiverId() { return receiverId; }
    public String getText() { return text; }
    public int getTtl() { return ttl; }

    public void setTtl(int ttl) { this.ttl = ttl; }

    // Convert Message object to JSON string for BLE transmission
    public String toJson() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("messageId", messageId);
            jsonObject.put("senderId", senderId);
            jsonObject.put("receiverId", receiverId);
            jsonObject.put("text", text);
            jsonObject.put("ttl", ttl);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject.toString();
    }

    // Reconstruct Message object from received JSON string
    public static Message jsonToMessage(String jsonString) {
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            return new Message(
                    jsonObject.getString("messageId"),
                    jsonObject.getString("senderId"),
                    jsonObject.getString("receiverId"),
                    jsonObject.getString("text"),
                    jsonObject.getInt("ttl")
            );
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }
}