package handyapps.price.gareth.odometer;

import android.os.Bundle;
import android.preference.PreferenceFragment;

/**
 * Created by Gareth on 2015-02-28.
 * Package: handyapps.price.gareth.odometer
 */
public class UserPreferencesFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);
    }
}
