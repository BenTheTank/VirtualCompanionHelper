package de.virtualcompanion.helper;

import android.app.ActionBar;
import android.app.Activity;
import android.app.DialogFragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.LatLng;

import de.virtualcompanion.helper.SipService.SipBinder;

public class MasterActivity extends Activity implements Runnable,
								IncomingCallFragment.IncomingCallFragmentListener	{
	private static final String TAG = "virtualCompanion";
	private ImageView videoFragment_imageView = null;
	private MenuItem connectionLight;
	private MenuItem followUser;
	public MenuItem startStopCall;
	
	// Handler fuer zeitverzoegertes senden
	private Handler handler = new Handler();
	private static final int INTERVALL = 2000; // Verzoegerung in ms
	protected Data data; // Datencontainer
	protected LocationMisc locationMisc;
	private ActionBar actionBar;
	
	private boolean startHandler = true;
	
	// for autofollowing function of the map fragment
	private static float zoomLevel = 14;
	
	
	/*
	 * SIP
	 * (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	
	SharedPreferences settings = null;
	protected SipService sipService = null;
	private boolean sipBound = false;
	private boolean canMakeCall = false;
	private boolean isInCall = false;
	
	
	/** Defines callbacks for service binding, passed to bindService() */
	private ServiceConnection mConnection = new ServiceConnection() {
	    // Called when the connection with the service is established
	    public void onServiceConnected(ComponentName className, IBinder service) {
	        // Because we have bound to an explicit
	        // service that is running in our own process, we can
	        // cast its IBinder to a concrete class and directly access it.
	        SipBinder binder = (SipBinder) service;
	        sipService = binder.getService();
	        sipBound = true;
	    }
	    
	 // Called when the connection with the service disconnects unexpectedly
	    public void onServiceDisconnected(ComponentName className) {
	        //Log.e(TAG, "onServiceDisconnected");
	        sipBound = false;
	    }
	};
	
	/*
	 * SIP variables DONE
	 * (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */

	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//setContentView(R.layout.activity_master);
		
	    actionBar = getActionBar();
	    
	    // Specify that tabs should be displayed in the action bar.
	    actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

	
	    // Add 3 tabs(Maps,Video,x, specifying the tab's text and TabListener
	    actionBar.addTab(
	    		actionBar.newTab()
	    				.setText(getString(R.string.tab_video))
	    				.setTabListener(new TabListener<VideoFragment>(this, "TAG", VideoFragment.class)));
	    
	    actionBar.addTab(
	    		actionBar.newTab()
	    				.setText(getString(R.string.tab_map))
	    				.setTabListener(new TabListener<FragmentMap>(this, "TAG", FragmentMap.class)));
	   
	    actionBar.addTab(
	    		actionBar.newTab()
	    				.setText(getString(R.string.tab_settings))
	    				.setTabListener(new TabListener<SettingsFragment>(this, "TAG", SettingsFragment.class)));
	    startSip();
	}
	
	@Override
	public void onStart()	{
		super.onStart();
		
		bindSip();
	}
	
	@Override
	public void onResume()	{
		super.onResume();
		
		startHandler = true;
		
		if(locationMisc == null)
			locationMisc = new LocationMisc(this.getApplicationContext());
		
		if(!locationMisc.locationclient.isConnected())
			locationMisc.locationclient.connect();
		
		if(data == null)
			data = new Data();
		
		handler.postDelayed(this,INTERVALL); // startet handler (run())!
	}
	
	@Override
	protected void onDestroy()	{
		super.onDestroy();
		
		// SIP
		doUnbind();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu items for use in the action bar
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.master, menu);
	    connectionLight = menu.findItem(R.id.connection);
	    followUser = menu.findItem(R.id.followUser);
	    startStopCall = menu.findItem(R.id.startStopCall);
	    
	    return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	        case R.id.followUser:
	            if (item.isChecked())
	            	item.setChecked(false);
	            else
	            	item.setChecked(true);
	            return true;
	        case R.id.startStopCall:
	        	return callButtonBehaviour();
	        case R.id.action_settings:
	        	openIncomingCallDialog();
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	
	@Override
	public void run() {
		// Methode fuer den Handler laeuft alle INTERVALL ms

		if(locationMisc.hasLocation) {
			data.setLocation(locationMisc.location); // Should be done once ? 		
			data.getData();
			locationMisc.locationclient.setMockLocation(data.getLocation());
		}
		
		// change the name of the title so it matches the username you are helping right now
		this.setTitle("Watching: " + data.getName());
		
		// change here the connection quality indicator in the action bar
		if(data.isStatus())	{
			connectionLight.setIcon(R.drawable.light_green);
		} else	{
			connectionLight.setIcon(R.drawable.light_red);
		}
		
		// download image from server only if the VideoFragment is active
		if((actionBar.getSelectedNavigationIndex() == 0) & data.isPic()) {
			new DownloadImageTask((ImageView) this.findViewById(R.id.imageView_videoFragment)).execute(data.getPicpath());
		}			
		
		// Update MapViews Camera only if FragmentMap is active and checkbox is checked
		updateMapCamera();
		
		// Changing the make call icon
		updateCallIcon();
		
		//if (data.isStatus())
		if(startHandler)
			handler.postDelayed(this,INTERVALL); // startet nach INTERVALL wieder den handler (Endlosschleife)
	}
	
	// Update MapViews Camera only if FragmentMap is active and checkbox is checked
	private void updateMapCamera()	{
		if((actionBar.getSelectedNavigationIndex() == 1) & (followUser.isChecked()))	{
			try	{
				GoogleMap googlemap = ((MapView) findViewById(R.id.mapView)).getMap();
				MapsInitializer.initialize(this);
				
				Location location = googlemap.getMyLocation();
				if(location != null)	{
					LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
					//CameraUpdate camUp = CameraUpdateFactory.newLatLng(latLng);
					CameraUpdate camUp = CameraUpdateFactory.newLatLngZoom(latLng, zoomLevel);
					googlemap.animateCamera(camUp);
				}
			} catch(GooglePlayServicesNotAvailableException e){
			}
		}
	}
	
	@Override
	protected void onPause()	{
		super.onPause();
		startHandler = false;
	}
	
	
	/**
	 * Some routines for sip
	 */
	
	private void startSip()	{
	    settings = PreferenceManager.getDefaultSharedPreferences(this);
	    // Starting the Sip Service when we create the activity
	 	Intent intent = new Intent(this, SipService.class);
	 	startService(intent);
	}
	
	// This binds our SipService to the Activity
	private void bindSip()	{
		Intent intent = new Intent(this, SipService.class);
		bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
	}
	
	// Unbinding the service from the activity
	private void doUnbind()	{
		if(sipBound == true){
			// This call unbinds SipService from this acitivity
			unbindService(mConnection);
			sipBound = false;
		}
	}
	
	// Method for changing the call icon
	private void updateCallIcon()	{
		if(sipBound & sipService.isSipRegistrated())	{
			isInCall = sipService.isInCall();
			if(isInCall)
				startStopCall.setIcon(R.drawable.phone_red);
			else
				startStopCall.setIcon(R.drawable.phone_green);
			canMakeCall = true;
		}
		else	{
			startStopCall.setIcon(R.drawable.phone_gray);
			canMakeCall = false;
		}
	}
	
	// Behaviour selection for clicking the call button
	private boolean callButtonBehaviour()	{
		if(canMakeCall & !isInCall)	{
    		sipService.initiateAudioCall();
    		return true;
    	} else if(canMakeCall & isInCall)	{
    		sipService.endAudioCall();
    		return true;
    	} else
    		return true;
	}

	public void openIncomingCallDialog()	{
		DialogFragment dialog = new IncomingCallFragment();
		dialog.show(getFragmentManager(), "IncomingCallFragment");
	}
	
	@Override
	public void onDialogPositiveClick(DialogFragment dialog) {
		sipService.answerCall();
	}

	@Override
	public void onDialogNegativeClick(DialogFragment dialog) {
		sipService.endAudioCall();
	}
}
