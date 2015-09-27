package com.weightocook.weightocook;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class BlueberryCobblerBluetoothSteps extends AppCompatActivity {


    TextView btStatusDisplay;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blueberry_cobbler_bluetooth_steps);
        btStatusDisplay = (TextView)findViewById(R.id.cobblerBtStatus);

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
                //open Bluetooth
                btStatusDisplay.setText("Open BT Action Tapped");
                return true;
            case R.id.action_CloseBT:
                //close Bluetooth
                btStatusDisplay.setText("Close BT Action Tapped");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }




}
