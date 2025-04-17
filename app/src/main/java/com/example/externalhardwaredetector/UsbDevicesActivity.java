package com.example.externalhardwaredetector;

import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.ArrayList;
import java.util.List;

public class UsbDevicesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private DeviceAdapter adapter;
    private List<DeviceInfo> usbDevicesList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usb_devices); // Create this layout separately

        recyclerView = findViewById(R.id.recyclerView);
        adapter = new DeviceAdapter(usbDevicesList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        listUsbDevices();
    }

    private void listUsbDevices() {
        UsbManager usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();
        String currentTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());

        for (UsbDevice device : deviceList.values()) {
            String name = device.getDeviceName();
            String id = device.getDeviceId() + "";
            String vendor = "Vendor ID: " + device.getVendorId();
            String product = "Product ID: " + device.getProductId();
            String details = vendor + ", " + product;

            usbDevicesList.add(new DeviceInfo(name, id, "Connected", currentTime, "", details));
        }

        adapter.notifyDataSetChanged();
    }

    public static class DeviceInfo {
        private String name;
        private String address;
        private String status;
        private String connectedTime;
        private String disconnectedTime;
        private String extraInfo;

        public DeviceInfo(String name, String address, String status, String connectedTime, String disconnectedTime, String extraInfo) {
            this.name = name;
            this.address = address;
            this.status = status;
            this.connectedTime = connectedTime;
            this.disconnectedTime = disconnectedTime;
            this.extraInfo = extraInfo;
        }

        public String getName() { return name; }
        public String getAddress() { return address; }
        public String getStatus() { return status; }
        public String getConnectedTime() { return connectedTime; }
        public String getDisconnectedTime() { return disconnectedTime; }
        public String getExtraInfo() { return extraInfo; }
    }

    public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder> {

        private List<DeviceInfo> deviceList;

        public DeviceAdapter(List<DeviceInfo> deviceList) {
            this.deviceList = deviceList;
        }

        @Override
        public DeviceViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.device_card, parent, false);
            return new DeviceViewHolder(view);
        }

        @Override
        public void onBindViewHolder(DeviceViewHolder holder, int position) {
            DeviceInfo device = deviceList.get(position);
            holder.cardView.setCardBackgroundColor(getResources().getColor(R.color.connected_device));
            holder.name.setText("Name: " + device.getName());
            holder.address.setText("ID: " + device.getAddress());
            holder.status.setText("Status: " + device.getStatus());
            holder.connectedTime.setText("Connected: " + device.getConnectedTime());
            holder.disconnectedTime.setText("Info: " + device.getExtraInfo());
        }

        @Override
        public int getItemCount() {
            return deviceList.size();
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
