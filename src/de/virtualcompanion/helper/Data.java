package de.virtualcompanion.helper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;

public class Data {	
	// GPS Daten
	private Location location; // Position
	
	// Benutzerdaten
	private String name; // Simpler Benutzername
	private String ip = "0.0.0.0";
	private String network_type; // Der Datenempfangstyp (GSM, GPRS, 3G, etc.. )
	private String pic;
	private String resolution;
	private boolean flashlight;
	private boolean status; // Verbindung soll aktiv sein oder beendet
	private long timestamp;
	private NetworkInfo netInf;
	private ConnectivityManager conMan;
	
	// Server
	private String domain = "http://virtuellerbegleiter.rothed.de/";
	private String get = "androidmessages.html";
	private String post = "reply.php";
	
	/* Zu empfangende Konstanten */	
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
	/* Zu sendende Konstanten */
	public static final String TAG_RESOLUTION = "resolution";
	public static final String TAG_FLASHLIGHT = "flashlight";
	
	Data(Context context) {
		conMan = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		netInf = conMan.getActiveNetworkInfo();
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
        	// This should fix errors on < API 17 devices
        	//location.setElapsedRealtimeNanos(rawLocation.getLong(TAG_LOC_ET));
        	location.setTime(rawData.getLong(TAG_TIMESTAMP));
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
	
	public String getName()	{
		return(name == null ? "nobody" : name);
	}
	
	public String getPic() {
		return (pic);
	}
	
	public boolean isPic() {
		return (pic == null ? false : true);
	}
	
	public void setFlashlight (boolean status) {
		this.flashlight = status;
	}
	
	public void setResolution(String res) {
		this.resolution = res;
	}
	
	private JSONObject createJSON() {
		JSONObject object = new JSONObject();
		try {
			object.put(TAG_RESOLUTION, resolution);
			object.put(TAG_FLASHLIGHT, flashlight);
			return object;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public void sendData() {
		if (netInf != null && netInf.isConnected()) {
			//new SendToWebpage().execute(domain + post);
			new SendToWebpage().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, domain + post);
		} else
			Log.e("VirtualCompanion", "No Network Connection available");
	}
	
	/*		FOR SENDING		*/
	private String startSending(String strUrl, HttpClient httpclient, HttpPost httppost){
		try{
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
		    nameValuePairs.add(new BasicNameValuePair("message", createJSON().toString()));
		    
		    httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
		    httpclient.execute(httppost);
		}catch (ClientProtocolException cpex) {
			return cpex.getMessage();
	    }
		catch (IOException ioex){
			return ioex.getMessage();
		}
		return "DONE";
	}
	
	private class SendToWebpage extends AsyncTask<String, String, Boolean>{
		@Override
		
		protected Boolean doInBackground(String... httpurl){
			HttpClient httpclient = new DefaultHttpClient();
	   	    HttpPost httppost = new HttpPost(httpurl[0]);
			publishProgress(startSending(httpurl[0], httpclient, httppost));
			return true;
		}
		
	}
}
