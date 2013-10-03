package de.virtualcompanion.helper;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Keine Ahnung was ich hier reinschreiben soll.
 * Ist halt ein Fragment auf das man sein eigenes Zeugs aufbauen kann ;)
 * @author florianzorn
 *
 */

public class MyFragment extends Fragment {
	View myView = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState)	{
		super.onCreate(savedInstanceState);
		//TODO
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)	{
		super.onCreateView(inflater, container, savedInstanceState);
		//TODO
		return inflater.inflate(R.layout.my_fragment, container, false);
	}
	
	/*
	@Override
	protected void onPause()	{
		//TODO
	}
	*/
	
	
}
