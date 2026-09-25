package com.example.bl_mesh_msg.ble;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.util.Log;

import com.example.bl_mesh_msg.utils.Constants;

import java.nio.charset.StandardCharsets;

public class GattClient {

    private static final String TAG = "GattClient";

    public interface SendCallback {
        void onSuccess();
        void onFailure(String error);
    }

    @SuppressLint("MissingPermission")
    public void sendMessage(Context context, BluetoothDevice device, String jsonMessage, SendCallback callback) {
        if (device == null) {
            if (callback != null) callback.onFailure("Target device is null.");
            return;
        }

        device.connectGatt(context, false, new BluetoothGattCallback() {
            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                super.onConnectionStateChange(gatt, status, newState);
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    Log.d(TAG, "Connected to GATT server on " + device.getAddress() + ". Discovering services...");
                    gatt.discoverServices();
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    Log.d(TAG, "Disconnected from GATT server on " + device.getAddress());
                    gatt.close();
                }
            }

            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                super.onServicesDiscovered(gatt, status);
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    BluetoothGattService service = gatt.getService(Constants.SERVICE_UUID);
                    if (service != null) {
                        BluetoothGattCharacteristic characteristic = service.getCharacteristic(Constants.CHARACTERISTIC_UUID);
                        if (characteristic != null) {
                            characteristic.setValue(jsonMessage.getBytes(StandardCharsets.UTF_8));
                            boolean success = gatt.writeCharacteristic(characteristic);
                            Log.d(TAG, "Writing message to " + device.getAddress() + " initiated: " + success);
                        } else {
                            if (callback != null) callback.onFailure("Characteristic not found.");
                            gatt.disconnect();
                        }
                    } else {
                        if (callback != null) callback.onFailure("Service not found.");
                        gatt.disconnect();
                    }
                } else {
                    if (callback != null) callback.onFailure("Service discovery failed.");
                    gatt.disconnect();
                }
            }

            @Override
            public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                super.onCharacteristicWrite(gatt, characteristic, status);
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    Log.d(TAG, "Message write successful to " + device.getAddress());
                    if (callback != null) callback.onSuccess();
                } else {
                    Log.e(TAG, "Message write failed with status: " + status);
                    if (callback != null) callback.onFailure("Write failed status: " + status);
                }
                gatt.disconnect();
            }
        });
    }
}