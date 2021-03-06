package com.weightocook.weightocook;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.internal.widget.AdapterViewCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

public class DinnerCategoryActivity extends AppCompatActivity{

    public final static String SEARCH_RESULTS = "com.weightocook.weightocook.SEARCH_RESULTS";
    String [] dinnerRecipeList = new String[]{"Baked Teriyaki Chicken","Slow Cooker Beef Pot Roast",
            "Brown Sugar Meatloaf","Broiled Tilapia Parmesan", "Chicken Cordon Bleu"};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dinner_category);
        ListView dinnerListView = (ListView)findViewById(R.id.dinnerListView);

        ArrayAdapter<String> myAdapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_list_item_1,
                dinnerRecipeList);


        dinnerListView.setAdapter(myAdapter);

        dinnerListView.setOnItemClickListener (new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    if (position == 0)
                    {

                        Intent intent = new Intent(DinnerCategoryActivity.this, BakedTeriyakiChickenActivity.class);
                        startActivity(intent);

                    }


            }
        } );
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_dinner_category, menu);
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

    /** Called when the user clicks the Search button */
    public void recipeSearch(View view){
        Intent intent = new Intent(this, SearchResultsActivity.class);
        EditText editText = (EditText) findViewById(R.id.editSearch); //gets the EditText element from the search box
        String searchResultsStr = editText.getText().toString(); // turn the search results into a string for use in next method
        intent.putExtra(SEARCH_RESULTS, searchResultsStr); // this is packaging the contents of editText for use in the new activity
        startActivity(intent);
    }

}
