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
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

import static java.lang.Integer.parseInt;

public class BlueberryCobblerBluetoothSteps extends AppCompatActivity {

    final static int FLOUR_GOAL = 125;
    final static int SUGAR_GOAL = 200;
    final static int MILK_GOAL = 245;
    private final static int REQUEST_ENABLE_BT = 1;
    TextView btStatusDisplay;
    TextView miscDisplay;
    TextView flourAddedDisplay;
    TextView sugarAddedDisplay;
    TextView milkAddedDisplay;
    TextView rawDataDisplay;
    BluetoothDevice mmDevice;
    BluetoothSocket mmSocket;
    OutputStream mmOutputStream;
    InputStream mmInputStream;
    Thread workerThread;
    byte[] readBuffer;
    int readBufferPosition;
    volatile boolean stopWorker;
    int currentWeight = 0;
    int bowlWeight = 0;
    boolean nextStepReady = false;
    boolean weighBowl = true;
    int ingredientChosen = 0;

    /** Cups to grams
     * 1 cup self-rising flour = 125 grams
     * 1 cup white sugar = 200 grams
     * 1 cup milk = 245 grams
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blueberry_cobbler_bluetooth_steps);
        btStatusDisplay = (TextView)findViewById(R.id.cobblerBtStatus);
        miscDisplay = (TextView)findViewById(R.id.miscDisplay);
        flourAddedDisplay = (TextView)findViewById(R.id.flourAddedDisplay);
        sugarAddedDisplay = (TextView)findViewById(R.id.sugarAddedDisplay);
        milkAddedDisplay = (TextView)findViewById(R.id.milkAddedDisplay);
        rawDataDisplay = (TextView)findViewById(R.id.testingDisplay);
        findBluetooth();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_blueberry_cobbler_bluetooth_steps, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()){
            case R.id.action_OpenBT:
                try {
                    openBluetooth();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return true;
            case R.id.action_CloseBT:
                try {
                    closeBluetooth();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /** ------------------ BLUETOOTH METHODS -----------------------------------*/

    /**
     * Checks whether Bluetooth is supported and enabled - requests to enable if not
     */
    public void findBluetooth() {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
            btStatusDisplay.setText("Device does not support");
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
                    btStatusDisplay.setText("Device found");
                    break;
                }
            }
        }
    }

    /**
     * Opens connection to mmDevice
     */
    public void openBluetooth() throws IOException {
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

            btStatusDisplay.setText("Open");
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
                                            if (data.charAt(0) == '+') {
                                                // it's a weight value
                                                rawDataDisplay.setText(data);
                                                Integer intWeight = parseInt(data);
                                                //interpretedDataDisplay.setText(String.valueOf(intWeight));
                                                setCurrentWeight(intWeight);
                                                if (weighBowl){setBowlWeight(intWeight);}
                                                switch(ingredientChosen){
                                                    case 1:
                                                        //flour
                                                        weighBowl = false;
                                                        miscDisplay.setText("Add flour!");
                                                        if (currentWeight-bowlWeight < FLOUR_GOAL) {
                                                            //TODO: add margin of error
                                                            int flourPercentage;
                                                            flourPercentage = (int)(Math.round((currentWeight-bowlWeight)*100.0/FLOUR_GOAL));
                                                            flourAddedDisplay.setText("Flour: " + (String.valueOf(flourPercentage)) + "%");
                                                        }
                                                        else{
                                                            miscDisplay.setText("Good on flour! Choose next ingredient.");
                                                            flourAddedDisplay.setText("Flour: 100%");
                                                            ingredientChosen = 0;
                                                        }
                                                        break;
                                                    case 2:
                                                        //sugar
                                                        int previousWeight = bowlWeight + FLOUR_GOAL;
                                                        miscDisplay.setText("Add sugar!");
                                                        if((currentWeight - previousWeight) < SUGAR_GOAL){
                                                            int sugarPercentage;
                                                            sugarPercentage = (int)(Math.round((currentWeight - previousWeight)*100.0/SUGAR_GOAL));
                                                            sugarAddedDisplay.setText("Sugar: " + (String.valueOf(sugarPercentage)) + "%");
                                                        }
                                                        else{
                                                            miscDisplay.setText("Done with sugar! Choose next ingredient.");
                                                            sugarAddedDisplay.setText("Sugar: 100%");
                                                            ingredientChosen = 0;
                                                        }
                                                        break;
                                                    case 3:
                                                        //milk
                                                        int previousWeight2 = bowlWeight + FLOUR_GOAL + SUGAR_GOAL;
                                                        miscDisplay.setText("Add milk!");
                                                        if((currentWeight - previousWeight2) < MILK_GOAL){
                                                            int milkPercentage;
                                                            milkPercentage = (int)(Math.round((currentWeight - previousWeight2)*100.0/MILK_GOAL));
                                                            milkAddedDisplay.setText("Milk: " +(String.valueOf(milkPercentage)) + "%");
                                                        }
                                                        else{
                                                            miscDisplay.setText("Nice! Everything's added. Tap the Next Step button to continue.");
                                                            milkAddedDisplay.setText("Milk: 100%");
                                                            ingredientChosen = 0;
                                                            nextStepReady = true;
                                                        }
                                                        break;
                                                }
/**
                                                if (weighBowl){setBowlWeight(intWeight);}
                                                if (addingFlour){
                                                    weighBowl = false;
                                                    miscDisplay.setText("Add flour!");
                                                    if (currentWeight-bowlWeight < FLOUR_GOAL) {
                                                        //TODO: add margin of error
                                                        int flourPercentage;
                                                        flourPercentage = (int)(Math.round((currentWeight-bowlWeight)*100.0/FLOUR_GOAL));
                                                        flourAddedDisplay.setText("Flour: " + (String.valueOf(flourPercentage)) + "%");
                                                    }
                                                    else{
                                                        miscDisplay.setText("Good! Choose next ingredient.");
                                                        flourAddedDisplay.setText("Flour: 100%");
                                                    }
                                                }*/

                                            }
                                            else { // it's a command
                                                switch (data) {
                                                    case "@ACKON": // Acknowledge @MONON# successfully turned on weight monitoring
                                                        // TODO: do stuff
                                                        rawDataDisplay.setText(data);
                                                        break;
                                                    case "@ACKOFF": // Acknowledge @MONOFF# successfully turned off weight monitoring
                                                        // TODO: do stuff
                                                        rawDataDisplay.setText(data);
                                                        break;
                                                    case "@ACKPWRDN": // Acknowledge @PWRDN# successfully turned off power
                                                        // TODO: do stuff
                                                        rawDataDisplay.setText(data);
                                                        break;
                                                    case "@ACKPWRUP": // Acknowledge @PWRUP# successfully powered on scale
                                                        // TODO: do stuff
                                                        rawDataDisplay.setText(data);
                                                        break;
                                                    case "@RESET": // Scale just powered up; connect Bluetooth
                                                        // TODO: do stuff
                                                        rawDataDisplay.setText(data);
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

    public void closeBluetooth() throws IOException {
        stopWorker = true;
        mmOutputStream.close();
        mmInputStream.close();
        mmSocket.close();
        btStatusDisplay.setText("Closed");
    }

    /** -------------- END BLUETOOTH METHODS -----------------------------------*/

    public void sendCommand(String command) throws IOException {
        mmOutputStream.write(command.getBytes());
        btStatusDisplay.setText("Command " + command + " sent.");
    }

    public void setCurrentWeight(int integer){
        currentWeight = integer;
    }

    public void setBowlWeight(int integer) {bowlWeight = integer;}
/**
    public void flourTapped(View view){
        bowlWeight = currentWeight;
        miscDisplay.setText("Add flour!");
        while (currentWeight-bowlWeight < FLOUR_GOAL) {
            //TODO: add margin of error
            Integer flourPercentage;
            flourPercentage = (currentWeight-bowlWeight)/FLOUR_GOAL;
            flourAddedDisplay.setText("Flour: " + (String.valueOf(flourPercentage)) + "%");
        }
        miscDisplay.setText("Good! Choose next ingredient.");
    }
*/

    public void flourTapped(View view){
        weighBowl = false;
        ingredientChosen = 1;
    }

    public void sugarTapped(View view){
        ingredientChosen = 2;
    }

    public void milkTapped(View view){
        ingredientChosen = 3;
    }
/**
    public void sugarTapped(View view){
        int previousWeight = currentWeight;
        miscDisplay.setText("Add sugar!");
        while(currentWeight-previousWeight < SUGAR_GOAL){
            Integer sugarPercentage;
            sugarPercentage = (currentWeight-previousWeight)/SUGAR_GOAL;
            sugarAddedDisplay.setText("Sugar: " + (String.valueOf(sugarPercentage)) + "%");
        }
        miscDisplay.setText("Good! Choose next ingredient.");
    }


    public void milkTapped(View view){
        int previousWeight = currentWeight;
        miscDisplay.setText("Add milk!");
        while(currentWeight-previousWeight < MILK_GOAL){
            Integer milkPercentage;
            milkPercentage = (currentWeight-previousWeight)/MILK_GOAL;
            milkAddedDisplay.setText("Milk: " + (String.valueOf(milkPercentage)) + "%");
        }
        miscDisplay.setText("Nice! You're ready for the next step! Tap button to continue.");
        nextStepReady = true;
    }
 */

    public void selectNextStep(View view){
        if (nextStepReady){
            miscDisplay.setText("Next step launched.");
            Intent intent = new Intent(this, BlueberryCobblerFinalStep.class);
            startActivity(intent);
        }
        else{
            miscDisplay.setText("Not done adding ingredients! Please complete before continuing to next step.");
        }
    }

    public void selectMonOn(View view){
        try {
            sendCommand("@MONON#");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void selectMonOff(View view){
        try {
            sendCommand("@MONOFF#");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



}
