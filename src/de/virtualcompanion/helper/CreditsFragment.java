package de.virtualcompanion.helper;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.View;

public class CreditsFragment extends DialogFragment	{
	AlertDialog dialog;
	View view;

	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState)	{
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		view = getActivity().getLayoutInflater().inflate(R.layout.credits_fragment, null);
		builder.setView(view);
		builder.setTitle(R.string.credits);
		dialog = builder.create();
		dialog.setCancelable(true);
		dialog.setCanceledOnTouchOutside(true);
		this.setCancelable(true);
		return dialog;
	}
	
	@Override
	public void onStart()	{
		super.onStart();
		
	}
}
