package de.virtualcompanion.helper;

import android.app.Fragment;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapFragment;

public class Map_Fragment extends Fragment implements LocationListener, Runnable, 
		GooglePlayServicesClient.ConnectionCallbacks, GooglePlayServicesClient.OnConnectionFailedListener {
	
	GoogleMap googlemap;
	GoogleMapOptions googlemapoptions;
	LocationClient locationclient;
	Location location;
	
	private Handler handler = new Handler();
	
	private String LOG_TAG = "faren";
	
	private double lat = 40.714418;
	private double lon = -74.006118;
	private float bear = 0;
	
	@Override
	public void onCreate(Bundle savedInstanceState)	{
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)	{
		super.onCreateView(inflater, container, savedInstanceState);
		//TODO
		return inflater.inflate(R.layout.my_fragment, container, false);
	}
	
	public void onViewCreated(View view, Bundle savedInstanceState)	{
		initmap();
		
		handler.postDelayed(this, 3000);
		
		locationclient = new LocationClient(this.getActivity().getApplicationContext(), this, this);
		locationclient.connect();
	}
	
	/*
	@Override
	protected void onPause()	{
		//TODO
	}
	*/
	
	private void initmap() 
	{
		MapFragment mapfragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
		//MapFragment mapfragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
		googlemap = mapfragment.getMap();

		googlemap.setMyLocationEnabled(true);
		googlemap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

	}

	@Override
	public void onLocationChanged(Location location) 
	{
		// TODO Auto-generated method stub
	}
	
	public void run() 
	{
	        // Set one position
//	        Location location = new Location(mocLocationProvider);
		
			location.setLatitude(40.714418);
	        location.setLongitude(-74.006118);
	        location.setAltitude(0);
	        location.setTime(System.currentTimeMillis());
	        location.setAccuracy(5);
	        location.setBearing(bear);
	        
//	        location.setElapsedRealtimeNanos(500);

	        Log.e(LOG_TAG, location.toString());

	        // set the time in the location. If the time on this location
	        // matches the time on the one in the previous set call, it will be
	        // ignored
	        location.setTime(System.currentTimeMillis());

			locationclient.setMockLocation(location);

	        lat = lat + 0.00001;
	        lon = lon + 0.0001;
	        bear = bear + 20;

	        handler.postDelayed(this, 3000);
	}

	@Override
	public void onConnectionFailed(ConnectionResult arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onConnected(Bundle arg0) {
		// TODO Auto-generated method stub
		LocationRequest request = LocationRequest.create();
		request.setNumUpdates(1);
		locationclient.requestLocationUpdates(request, this);
		location = locationclient.getLastLocation();
		locationclient.setMockMode(true);	
	}

	@Override
	public void onDisconnected() {
		// TODO Auto-generated method stub
		
	}
}
