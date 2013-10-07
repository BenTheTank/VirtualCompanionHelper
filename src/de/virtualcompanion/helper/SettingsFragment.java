package de.virtualcompanion.helper;

import android.os.Bundle;
import android.preference.PreferenceFragment;

/**
 * Tab for all the settings
 * most of them are for the sip-audio-connection
 * @author florianzorn
 *
 */

public class SettingsFragment extends PreferenceFragment {
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
    }
}
