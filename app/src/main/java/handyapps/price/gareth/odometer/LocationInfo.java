package handyapps.price.gareth.odometer;

import android.content.Context;
import android.location.Location;

import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * Created by Gareth on 2015-02-28.
 * Package: handyapps.price.gareth.odometer
 */
public class LocationInfo {


    private ArrayList<Location> locations;
    private Location locationA,locationB;
    private Context context;
    double distance;
    DecimalFormat twoDec = new DecimalFormat("#.##");

    public LocationInfo(ArrayList<Location> locsList,Context con){

        locations = locsList;
        context = con;
    }

    // Returns the total distance between all locations stores in locations list
    protected double getDistance() {

        if(locations.size() == 1){
            // Return 0 if list only has one entry
            distance = 0;
        }
        else{
            // Loop through list and return total distance
            for(int i = 0 ; i < (locations.size()-1); i++){

                // Set Location A
                locationA = new Location("A");
                locationA.setLatitude(locations.get(i).getLatitude());
                locationA.setLongitude(locations.get(i).getLongitude());

                // Set Location B
                locationB = new Location("B");
                locationB.setLatitude(locations.get(i+1).getLatitude());
                locationB.setLongitude(locations.get(i+1).getLongitude());
                locationB.setAccuracy(locations.get(i+1).getAccuracy());

                // Set total distance
                distance += locationA.distanceTo(locationB);
            }
        }
        // return total distance formatted with 2 decimal places and in the required distance unit
        Preferences prefs = new Preferences(context);
        return Double.valueOf(twoDec.format(distance / prefs.getDistanceUnit()));
    }

    // Returns the accuracy of the location data
    protected int getAccuracy(){

        if(locations.size() == 1){
            return (int)locations.get(0).getAccuracy();
        }
        else{
            return (int)locationB.getAccuracy();
        }
    }
}
