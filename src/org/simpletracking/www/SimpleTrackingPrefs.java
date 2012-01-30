package org.simpletracking.www;
/* The part of SimpleTracking open source project. Web: http://simpletracking.org */
import android.os.Bundle;
import android.preference.PreferenceActivity;


public class SimpleTrackingPrefs extends PreferenceActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.prefs);
    }
    
}