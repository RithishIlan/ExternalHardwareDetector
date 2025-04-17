package com.example.externalhardwaredetector;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class DashboardActivity extends AppCompatActivity {

    Button bluetoothBtn, usbBtn, wifiBtn, logoutBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        bluetoothBtn = findViewById(R.id.bluetoothBtn);
        usbBtn = findViewById(R.id.usbBtn);
        wifiBtn = findViewById(R.id.wifiBtn);
        logoutBtn = findViewById(R.id.logoutBtn);

        bluetoothBtn.setOnClickListener(v -> startActivity(new Intent(this, BluetoothDevicesActivity.class)));
        usbBtn.setOnClickListener(v -> startActivity(new Intent(this, UsbDevicesActivity.class)));
        wifiBtn.setOnClickListener(v -> startActivity(new Intent(this, WifiDevicesActivity.class)));

        logoutBtn.setOnClickListener(v -> {
            SharedPreferences.Editor editor = getSharedPreferences("app_prefs", MODE_PRIVATE).edit();
            editor.putBoolean("is_logged_in", false).apply();
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });
    }
}
