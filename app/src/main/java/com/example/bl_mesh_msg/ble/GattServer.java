package com.example.bl_mesh_msg.ble;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.util.Log;

import com.example.bl_mesh_msg.utils.Constants;

import java.nio.charset.StandardCharsets;

public class GattServer {

    private static final String TAG = "GattServer";

    public interface ServerListener {
        void onMessageReceived(String rawMessage, BluetoothDevice senderDevice);
    }

    private BluetoothGattServer gattServer;
    private ServerListener listener;

    @SuppressLint("MissingPermission")
    public void startServer(Context context, ServerListener listener) {
        this.listener = listener;
        BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        if (bluetoothManager == null) return;

        gattServer = bluetoothManager.openGattServer(context, new BluetoothGattServerCallback() {
            @Override
            public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
                super.onConnectionStateChange(device, status, newState);
                Log.d(TAG, "Device connection state changed: " + device.getAddress() + " state: " + newState);
            }

            @Override
            public void onCharacteristicWriteRequest(BluetoothDevice device, int requestId,
                                                     BluetoothGattCharacteristic characteristic,
                                                     boolean preparedWrite, boolean responseNeeded,
                                                     int offset, byte[] value) {
                super.onCharacteristicWriteRequest(device, requestId, characteristic, preparedWrite, responseNeeded, offset, value);

                if (responseNeeded) {
                    gattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, null);
                }

                if (Constants.CHARACTERISTIC_UUID.equals(characteristic.getUuid()) && value != null) {
                    String messageStr = new String(value, StandardCharsets.UTF_8);
                    Log.d(TAG, "GATT Write received from " + device.getAddress() + ": " + messageStr);

                    if (GattServer.this.listener != null) {
                        GattServer.this.listener.onMessageReceived(messageStr, device);
                    }
                }
            }
        });

        if (gattServer == null) {
            Log.e(TAG, "Unable to open GATT Server.");
            return;
        }

        BluetoothGattService service = new BluetoothGattService(
                Constants.SERVICE_UUID,
                BluetoothGattService.SERVICE_TYPE_PRIMARY
        );

        BluetoothGattCharacteristic characteristic = new BluetoothGattCharacteristic(
                Constants.CHARACTERISTIC_UUID,
                BluetoothGattCharacteristic.PROPERTY_WRITE | BluetoothGattCharacteristic.PROPERTY_READ,
                BluetoothGattCharacteristic.PERMISSION_WRITE | BluetoothGattCharacteristic.PERMISSION_READ
        );

        service.addCharacteristic(characteristic);
        gattServer.addService(service);
        Log.d(TAG, "GATT Server started and service added.");
    }

    @SuppressLint("MissingPermission")
    public void stopServer() {
        if (gattServer != null) {
            gattServer.close();
            gattServer = null;
            Log.d(TAG, "GATT Server stopped.");
        }
    }
}