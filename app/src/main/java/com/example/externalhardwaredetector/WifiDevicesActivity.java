package com.example.externalhardwaredetector;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class WifiDevicesActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 101;
    private TextView wifiDetailsTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_devices);

        wifiDetailsTextView = findViewById(R.id.wifiDetailsTextView);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSION_REQUEST_CODE);
        } else {
            displayWifiInfo();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE &&
                grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            displayWifiInfo();
        } else {
            Toast.makeText(this, "Permission denied. Cannot access Wi-Fi info.", Toast.LENGTH_SHORT).show();
        }
    }

    private void displayWifiInfo() {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        if (wifiManager != null && wifiManager.isWifiEnabled()) {
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();

            if (wifiInfo != null && wifiInfo.getNetworkId() != -1) {
                String ssid = wifiInfo.getSSID().replace("\"", "");
                String bssid = wifiInfo.getBSSID();
                int ip = wifiInfo.getIpAddress();
                int linkSpeed = wifiInfo.getLinkSpeed();
                int rssi = wifiInfo.getRssi();

                String ipString = (ip & 0xFF) + "." +
                        ((ip >> 8) & 0xFF) + "." +
                        ((ip >> 16) & 0xFF) + "." +
                        ((ip >> 24) & 0xFF);

                String info = "Connected to:\n\n"
                        + "SSID: " + ssid + "\n"
                        + "BSSID: " + bssid + "\n"
                        + "IP Address: " + ipString + "\n"
                        + "Link Speed: " + linkSpeed + " Mbps\n"
                        + "Signal Strength: " + rssi + " dBm";

                wifiDetailsTextView.setText(info);
            } else {
                wifiDetailsTextView.setText("Not connected to any Wi-Fi.");
            }
        } else {
            wifiDetailsTextView.setText("Wi-Fi is disabled.");
        }
    }
}
