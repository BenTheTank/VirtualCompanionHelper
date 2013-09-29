package de.virtualcompanion.helper;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.LatLng;

public class FragmentMap extends Fragment implements Runnable {
	
	GoogleMap googlemap;
	GoogleMapOptions googlemapoptions;
	LocationMisc locationMisc;
	
	private Handler handler = new Handler();
	
	private String LOG_TAG = "faren";
	
	private double lat = 40.714418;
	private double lon = -74.006118;
	private float bear = 0;
	
	MapView mapView;
	
	Bundle savedInstanceState;
	
	@Override
	public void onCreate(Bundle savedInstanceState)	{
		super.onCreate(savedInstanceState);
		
		this.savedInstanceState = savedInstanceState;
		
		//locationMisc = new LocationMisc();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)	{
		super.onCreateView(inflater, container, savedInstanceState);
		return inflater.inflate(R.layout.map_fragment_layout, container, false);
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState)	{
		
		initmap();
		
		
		handler.postDelayed(this, 3000);
		
		locationMisc = new LocationMisc(this.getActivity().getApplicationContext());
		locationMisc.locationclient.connect();
	}
	
	private void initmap() 
	{
		
		mapView = (MapView) getView().findViewById(R.id.mapView);
		mapView.onCreate(savedInstanceState);
		googlemap = mapView.getMap();
		
		googlemap.setMyLocationEnabled(true);
		//googlemap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
		
		// I like the normal map more than that hybrid crap ;) @florianzorn
		googlemap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
	}
	
	@Override
	public void onDestroy()	{
		super.onDestroy();
		mapView.onDestroy();
	}
	
	@Override
	public void onPause()	{
		super.onPause();
		mapView.onPause();
	}
	
	@Override
	public void onResume()	{
		super.onResume();
		mapView.onResume();
	}
	
	public void run() 
	{
	        // Set one position
//	        Location location = new Location(mocLocationProvider);
		
			locationMisc.location.setLatitude(40.714418);
			locationMisc.location.setLongitude(-74.006118);
			locationMisc.location.setAltitude(0);
			locationMisc.location.setTime(System.currentTimeMillis());
			locationMisc.location.setAccuracy(5);
			locationMisc.location.setBearing(bear);
	        
//	        location.setElapsedRealtimeNanos(500);

	        Log.e(LOG_TAG, locationMisc.location.toString());

	        // set the time in the location. If the time on this location
	        // matches the time on the one in the previous set call, it will be
	        // ignored
	        locationMisc.location.setTime(System.currentTimeMillis());

	        locationMisc.locationclient.setMockLocation(locationMisc.location);

	        lat = lat + 0.00001;
	        lon = lon + 0.0001;
	        bear = bear + 20;
	        
	        handler.postDelayed(this, 3000);
	}
}
