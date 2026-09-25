package com.example.bl_mesh_msg.mesh;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.util.Log;

import com.example.bl_mesh_msg.ble.BLEAdvertiser;
import com.example.bl_mesh_msg.ble.BLEScanner;
import com.example.bl_mesh_msg.ble.GattClient;
import com.example.bl_mesh_msg.ble.GattServer;
import com.example.bl_mesh_msg.model.Message;

import java.util.HashMap;
import java.util.Map;

public class MeshManager implements GattServer.ServerListener, BLEScanner.ScanListener {

    private static final String TAG = "MeshManager";

    public interface MeshEventListener {
        void onMessageReceived(Message message);
        void onPeerDiscovered(String deviceAddress);
        void onLogUpdate(String log);
    }

    private final Context context;
    private final String myDeviceId;
    private final BLEAdvertiser advertiser;
    private final BLEScanner scanner;
    private final GattServer gattServer;
    private final GattClient gattClient;
    private final MessageManager messageManager;

    private final Map<String, BluetoothDevice> nearbyPeers = new HashMap<>();
    private MeshEventListener listener;

    public MeshManager(Context context, String myDeviceId) {
        this.context = context;
        this.myDeviceId = myDeviceId;
        this.advertiser = new BLEAdvertiser();
        this.scanner = new BLEScanner();
        this.gattServer = new GattServer();
        this.gattClient = new GattClient();
        this.messageManager = new MessageManager();
    }

    public void setListener(MeshEventListener listener) {
        this.listener = listener;
    }

    public void startMesh() {
        gattServer.startServer(context, this);
        advertiser.startAdvertising(myDeviceId);
        scanner.startScan(this);
        log("Mesh network started for device: " + myDeviceId);
    }

    public void stopMesh() {
        advertiser.stopAdvertising();
        scanner.stopScan();
        gattServer.stopServer();
        log("Mesh network stopped.");
    }

    @Override
    public void onDeviceFound(BluetoothDevice device) {
        if (!nearbyPeers.containsKey(device.getAddress())) {
            nearbyPeers.put(device.getAddress(), device);
            log("Discovered nearby peer: " + device.getAddress());
            if (listener != null) {
                listener.onPeerDiscovered(device.getAddress());
            }
        }
    }

    @Override
    public void onMessageReceived(String rawMessage, BluetoothDevice senderDevice) {
        Message message = Message.jsonToMessage(rawMessage);
        if (message == null) return;

        log("Received message packet: ID=" + message.getMessageId() + " TTL=" + message.getTtl());

        // Process message through MessageManager
        if (!messageManager.processIncomingMessage(message, myDeviceId)) {
            return; // Dropped because duplicate or TTL <= 0
        }

        if (messageManager.isForMe(message, myDeviceId)) {
            log("Message is intended for ME! Text: " + message.getText());
            if (listener != null) {
                listener.onMessageReceived(message);
            }
        } else {
            log("Message is NOT for me (Target: " + message.getReceiverId() + "). Attempting to RELAY...");
            relayMessage(message);
        }
    }

    public void sendMessage(Message message) {
        // Process local message first to record message ID in duplicate set
        messageManager.isDuplicate(message.getMessageId());
        log("Sending message to peers... Target: " + message.getReceiverId());
        broadcastToPeers(message);
    }

    private void relayMessage(Message message) {
        Message relayedMessage = messageManager.prepareForRelay(message);
        if (relayedMessage != null) {
            log("Relaying message ID=" + relayedMessage.getMessageId() + " with decremented TTL=" + relayedMessage.getTtl());
            broadcastToPeers(relayedMessage);
        }
    }

    private void broadcastToPeers(Message message) {
        if (nearbyPeers.isEmpty()) {
            log("No nearby peers found to transmit message!");
            return;
        }

        String json = message.toJson();
        for (BluetoothDevice device : nearbyPeers.values()) {
            log("Transmitting packet to peer: " + device.getAddress());
            gattClient.sendMessage(context, device, json, new GattClient.SendCallback() {
                @Override
                public void onSuccess() {
                    log("Packet successfully delivered to peer: " + device.getAddress());
                }

                @Override
                public void onFailure(String error) {
                    log("Failed to deliver packet to peer: " + device.getAddress() + " Error: " + error);
                }
            });
        }
    }

    private void log(String msg) {
        Log.d(TAG, msg);
        if (listener != null) {
            listener.onLogUpdate(msg);
        }
    }
}