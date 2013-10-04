package de.virtualcompanion.helper;

import org.json.JSONException;
import org.json.JSONObject;

import android.location.Location;

public class Data {	
	// GPS Daten
	private Location location; // Position
	
	// Benutzerdaten
	private String name; // Simpler Benutzername
	private String ip = "0.0.0.0";
	private String network_type; // Der Datenempfangstyp (GSM, GPRS, 3G, etc.. )
	private String pic;
	private boolean status; // Verbindung soll aktiv sein oder beendet
	long timestamp;
	LocationMisc locationMisc;
	
	// Server
	private String domain = "http://virtuellerbegleiter.rothed.de/";
	private String get = "androidmessages.html";
	private String pic_path = "pictures/";
	private JSONObject rawData;
	
	/* Konstanten */	
	private static final String TAG_TIMESTAMP = "timestamp";
	private static final String TAG_STATUS = "status";
	private static final String TAG_NAME = "name";
	private static final String TAG_IP = "ip";
	private static final String TAG_NETWORK = "network";
	private static final String TAG_PIC = "pic";
	private static final String TAG_LOC = "location";
	private static final String TAG_LOC_LONG = "long";
	private static final String TAG_LOC_LAT = "lat";
	private static final String TAG_LOC_ACC = "acc";
	private static final String TAG_LOC_ET = "et";
	private static final String TAG_LOC_ALT = "alt";
	private static final String TAG_LOC_BEAR = "bear";
	
	Data() {
		rawData = new JSONObject();	
	}
	
	public void getData() {
		new JSONParser(this).execute(domain + get);
	}
	
	public void fillData(JSONObject rawData) {        
        try {                              
            // Storing each json item in variable        	
        	timestamp = rawData.getLong(TAG_TIMESTAMP);
        	status = rawData.getBoolean(TAG_STATUS);
        	name = rawData.getString(TAG_NAME);
        	ip = rawData.getString(TAG_IP);
        	network_type = rawData.getString(TAG_NETWORK);
        	pic = rawData.getString(TAG_PIC);
        	JSONObject rawLocation = rawData.getJSONObject(TAG_LOC);
        	location.setAccuracy((float) rawLocation.getDouble(TAG_LOC_ACC));
        	location.setAltitude(rawLocation.getDouble(TAG_LOC_ALT));
        	location.setBearing((float) rawLocation.getDouble(TAG_LOC_BEAR));
        	location.setLatitude(rawLocation.getDouble(TAG_LOC_LAT));
        	location.setLongitude(rawLocation.getDouble(TAG_LOC_LONG));
        	location.setElapsedRealtimeNanos(rawLocation.getLong(TAG_LOC_ET));
        } catch (JSONException e) {
            e.printStackTrace();
        }
	}
	
	public Location getLocation()	{
		return location;
	}
	
	public void setLocation(Location location) {
		this.location = location;
	}
	
	public boolean isStatus()	{
		return status;
	}
	
	public String getPicpath() {
		return (domain + pic_path + pic);
	}
	
	public boolean isPic() {
		return (pic == null ? false : true);
	}
}
