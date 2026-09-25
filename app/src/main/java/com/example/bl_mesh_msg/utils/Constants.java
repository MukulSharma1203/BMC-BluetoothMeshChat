package com.example.bl_mesh_msg.utils;

import java.util.UUID;

public class Constants {

    // Custom UUIDs for BLE Mesh Service and Message Characteristic
    public static final UUID SERVICE_UUID = UUID.fromString("00001805-0000-1000-8000-00805f9b34fb");
    public static final UUID CHARACTERISTIC_UUID = UUID.fromString("00002a2b-0000-1000-8000-00805f9b34fb");

    // Mesh Default Configuration
    public static final int DEFAULT_TTL = 5;

    // Intent / Action Constants
    public static final String ACTION_MESSAGE_RECEIVED = "com.example.bl_mesh_msg.MESSAGE_RECEIVED";
    public static final String EXTRA_MESSAGE_TEXT = "extra_message_text";
}