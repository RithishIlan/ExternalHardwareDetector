package com.example.externalhardwaredetector;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;
import java.util.ArrayList;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class BluetoothDevicesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private DeviceAdapter adapter;
    private List<DeviceInfo> connectedDevicesList = new ArrayList<>();
    private List<DeviceInfo> pairedDevicesList = new ArrayList<>();
    private BluetoothAdapter bluetoothAdapter;

    private final BroadcastReceiver bluetoothReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            if (device == null) return;

            // Check for the required permission for Bluetooth Connect
            if (ActivityCompat.checkSelfPermission(BluetoothDevicesActivity.this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                // Request the necessary permissions here
                ActivityCompat.requestPermissions(BluetoothDevicesActivity.this, new String[]{android.Manifest.permission.BLUETOOTH_CONNECT}, 1);
                return;
            }

            String deviceName = device.getName();
            String deviceAddress = device.getAddress();
            String currentTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());

            if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
                DeviceInfo deviceInfo = new DeviceInfo(deviceName, deviceAddress, "Connected", currentTime, "");
                // Add to top of connected devices list
                connectedDevicesList.add(0, deviceInfo);
                // Remove from paired devices list if it exists there
                removeDeviceFromList(pairedDevicesList, deviceAddress);
                adapter.notifyDataSetChanged();
            } else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                for (DeviceInfo info : connectedDevicesList) {
                    if (info.getAddress().equals(deviceAddress)) {
                        info.setStatus("Disconnected");
                        info.setDisconnectedTime(currentTime);
                        connectedDevicesList.remove(info);
                        pairedDevicesList.add(info);
                        break;
                    }
                }
                adapter.notifyDataSetChanged();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_devices);

        recyclerView = findViewById(R.id.recyclerView);
        adapter = new DeviceAdapter(connectedDevicesList, pairedDevicesList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            // Handle the case where Bluetooth is not supported on the device
            Toast.makeText(this, "Bluetooth not supported", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!bluetoothAdapter.isEnabled()) {
            // You can prompt the user to enable Bluetooth if it's disabled
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 1);
        }

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            // Request Bluetooth permissions if not already granted
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.BLUETOOTH_CONNECT}, 1);
            return;
        }

        // Get paired devices and update the list
        for (BluetoothDevice device : bluetoothAdapter.getBondedDevices()) {
            pairedDevicesList.add(new DeviceInfo(device.getName(), device.getAddress(), "Paired", "", ""));
        }
        adapter.notifyDataSetChanged();

        // Register for Bluetooth device connection and disconnection events
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        registerReceiver(bluetoothReceiver, filter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(bluetoothReceiver);
    }

    // Helper method to remove device from the list by address
    private void removeDeviceFromList(List<DeviceInfo> deviceList, String deviceAddress) {
        for (DeviceInfo info : deviceList) {
            if (info.getAddress().equals(deviceAddress)) {
                deviceList.remove(info);
                break;
            }
        }
    }

    // DeviceInfo class to hold device details
    public static class DeviceInfo {
        private String name;
        private String address;
        private String status;
        private String connectedTime;
        private String disconnectedTime;

        public DeviceInfo(String name, String address, String status, String connectedTime, String disconnectedTime) {
            this.name = name;
            this.address = address;
            this.status = status;
            this.connectedTime = connectedTime;
            this.disconnectedTime = disconnectedTime;
        }

        // Getters and setters
        public String getName() { return name; }
        public String getAddress() { return address; }
        public String getStatus() { return status; }
        public String getConnectedTime() { return connectedTime; }
        public String getDisconnectedTime() { return disconnectedTime; }
        public void setStatus(String status) { this.status = status; }
        public void setDisconnectedTime(String disconnectedTime) { this.disconnectedTime = disconnectedTime; }
    }

    // RecyclerView Adapter
    public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder> {

        private List<DeviceInfo> connectedDevices;
        private List<DeviceInfo> pairedDevices;

        public DeviceAdapter(List<DeviceInfo> connectedDevices, List<DeviceInfo> pairedDevices) {
            this.connectedDevices = connectedDevices;
            this.pairedDevices = pairedDevices;
        }

        @Override
        public DeviceViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.device_card, parent, false);
            return new DeviceViewHolder(view);
        }

        @Override
        public void onBindViewHolder(DeviceViewHolder holder, int position) {
            DeviceInfo device;
            // If position is less than the size of connected devices list, it's a connected device
            if (position < connectedDevices.size()) {
                device = connectedDevices.get(position);
                holder.cardView.setCardBackgroundColor(getResources().getColor(R.color.connected_device)); // Highlight connected devices
            } else {
                // Otherwise, it's a paired device
                device = pairedDevices.get(position - connectedDevices.size());
                holder.cardView.setCardBackgroundColor(getResources().getColor(R.color.paired_device)); // Default color for paired devices
            }

            holder.name.setText("Name: " + device.getName());
            holder.address.setText("MAC: " + device.getAddress());
            holder.status.setText("Status: " + device.getStatus());
            holder.connectedTime.setText("Connected: " + device.getConnectedTime());
            holder.disconnectedTime.setText("Disconnected: " + device.getDisconnectedTime());
        }

        @Override
        public int getItemCount() {
            return connectedDevices.size() + pairedDevices.size();  // Total devices to show
        }

        class DeviceViewHolder extends RecyclerView.ViewHolder {
            TextView name, address, status, connectedTime, disconnectedTime;
            CardView cardView;

            public DeviceViewHolder(View itemView) {
                super(itemView);
                name = itemView.findViewById(R.id.deviceName);
                address = itemView.findViewById(R.id.deviceAddress);
                status = itemView.findViewById(R.id.deviceStatus);
                connectedTime = itemView.findViewById(R.id.deviceConnectedTime);
                disconnectedTime = itemView.findViewById(R.id.deviceDisconnectedTime);
                cardView = itemView.findViewById(R.id.deviceCardView);
            }
        }
    }
}
