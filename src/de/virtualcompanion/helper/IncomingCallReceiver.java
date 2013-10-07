package de.virtualcompanion.helper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import de.virtualcompanion.helper.SipService.SipBinder;

/*** Listens for incoming SIP calls, intercepts and hands them off to WalkieTalkieActivity.
 */

public class IncomingCallReceiver extends BroadcastReceiver {
	/**
     * Processes the incoming call, answers it, and hands it over to the
     * WalkieTalkieActivity.
     * @param context The context under which the receiver is running.
     * @param intent The intent being received.
     */
	
	public static final int TIMEOUT = 30;
	
	@Override
	public void onReceive(Context context, Intent intent)	{
		Intent myIntent = new Intent(context, SipService.class);
		SipBinder binder = (SipBinder) peekService(context, myIntent);
		SipService sipService = binder.getService();
		sipService.incomingCall(intent);
	}
}