package com.example.bl_mesh_msg;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bl_mesh_msg.mesh.MeshManager;
import com.example.bl_mesh_msg.model.Message;
import com.example.bl_mesh_msg.model.MessageAdapter;
import com.example.bl_mesh_msg.utils.Constants;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements MeshManager.MeshEventListener {

    private static final int PERMISSION_REQUEST_CODE = 101;

    private EditText etMyDeviceId, etTargetId, etMessageText;
    private Button btnStartMesh, btnSend;
    private TextView tvPeerCount, tvLogs;
    private ScrollView scrollLogs;
    private RecyclerView rvMessages;

    private MessageAdapter messageAdapter;
    private MeshManager meshManager;
    private final Set<String> discoveredPeers = new HashSet<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        checkAndRequestPermissions();
    }

    private void initViews() {
        etMyDeviceId = findViewById(R.id.etMyDeviceId);
        etTargetId = findViewById(R.id.etTargetId);
        etMessageText = findViewById(R.id.etMessageText);
        btnStartMesh = findViewById(R.id.btnStartMesh);
        btnSend = findViewById(R.id.btnSend);
        tvPeerCount = findViewById(R.id.tvPeerCount);
        tvLogs = findViewById(R.id.tvLogs);
        scrollLogs = findViewById(R.id.scrollLogs);
        rvMessages = findViewById(R.id.rvMessages);

        messageAdapter = new MessageAdapter();
        rvMessages.setLayoutManager(new LinearLayoutManager(this));
        rvMessages.setAdapter(messageAdapter);

        btnStartMesh.setOnClickListener(v -> {
            String myId = etMyDeviceId.getText().toString().trim().toUpperCase();
            if (myId.isEmpty()) {
                Toast.makeText(this, "Enter a Device ID (e.g. A, B, C)", Toast.LENGTH_SHORT).show();
                return;
            }

            if (meshManager == null) {
                meshManager = new MeshManager(this, myId);
                meshManager.setListener(this);
                meshManager.startMesh();
                btnStartMesh.setEnabled(false);
                etMyDeviceId.setEnabled(false);
                Toast.makeText(this, "Mesh Network active as Device " + myId, Toast.LENGTH_SHORT).show();
            }
        });

        btnSend.setOnClickListener(v -> {
            if (meshManager == null) {
                Toast.makeText(this, "Start the Mesh network first!", Toast.LENGTH_SHORT).show();
                return;
            }

            String targetId = etTargetId.getText().toString().trim().toUpperCase();
            String text = etMessageText.getText().toString().trim();
            String myId = etMyDeviceId.getText().toString().trim().toUpperCase();

            if (targetId.isEmpty() || text.isEmpty()) {
                Toast.makeText(this, "Enter Target ID and Message Text", Toast.LENGTH_SHORT).show();
                return;
            }

            Message message = new Message(myId, targetId, text, Constants.DEFAULT_TTL);
            messageAdapter.addMessage(message);
            meshManager.sendMessage(message);
            etMessageText.setText("");
        });
    }

    // --- MeshManager Callbacks ---

    @Override
    public void onMessageReceived(Message message) {
        runOnUiThread(() -> {
            messageAdapter.addMessage(message);
            rvMessages.smoothScrollToPosition(messageAdapter.getItemCount() - 1);
        });
    }

    @Override
    public void onPeerDiscovered(String deviceAddress) {
        runOnUiThread(() -> {
            discoveredPeers.add(deviceAddress);
            tvPeerCount.setText("Discovered Peers: " + discoveredPeers.size());
        });
    }

    @Override
    public void onLogUpdate(String log) {
        runOnUiThread(() -> {
            tvLogs.append("\n" + log);
            scrollLogs.post(() -> scrollLogs.fullScroll(ScrollView.FOCUS_DOWN));
        });
    }

    // --- Permissions Management ---

    private void checkAndRequestPermissions() {
        List<String> requiredPermissions = new ArrayList<>();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                requiredPermissions.add(Manifest.permission.BLUETOOTH_SCAN);
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADVERTISE) != PackageManager.PERMISSION_GRANTED) {
                requiredPermissions.add(Manifest.permission.BLUETOOTH_ADVERTISE);
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                requiredPermissions.add(Manifest.permission.BLUETOOTH_CONNECT);
            }
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requiredPermissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }

        if (!requiredPermissions.isEmpty()) {
            ActivityCompat.requestPermissions(
                    this,
                    requiredPermissions.toArray(new String[0]),
                    PERMISSION_REQUEST_CODE
            );
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (meshManager != null) {
            meshManager.stopMesh();
        }
    }
}