package handyapps.price.gareth.odometer;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.analytics.GoogleAnalytics;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity implements LocationListener {

    private TextView distance, distanceUnit;
    private ImageView gpsAccuracy;
    private LocationManager locMan;
    private String provider;
    private AdView mAdView;
    private int accuracy;
    private double dist;
    private ArrayList<Location> locations = new ArrayList<>();
    private final int REQUEST_PERMISSION_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // define the color of the action bar
        setActionBarColor();

        // Set font of text views
        setTypeFace();

        // sets the preference default values the first time the app runs
        PreferenceManager.setDefaultValues(this, R.xml.settings, false);

        // Get a Tracker (should auto-report)
        ((GATrackers) getApplication()).getTracker(GATrackers.TrackerName.APP_TRACKER);

        // reset distance
        distance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                locations.clear();
                distance.setText(getString(R.string.reset_value));
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        //Get an Analytics tracker to report app starts
        GoogleAnalytics.getInstance(this).reportActivityStart(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Stop the analytics tracking
        GoogleAnalytics.getInstance(this).reportActivityStop(this);
    }

    @Override
    protected void onResume() {

        super.onResume();
        // If the devices location services are disabled
        if (!locationServiceEnabled()) {
            // Notify user
            locationServiceDisabledAlert();
        } else {
            // Start retrieving location updates
            startLocationUpdates();
            // Start admob
            startAds();
        }
    }

    @Override
    protected void onPause() {

        super.onPause();
        // Stop retrieving location updates
        stopLocationUpdates();
        // Pause admob
        pauseAds();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_settings:
                startActivity(new Intent(this, ShowUserPreferences.class));
                break;
            case R.id.action_reset:
                locations.clear();
                distance.setText(getString(R.string.reset_value));
        }
        return true;
    }

    @Override
    public void onLocationChanged(Location location) {

        Log.i("onLocationChanged","Location update: " + location.toString());
        // Executes an async task to update the speed every time there is a location update
        new RetrieveDistance().execute(location);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.i("--onStatusChanged", "Status Changed " + status);
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.i("--onProviderEnabled", "Provider Enabled " + provider);
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.i("--onProviderDisabled", "Provider Disabled " + provider);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {

        // Save the current distance, distance unit and locations arraylist
        outState.putString("STATE_DIST", distance.getText().toString());
        outState.putString("STATE_UNIT", distanceUnit.getText().toString());
        outState.putParcelableArrayList("STATE_LOCATIONS", locations);
        outState.putInt("accuracy", accuracy);

        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {

        // Always call the superclass so it can restore the view hierarchy
        super.onRestoreInstanceState(savedInstanceState);

        // Restore from saved instance
        distance.setText(savedInstanceState.getString("STATE_DIST"));
        distanceUnit.setText(savedInstanceState.getString("STATE_UNIT"));
        locations = savedInstanceState.getParcelableArrayList("STATE_LOCATIONS");
        accuracy = savedInstanceState.getInt("accuracy");
        setGpsAccuracy();
    }

    // Starts new ad requests from admob
    private void startAds() {

        mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }

    // Pauses admob requests
    private void pauseAds() {

        if (mAdView != null) {
            mAdView.pause();
        }
    }

    // Sets the color of the actionbar
    private void setActionBarColor() {

        ActionBar actionbar = getSupportActionBar();
        if (actionbar != null) {
            actionbar.setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(MainActivity.this,R.color.primary_background)));
        }
    }

    // Determines if location services are enabled
    private boolean locationServiceEnabled() {

        LocationManager locMan = (LocationManager) getSystemService(LOCATION_SERVICE);
        return locMan.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    // Displays an alert dialog allowing the user to turn location services on
    private void locationServiceDisabledAlert() {

        //https://github.com/afollestad/material-dialogs
        new MaterialDialog.Builder(this)
                .title(R.string.enable_location_service_title)
                .content(R.string.enable_location_service)
                .positiveText(R.string.enable)
                .negativeText(R.string.exit)
                .cancelable(false)
                .iconRes(R.drawable.ic_alert_white)
                .theme(Theme.DARK)
                .onPositive(new MaterialDialog.SingleButtonCallback(){

                    @Override
                    public void onClick(MaterialDialog dialog, DialogAction which) {

                        //opens the location service settings
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        // closes the speedometer application
                        finish();
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(MaterialDialog dialog, DialogAction which) {

                        finish();
                    }
                })

                .show();
    }

    // Sets the text to the digital typeface stored in the assets folder
    private void setTypeFace() {

        Typeface digitalFont = Typeface.createFromAsset(getAssets(), "fonts/digital.ttf");
        distance = (TextView) findViewById(R.id.tvDistance);
        distance.setTypeface(digitalFont);

        distanceUnit = (TextView) findViewById(R.id.tvDistanceUnit);
        distanceUnit.setTypeface(digitalFont);
    }

    // Sets the unit value to display
    private void setUnit() {

        Preferences preferences = new Preferences(MainActivity.this);

        if (preferences.getDistanceUnit() == 1000) {

            distanceUnit.setText(R.string.kilometers);
        } else if (preferences.getDistanceUnit() == 1609.344) {
            distanceUnit.setText(R.string.miles);
        } else {
            distanceUnit.setText(R.string.nautical_miles);
        }
    }

    // Sets the satellite icon depending on the location accuracy
    private void setGpsAccuracy() {

        ImageView gpsAccuracy = (ImageView) findViewById(R.id.ivGPSAccuracy);
        if (accuracy == 0) {
            gpsAccuracy.setBackgroundResource(R.drawable.gps_low);
        } else if (accuracy < 5) {
            gpsAccuracy.setBackgroundResource(R.drawable.gps_high);
        } else if (accuracy < 10) {
            gpsAccuracy.setBackgroundResource(R.drawable.gps_med);
        } else {
            gpsAccuracy.setBackgroundResource(R.drawable.gps_low);
        }
    }

    // Starts location updates
    private void startLocationUpdates() {

        // Before starting location updates check the required permissions are allowed
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,Manifest.permission.ACCESS_FINE_LOCATION)){

                Snackbar.make(findViewById(android.R.id.content),"location permission required",Snackbar.LENGTH_INDEFINITE)
                        .setAction("Allow", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_PERMISSION_CODE);
                            }
                        })
                        .show();
            }
            else {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_PERMISSION_CODE);
            }
        }
        else {
            locMan = (LocationManager) getSystemService(LOCATION_SERVICE);
            provider = LocationManager.GPS_PROVIDER;
            locMan.requestLocationUpdates(provider, 2000, 0, this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode){

            case REQUEST_PERMISSION_CODE:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(MainActivity.this, "Permission Granted", Toast.LENGTH_SHORT).show();
                }
        }
    }

    // Stops location updates
    private void stopLocationUpdates(){

        if(locMan != null) {
            locMan.removeUpdates(this);
        }
    }

    // Update distance on background thread
    private class RetrieveDistance extends AsyncTask<Location,Void,Double>{

        @Override
        protected Double doInBackground(Location... params) {

            locations.add(params[0]);
            LocationInfo locationInfo = new LocationInfo(locations,getApplicationContext());
            dist = locationInfo.getDistance();
            accuracy = locationInfo.getAccuracy();
            return dist;
        }

        @Override
        protected void onPostExecute(Double aDouble) {

            super.onPostExecute(aDouble);
            // Sets the text view to the total distance
            distance.setText(String.valueOf(aDouble));

            // sets accuracy icon
            setGpsAccuracy();

            // Sets the distance unit text view
            setUnit();
        }
    }
}
