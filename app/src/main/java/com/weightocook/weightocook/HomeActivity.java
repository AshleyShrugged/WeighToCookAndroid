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
}
