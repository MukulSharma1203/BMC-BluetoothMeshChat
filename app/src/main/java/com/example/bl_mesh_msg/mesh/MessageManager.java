package com.example.bl_mesh_msg.mesh;

import android.util.Log;

import com.example.bl_mesh_msg.model.Message;

import java.util.HashSet;
import java.util.Set;

public class MessageManager {

    private static final String TAG = "MessageManager";
    private final Set<String> seenMessageIds = new HashSet<>();

    public boolean isDuplicate(String messageId) {
        if (seenMessageIds.contains(messageId)) {
            Log.d(TAG, "Duplicate message detected and dropped: " + messageId);
            return true;
        }
        seenMessageIds.add(messageId);
        return false;
    }

    public boolean processIncomingMessage(Message message, String myDeviceId) {
        // 1. Check if already processed
        if (isDuplicate(message.getMessageId())) {
            return false;
        }

        // 2. Check TTL (Time To Live)
        if (message.getTtl() <= 0) {
            Log.d(TAG, "Message TTL expired (<= 0), dropping message: " + message.getMessageId());
            return false;
        }

        return true;
    }

    public boolean isForMe(Message message, String myDeviceId) {
        return message.getReceiverId().equalsIgnoreCase(myDeviceId);
    }

    public Message prepareForRelay(Message message) {
        // Decrement TTL before forwarding
        int newTtl = message.getTtl() - 1;
        if (newTtl <= 0) {
            Log.d(TAG, "Message TTL reached 0 after decrement. Will not relay.");
            return null;
        }
        return new Message(
                message.getMessageId(),
                message.getSenderId(),
                message.getReceiverId(),
                message.getText(),
                newTtl
        );
    }
}