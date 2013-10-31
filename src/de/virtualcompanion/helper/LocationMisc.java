package de.virtualcompanion.helper;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;

public class LocationMisc implements LocationListener, ConnectionCallbacks,
								GooglePlayServicesClient.OnConnectionFailedListener {
	
	public LocationClient locationclient;
	public Location location;
	public boolean hasLocation = false;
	
	LocationMisc(Context context)	{
		locationclient = new LocationClient(context, this, this);
	}
	
	@Override
	public void onConnected(Bundle arg0) {
		LocationRequest request = LocationRequest.create();
		request.setNumUpdates(1);
		locationclient.requestLocationUpdates(request, (LocationListener) this);
		location = locationclient.getLastLocation();
		hasLocation = true;
		locationclient.setMockMode(true);	
	}

	@Override
	public void onDisconnected() {
		// TODO Auto-generated method stub
	}

	@Override
	public void onLocationChanged(Location arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onConnectionFailed(ConnectionResult arg0) {
		// TODO Auto-generated method stub
	}

}
