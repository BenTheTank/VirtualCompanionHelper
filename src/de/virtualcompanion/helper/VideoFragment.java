package de.virtualcompanion.helper;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

public class VideoFragment extends Fragment {
	ImageView imageView = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState)	{
		super.onCreate(savedInstanceState);		
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)	{
		super.onCreateView(inflater, container, savedInstanceState);
		return inflater.inflate(R.layout.video_fragment, container, false);
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState)	{
		imageView = (ImageView) view.findViewById(R.id.imageView_videoFragment);	//getView().findViewById(int) would work as well, only findViewById(int) alone wouldn't work (works only inside an activities sibling)
		
		// setting an image to be displayed as long as there has no image been loaded from the server
		imageView.setImageResource(R.drawable.icon);
	}
	
	/*
	 * Method for changing the image in the ImageView of VideoFragment Fragment
	 * for example when a new picture has been loaded from the server...
	 */
	public void changeImage(String url){
	//	new DownloadImageTask(imageView).execute(url);
	}

	/*
	@Override
	protected void onPause()	{
		//TODO
	}
	*/
	
	
}
