package com.example.magulo.scarf_wearable;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothAdapter.LeScanCallback;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Button;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.client.ClientProtocolException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.security.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class TempSetter extends Activity {
    private Button next;
    long curTime;
    private ImageView tempView;
    private SharedPreferences shared_pref;
    int tempInput;
    HashMap<Long, Float> time_temp = new HashMap<Long, Float>();
    Handler handler;
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
                //              writeLine("Connected!");
                // Discover services.
                if (!gatt.discoverServices()) {
                    //                writeLine("Failed to start discovering services!");
                }
            } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                //            writeLine("Disconnected!");
            } else {
                //             writeLine("Connection state changed.  New state: " + newState);
            }
        }

        // Called when services have been discovered on the remote device.
        // It seems to be necessary to wait for this discovery to occur before
        // manipulating any services or characteristics.
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                //               writeLine("Service discovery completed!");
            } else {
//                writeLine("Service discovery failed with status: " + status);
            }
            // Save reference to each characteristic.
            tx = gatt.getService(UART_UUID).getCharacteristic(TX_UUID);
            rx = gatt.getService(UART_UUID).getCharacteristic(RX_UUID);
            // Setup notifications on RX characteristic changes (i.e. data received).
            // First call setCharacteristicNotification to enable notification.
            if (!gatt.setCharacteristicNotification(rx, true)) {
                Log.v("record", "Couldn't set notifications for RX characteristic!");
            }
            // Next update the RX characteristic's client descriptor to enable notifications.
            if (rx.getDescriptor(CLIENT_UUID) != null) {
                BluetoothGattDescriptor desc = rx.getDescriptor(CLIENT_UUID);
                desc.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                if (!gatt.writeDescriptor(desc)) {
//                    writeLine("Couldn't write RX client descriptor value!");
                }
            } else {
//                writeLine("Couldn't get RX client descriptor!");
            }
        }



//            @Override
//            protected void onPostExecute(String result) {
//                super.onPostExecute(result);
                //Do anything with response..
//            }

        // Called when a remote characteristic changes (like the RX characteristic).
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            int output = characteristic.getStringValue(0).charAt(0);

            temp_view = (TextView) findViewById(R.id.temp_view);
            float x = (float) output;
            float voltage = x * 5/1023;
            float temp = voltage * 100;
            curTime = System.currentTimeMillis();
            time_temp.put(curTime, temp);
            String form_hash="1WEUHwRj8KTuTHdiwVhLLVL8idxSPbFU8kVVffkri7P4";
            String data="entry.2134679435=" + "hi";
         //   String url = "https://docs.google.com/forms/d/" + form_hash + "/formResponse?ifq&" + data;
            String auto_submit = "&submit=Submit"; //add this to the end to make it autosubmit
//        String url = "https://docs.google.com/forms/d/" + form_hash + "/formResponse?&" + data + auto_submit;
            String temp_string = String.format("%.2f", temp);
            String url = "https://docs.google.com/forms/d/" + form_hash + "/formResponse?&entry.2134679435=" + temp_string + "&submit=Submit";
//            1WEUHwRj8KTuTHdiwVhLLVL8idxSPbFU8kVVffkri7P4/formResponse?&entry.2134679435=" + temp_feedback + "&submit=Submit";
            new RequestTask().execute(url);
            // this stores the current time and temperature for future access
//            writeLine("Received: " + temp);
            writeLine(temp_string + "C", temp);
            call(temp);
//            temp_view.setText("C");
        }
    };

    // BTLE device scanning callback.
    private LeScanCallback scanCallback = new LeScanCallback() {
        // Called when a device is found.
        @Override
        public void onLeScan(BluetoothDevice bluetoothDevice, int i, byte[] bytes) {
  //          writeLine("Found device: " + bluetoothDevice.getAddress());
            // Check if the device has the UART service.
            if (parseUUIDs(bytes).contains(UART_UUID)) {
                // Found a device, stop the scan.
                adapter.stopLeScan(scanCallback);
//                writeLine("Found UART service!");
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
//        tempInput = rand.nextInt(200) + 1;
//        temp_feedback = Integer.toString(tempInput);
        tempView = (ImageView) findViewById(R.id.tempView);
        temp_view = (TextView) findViewById(R.id.temp_view);
//        tempInput = (Integer) 200;

        handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //thing to do every 2000 milliseconds (aka every 2 seconds)
                sendClick();

          //      new RequestTask().execute(url);
                handler.postDelayed(this, 2000);
            }
            // never reaches this point, so this number doesn't matter
        }, 3000);
      //  temp_view = (TextView) findViewById(R.id.temp_view);
      //  temp_view.setText(temp_feedback);
//        setContentView(R.layout.activity_temp_setter);
        next = (Button) findViewById(R.id.next);
        next.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent_1 = new Intent(TempSetter.this, Contacts.class);
                startActivity(intent_1);
            }
        });

    }
    private void call(float curTemp) {
        if (curTemp >= 28 || curTemp <= 29) {
            shared_pref = getApplicationContext().getSharedPreferences(
                    "phone_storage", Context.MODE_PRIVATE
            );
            String call = shared_pref.getString("phone_number", "1235550199");
            Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + call));//area code and number, no spaces
            startActivity(intent);
        }
    }

    private void writeLine(final CharSequence text, final float currentTemp) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tempView.animate().scaleY((currentTemp-20f)/20f).setDuration(1500);
                temp_view.setText(text);
            }        });


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
//        writeLine("Scanning for devices...");
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
    class RequestTask extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... uri) {
            HttpClient httpclient = new DefaultHttpClient();
            HttpResponse response;
            String responseString = null;
            try {
                response = httpclient.execute(new HttpGet(uri[0]));
                StatusLine statusLine = response.getStatusLine();
                if(statusLine.getStatusCode() == HttpStatus.SC_OK){
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    response.getEntity().writeTo(out);
                    responseString = out.toString();
                    out.close();
                } else{
                    //Closes the connection.
                    response.getEntity().getContent().close();
                    throw new IOException(statusLine.getReasonPhrase());
                }
            } catch (ClientProtocolException e) {
                //TODO Handle problems..
            } catch (IOException e) {
                //TODO Handle problems..
            }
            return responseString;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            //Do anything with response..
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
    //    writeLine("Sent: " + message);

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
