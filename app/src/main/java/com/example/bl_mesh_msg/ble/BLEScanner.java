package com.example.bl_mesh_msg.ble;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.os.ParcelUuid;
import android.util.Log;

import com.example.bl_mesh_msg.utils.Constants;

import java.util.ArrayList;
import java.util.List;

public class BLEScanner {

    private static final String TAG = "BLEScanner";

    public interface ScanListener {
        void onDeviceFound(BluetoothDevice device);
    }

    private BluetoothLeScanner scanner;
    private ScanCallback scanCallback;
    private boolean isScanning = false;

    public BLEScanner() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter != null) {
            scanner = bluetoothAdapter.getBluetoothLeScanner();
        }
    }

    @SuppressLint("MissingPermission")
    public void startScan(ScanListener listener) {
        if (scanner == null || isScanning) return;

        List<ScanFilter> filters = new ArrayList<>();
        ScanFilter filter = new ScanFilter.Builder()
                .setServiceUuid(new ParcelUuid(Constants.SERVICE_UUID))
                .build();
        filters.add(filter);

        ScanSettings settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build();

        scanCallback = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                super.onScanResult(callbackType, result);
                if (result != null && result.getDevice() != null) {
                    Log.d(TAG, "Found BLE Peer: " + result.getDevice().getAddress());
                    if (listener != null) {
                        listener.onDeviceFound(result.getDevice());
                    }
                }
            }

            @Override
            public void onScanFailed(int errorCode) {
                super.onScanFailed(errorCode);
                Log.e(TAG, "BLE Scan failed with error: " + errorCode);
            }
        };

        scanner.startScan(filters, settings, scanCallback);
        isScanning = true;
        Log.d(TAG, "BLE Scan started.");
    }

    @SuppressLint("MissingPermission")
    public void stopScan() {
        if (scanner != null && scanCallback != null && isScanning) {
            scanner.stopScan(scanCallback);
            isScanning = false;
            Log.d(TAG, "BLE Scan stopped.");
        }
    }

    public boolean isScanning() {
        return isScanning;
    }
}