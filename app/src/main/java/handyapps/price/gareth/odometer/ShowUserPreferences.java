package handyapps.price.gareth.odometer;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;

/**
 * Created by Gareth on 2015-02-28.
 * Package: handyapps.price.gareth.odometer
 */
public class ShowUserPreferences extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new UserPreferencesFragment())
                .commit();

        // Adds the home left facing arrow (<-) to the action bar
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        // define the color of the action bar
        setActionBarColor();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // If the user clicks the back/home arrow
        switch (item.getItemId()){
            case android.R.id.home:
                finish(); // Close the preference screen
        }
        return super.onOptionsItemSelected(item);
    }

    // sets the color of the actionbar
    private void setActionBarColor(){

        ActionBar actionbar = getSupportActionBar();
        actionbar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.primary_background)));
    }
}
