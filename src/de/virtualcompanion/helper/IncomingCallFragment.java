package de.virtualcompanion.helper;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

public class IncomingCallFragment extends DialogFragment {
	AlertDialog dialog;
	View view;
	
	/* The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it. */
    public interface IncomingCallFragmentListener {
        public void onDialogPositiveClick(DialogFragment dialog);
        public void onDialogNegativeClick(DialogFragment dialog);
    }
    
    // Use this instance of the interface to deliver action events
    IncomingCallFragmentListener mListener;
    
    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (IncomingCallFragmentListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement NoticeDialogListener");
        }
    }

	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState)	{
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		view = getActivity().getLayoutInflater().inflate(R.layout.incoming_call_fragment, null);
		builder.setView(view);
		builder.setTitle(R.string.incomingCallDialogTitle);
		dialog = builder.create();
		dialog.setCancelable(false);
		dialog.setCanceledOnTouchOutside(false);
		this.setCancelable(false);
		return dialog;
	}
	
	@Override
	public void onStart()	{
		super.onStart();
		
		view.findViewById(R.id.positiveButton).setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// Send the positive button event back to the host activity
                mListener.onDialogPositiveClick(IncomingCallFragment.this);
                dialog.dismiss();
			}
			
		});
		view.findViewById(R.id.negativeButton).setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// Send the positive button event back to the host activity
                mListener.onDialogNegativeClick(IncomingCallFragment.this);
                dialog.dismiss();
			}
			
		});
	}
}
