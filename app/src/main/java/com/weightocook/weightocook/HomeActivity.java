package com.weightocook.weightocook;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

import static java.lang.Integer.parseInt;

public class HomeActivity extends AppCompatActivity {

    /** for recipeSearch method: "For the next activity to query the extra data, you should define the key for your intent's extra using a public constant" */
    public final static String SEARCH_RESULTS = "com.weightocook.weightocook.SEARCH_RESULTS";
    private final static int REQUEST_ENABLE_BT = 1;
    TextView btStatusDisplay;
    BluetoothDevice mmDevice;
    BluetoothSocket mmSocket;
    OutputStream mmOutputStream;
    InputStream mmInputStream;
    Thread workerThread;
    byte[] readBuffer;
    int readBufferPosition;
    volatile boolean stopWorker;
    int currentWeight = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        btStatusDisplay = (TextView) findViewById(R.id.label);
    }

    @Override
    protected void onStart() {
        super.onStart();
        //findBluetooth();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home, menu);
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
    /** Called when the user taps the Search button */
    public void recipeSearch(View view){
        Intent intent = new Intent(this, SearchResultsActivity.class);
        EditText editText = (EditText) findViewById(R.id.editSearch); //gets the EditText element from the search box
        String searchResultsStr = editText.getText().toString(); // turn the search results into a string for use in next method
        intent.putExtra(SEARCH_RESULTS, searchResultsStr); // this is packaging the contents of editText for use in the new activity
        startActivity(intent);
    }
    /** Called when the user taps the Dinner button */
    public void selectDinner(View view){
        Intent intent = new Intent(this, DinnerCategoryActivity.class);
        startActivity(intent);
    }
    /** Called when the user taps the Desserts button */
    public void selectDesserts(View view){
        Intent intent = new Intent(this, DessertsCategoryActivity.class);
        startActivity(intent);
    }

/** BLUETOOTH METHODS -------------------------------------------------------------------------*/

    /**
     * Checks whether Bluetooth is supported and enabled - requests to enable if not
     */
    public void findBluetooth() {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
            btStatusDisplay.setText("Device does not support Bluetooth");
        }
        btStatusDisplay.setText("Trying to connect...");

        /** Pops up request to enable if found not enabled */
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        /** Get reference to scale as Bluetooth device "mmDevice" */
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                if (device.getName().equals("BLUE")) {
                    mmDevice = device;
                    btStatusDisplay.setText("Bluetooth device found");
                    break;
                }
            }
        }
    }

    /**
     * Opens connection to mmDevice
     */
    public void openBluetooth(View view) throws IOException {
        if(mmDevice == null){
            btStatusDisplay.setText("mmDevice is null");
        }
        else {
            UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb"); //Standard SerialPortService ID
            mmSocket = mmDevice.createRfcommSocketToServiceRecord(uuid);
            mmSocket.connect();
            mmOutputStream = mmSocket.getOutputStream();
            mmInputStream = mmSocket.getInputStream();

            beginListenForData();

            btStatusDisplay.setText("Bluetooth Connection Opened");
        }
    }


    public void beginListenForData() {
        final Handler handler = new Handler();
        final byte delimiter = 35; //This is the ASCII code for #

        stopWorker = false;
        readBufferPosition = 0;
        readBuffer = new byte[1024];
        // readBuffer size above should match longest message size we might receive
        workerThread = new Thread(new Runnable() {
            public void run() {
                while (!Thread.currentThread().isInterrupted() && !stopWorker) {
                    try {
                        int bytesAvailable = mmInputStream.available();
                        if (bytesAvailable > 0) {
                            byte[] packetBytes = new byte[bytesAvailable];
                            mmInputStream.read(packetBytes);
                            for (int i = 0; i < bytesAvailable; i++) {
                                byte b = packetBytes[i];
                                if (b == delimiter) {
                                    byte[] encodedBytes = new byte[readBufferPosition];
                                    System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                    final String data = new String(encodedBytes, "US-ASCII");
                                    readBufferPosition = 0;

                                    handler.post(new Runnable() {
                                        public void run() {
                                            // handles "data"
                                            if (data.charAt(0) == '+'){
                                                // it's a weight value
                                                //rawDataDisplay.setText(data);
                                                Integer intWeight = parseInt(data);
                                                //interpretedDataDisplay.setText(String.valueOf(intWeight));
                                                setCurrentWeight(intWeight);

                                            }
                                            else { // it's a command
                                                switch (data) {
                                                    case "@ACKON": // Acknowledge @MONON# successfully turned on weight monitoring
                                                        // TODO: do stuff
                                                        //rawDataDisplay.setText(data);
                                                        break;
                                                    case "@ACKOFF": // Acknowledge @MONOFF# successfully turned off weight monitoring
                                                        // TODO: do stuff
                                                        //rawDataDisplay.setText(data);
                                                        break;
                                                    case "@ACKPWRDN": // Acknowledge @PWRDN# successfully turned off power
                                                        // TODO: do stuff
                                                        //rawDataDisplay.setText(data);
                                                        break;
                                                    case "@ACKPWRUP": // Acknowledge @PWRUP# successfully powered on scale
                                                        // TODO: do stuff
                                                        //rawDataDisplay.setText(data);
                                                        break;
                                                    case "@RESET": // Scale just powered up; connect Bluetooth
                                                        // TODO: do stuff
                                                        //rawDataDisplay.setText(data);
                                                        findBluetooth();
                                                        break;
                                                }
                                            }
                                        }
                                    });
                                } else {
                                    readBuffer[readBufferPosition++] = b;
                                }
                            }
                        }
                    } catch (IOException ex) {
                        stopWorker = true;
                    }
                }
            }
        });

        workerThread.start();
    }


    public void sendCommand(String command) throws IOException {
        mmOutputStream.write(command.getBytes());
        btStatusDisplay.setText("Command " + command + " sent.");
    }

    public void closeBluetooth(View view) throws IOException {
        stopWorker = true;
        mmOutputStream.close();
        mmInputStream.close();
        mmSocket.close();
        btStatusDisplay.setText("Bluetooth connection closed.");
    }

    public void setCurrentWeight(int integer){
        currentWeight = integer;
        // TODO: Make sure other methods set this back to zero when done.
    }

    public void monitorWeightOn(View view) {
        try {
            sendCommand("@MONON#");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void monitorWeightOff(View view) {
        try {
            sendCommand("@MONOFF#");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void powerOn(View view) {
        try {
            sendCommand("@PWRUP#");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void powerOff(View view) {
        try {
            sendCommand("@PWRDN#");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
