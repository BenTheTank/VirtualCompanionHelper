package de.virtualcompanion.helper;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapView;

public class FragmentMap extends Fragment	{
	
	GoogleMap googlemap;
	GoogleMapOptions googlemapoptions;
	
	MapView mapView;
	
	Bundle savedInstanceState;
	
	
	@Override
	public void onCreate(Bundle savedInstanceState)	{
		super.onCreate(savedInstanceState);
		
		this.savedInstanceState = savedInstanceState;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)	{
		super.onCreateView(inflater, container, savedInstanceState);
		return inflater.inflate(R.layout.map_fragment_layout, container, false);
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState)	{
		initmap();
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
}
