package de.virtualcompanion.helper;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceFragment;

/**
 * Tab for all the settings
 * most of them are for the sip-audio-connection
 * @author florianzorn
 *
 */

public class SettingsFragment extends PreferenceFragment
							implements OnSharedPreferenceChangeListener	{
	
	/* The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it. */
    public interface PreferenceListener {
        public void onPreferenceChanged();
    }
    
    // Use this instance of the interface to deliver action events
    PreferenceListener mListener;
    
    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (PreferenceListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement NoticeDialogListener");
        }
    }
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
    }
	
	@Override
	public void onResume()	{
		super.onResume();
		getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		mListener.onPreferenceChanged();
		
		((MasterActivity) this.getActivity()).data.setResolution(sharedPreferences.getString(Data.TAG_RESOLUTION, "low"));
		((MasterActivity) this.getActivity()).data.setFlashlight(sharedPreferences.getBoolean(Data.TAG_FLASHLIGHT, false));
		((MasterActivity) this.getActivity()).data.sendData();		
	}
}
