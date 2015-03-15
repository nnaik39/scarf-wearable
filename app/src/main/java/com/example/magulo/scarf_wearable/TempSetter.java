package com.example.magulo.scarf_wearable;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothAdapter.LeScanCallback;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Button;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class TempSetter extends Activity {
    private Button next;
    private ImageView tempView;
    int tempInput;
    String temp_feedback;
    private TextView temp_view;
    // UUIDs for UAT service and associated characteristics.
    public static UUID UART_UUID = UUID.fromString("6E400001-B5A3-F393-E0A9-E50E24DCCA9E");
    public static UUID TX_UUID = UUID.fromString("6E400002-B5A3-F393-E0A9-E50E24DCCA9E");
    public static UUID RX_UUID = UUID.fromString("6E400003-B5A3-F393-E0A9-E50E24DCCA9E");
    // UUID for the BTLE client characteristic which is necessary for notifications.
    public static UUID CLIENT_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    // UI elements
    private TextView messages;
    private EditText input;

    // BTLE state
    private BluetoothAdapter adapter;
    private BluetoothGatt gatt;
    private BluetoothGattCharacteristic tx;
    private BluetoothGattCharacteristic rx;

    // Main BTLE device callback where much of the logic occurs.
    private BluetoothGattCallback callback = new BluetoothGattCallback() {
        // Called whenever the device connection state changes, i.e. from disconnected to connected.
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            if (newState == BluetoothGatt.STATE_CONNECTED) {
                writeLine("Connected!");
                // Discover services.
                if (!gatt.discoverServices()) {
                    writeLine("Failed to start discovering services!");
                }
            }
            else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                writeLine("Disconnected!");
            }
            else {
                writeLine("Connection state changed.  New state: " + newState);
            }
        }

        // Called when services have been discovered on the remote device.
        // It seems to be necessary to wait for this discovery to occur before
        // manipulating any services or characteristics.
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                writeLine("Service discovery completed!");
            }
            else {
                writeLine("Service discovery failed with status: " + status);
            }
            // Save reference to each characteristic.
            tx = gatt.getService(UART_UUID).getCharacteristic(TX_UUID);
            rx = gatt.getService(UART_UUID).getCharacteristic(RX_UUID);
            // Setup notifications on RX characteristic changes (i.e. data received).
            // First call setCharacteristicNotification to enable notification.
            if (!gatt.setCharacteristicNotification(rx, true)) {
                writeLine("Couldn't set notifications for RX characteristic!");
            }
            // Next update the RX characteristic's client descriptor to enable notifications.
            if (rx.getDescriptor(CLIENT_UUID) != null) {
                BluetoothGattDescriptor desc = rx.getDescriptor(CLIENT_UUID);
                desc.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                if (!gatt.writeDescriptor(desc)) {
                    writeLine("Couldn't write RX client descriptor value!");
                }
            }
            else {
                writeLine("Couldn't get RX client descriptor!");
            }
        }

        // Called when a remote characteristic changes (like the RX characteristic).
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            int output = characteristic.getStringValue(0).charAt(0);
            float x = (float) output;
            float voltage = x * 5/1023;
            float temp = voltage * 100;
            writeLine("Received: " + temp);
            String temp_string = String.format("%.2f", temp);
            temp_view.setText(temp_string + "C");
//            temp_view.setText("C");
        }
    };

    // BTLE device scanning callback.
    private LeScanCallback scanCallback = new LeScanCallback() {
        // Called when a device is found.
        @Override
        public void onLeScan(BluetoothDevice bluetoothDevice, int i, byte[] bytes) {
            writeLine("Found device: " + bluetoothDevice.getAddress());
            // Check if the device has the UART service.
            if (parseUUIDs(bytes).contains(UART_UUID)) {
                // Found a device, stop the scan.
                adapter.stopLeScan(scanCallback);
                writeLine("Found UART service!");
                // Connect to the device.
                // Control flow will now go to the callback functions when BTLE events occur.
                gatt = bluetoothDevice.connectGatt(getApplicationContext(), false, callback);
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temp_setter);
        messages = (TextView) findViewById(R.id.messages);
        adapter = BluetoothAdapter.getDefaultAdapter();
        Random rand = new Random();
        tempInput = rand.nextInt(200) + 1;
        tempView = (ImageView) findViewById(R.id.tempView);
        temp_view = (TextView) findViewById(R.id.temp_view);
//        tempInput = (Integer) 200;
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //thing to do every 2000 milliseconds (aka every 2 seconds)
                sendClick();
                handler.postDelayed(this, 2000);
            }
            // never reaches this point, so this number doesn't matter
        }, 3000);
        if (100 < tempInput) {
            tempView.animate().scaleYBy(1.5f).setDuration(5000); }
        temp_view = (TextView) findViewById(R.id.temp_view);
        temp_feedback = Integer.toString(tempInput);
        temp_view.setText(temp_feedback);
//        setContentView(R.layout.activity_temp_setter);
        next = (Button) findViewById(R.id.next);
        next.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent_1 = new Intent(TempSetter.this, Contacts.class);
                startActivity(intent_1);
            }
        });


    }
    private void writeLine(final CharSequence text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                messages.append(text);
                messages.append("\n");
            }
        });
    }
    private List<UUID> parseUUIDs(final byte[] advertisedData) {
        List<UUID> uuids = new ArrayList<UUID>();

        int offset = 0;
        while (offset < (advertisedData.length - 2)) {
            int len = advertisedData[offset++];
            if (len == 0)
                break;

            int type = advertisedData[offset++];
            switch (type) {
                case 0x02: // Partial list of 16-bit UUIDs
                case 0x03: // Complete list of 16-bit UUIDs
                    while (len > 1) {
                        int uuid16 = advertisedData[offset++];
                        uuid16 += (advertisedData[offset++] << 8);
                        len -= 2;
                        uuids.add(UUID.fromString(String.format("%08x-0000-1000-8000-00805f9b34fb", uuid16)));
                    }
                    break;
                case 0x06:// Partial list of 128-bit UUIDs
                case 0x07:// Complete list of 128-bit UUIDs
                    // Loop through the advertised 128-bit UUID's.
                    while (len >= 16) {
                        try {
                            // Wrap the advertised bits and order them.
                            ByteBuffer buffer = ByteBuffer.wrap(advertisedData, offset++, 16).order(ByteOrder.LITTLE_ENDIAN);
                            long mostSignificantBit = buffer.getLong();
                            long leastSignificantBit = buffer.getLong();
                            uuids.add(new UUID(leastSignificantBit,
                                    mostSignificantBit));
                        } catch (IndexOutOfBoundsException e) {
                            // Defensive programming.
                            //Log.e(LOG_TAG, e.toString());
                            continue;
                        } finally {
                            // Move the offset to read the next uuid.
                            offset += 15;
                            len -= 16;
                        }
                    }
                    break;
                default:
                    offset += (len - 1);
                    break;
            }
        }
        return uuids;
    }
    @Override
    protected void onResume() {
        super.onResume();
        // Scan for all BTLE devices.
        // The first one with the UART service will be chosen--see the code in the scanCallback.
        writeLine("Scanning for devices...");
        adapter.startLeScan(scanCallback);
    }
    @Override
    protected void onStop() {
        super.onStop();
        if (gatt != null) {
            // For better reliability be careful to disconnect and close the connection.
            gatt.disconnect();
            gatt.close();
            gatt = null;
            tx = null;
            rx = null;
        }
    }
    public void sendClick() {
        String message = "Wassup!";
        //       String message = input.getText().toString();
        if (tx == null || message == null || message.isEmpty()) {
            // Do nothing if there is no device or message to send.
            return;
        }
        // Update TX characteristic value.  Note the setValue overload that takes a byte array must be used.
        tx.setValue(message.getBytes(Charset.forName("UTF-8")));
        gatt.writeCharacteristic(tx);
        writeLine("Sent: " + message);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_temp_setter, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
