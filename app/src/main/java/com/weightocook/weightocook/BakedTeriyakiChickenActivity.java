package com.weightocook.weightocook;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class BakedTeriyakiChickenActivity extends DinnerCategoryActivity {
    public final static String SEARCH_RESULTS = "com.weightocook.weightocook.SEARCH_RESULTS";
    ListView btcIngredientListView;
    ArrayAdapter<String> myAdapter;
    String [] btcIngredientList = {"8 grams of Cornstarch","15 grams of Cold Water",
            "100 grams of Sugar","144 grams of Soy Sauce", "60 grams of Cider Vinegar",
            "5 grams of Garlic, Minced", "1 gram of Ground Ginger","1 gram of Ground Black Pepper",
            "672 grams of Skinless Chicken Thighs"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_btc);

        /**
        btcIngredientListView = (ListView) findViewById(R.id.dinnerListView);

        myAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                btcIngredientList);


        btcIngredientListView.setAdapter(myAdapter);

*/
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_baked_teriyaki_chicken, menu);
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
