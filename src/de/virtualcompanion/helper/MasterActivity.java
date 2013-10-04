package de.virtualcompanion.helper;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;

public class MasterActivity extends Activity implements Runnable {
	ImageView videoFragment_imageView = null;
	MenuItem connectionLight;
	
	// Handler fuer zeitverzoegertes senden
	private Handler handler = new Handler();
	private static final int INTERVALL = 2000; // Verzoegerung in ms
	protected Data data; // Datencontainer
	protected LocationMisc locationMisc;
	private ActionBar actionBar;
	
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
	    				.setText(getString(R.string.tab_x))
	    				.setTabListener(new TabListener<MyFragment>(this, "TAG", MyFragment.class)));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu items for use in the action bar
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.master, menu);
	    connectionLight = menu.getItem(0);
	    return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public void onResume()	{
		super.onResume();
		
		if(locationMisc == null)
			locationMisc = new LocationMisc(this.getApplicationContext());
		
		if(!locationMisc.locationclient.isConnected())
			locationMisc.locationclient.connect();
		
		if(data == null)
			data = new Data();
		
		handler.postDelayed(this,INTERVALL); // startet handler (run())!
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
		
		if((actionBar.getSelectedNavigationIndex() == 0) & data.isPic()) {
			new DownloadImageTask((ImageView) this.findViewById(R.id.imageView_videoFragment)).execute(data.getPicpath());
		}			
		
		//if (data.isStatus())
			handler.postDelayed(this,INTERVALL); // startet nach INTERVALL wieder den handler (Endlosschleife)
	}
}
