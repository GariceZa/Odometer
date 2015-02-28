package handyapps.price.gareth.odometer;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by Gareth on 2015-02-28.
 * Package: handyapps.price.gareth.odometer
 */
public class Preferences {

    Context context;

    public Preferences(Context cont){

        context = cont;
    }

    // returns the users height unit preference
    protected double getDistanceUnit(){

        SharedPreferences get = PreferenceManager.getDefaultSharedPreferences(context);
        return Double.parseDouble(get.getString("unit", "1000"));
    }
}
