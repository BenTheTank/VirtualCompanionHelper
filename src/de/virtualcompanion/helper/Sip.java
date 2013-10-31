package de.virtualcompanion.helper;

import java.text.ParseException;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.sip.SipAudioCall;
import android.net.sip.SipException;
import android.net.sip.SipManager;
import android.net.sip.SipProfile;
import android.net.sip.SipRegistrationListener;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;

public class Sip extends BroadcastReceiver implements Runnable {
	
	private Handler handler = new Handler();
	
	// Sip Stuff
	private final static String TAG = "CLASS_SIP_VIRTUAL_COMPANION";
	private final static String ACTION_STRING = "android.virtualcompanion.helper.INCOMING_CALL";
	public final static int TIMEOUT = 30;
	public static final String PREFS_NAME = "preferences";
	
	SharedPreferences settings = null;
	
	private String peerSipAddress = null;
	
	public SipManager mSipManager = null;
	private SipProfile localSipProfile = null;
	public SipAudioCall audioCall = null;
	
	private int registrationAttemptCount = 0;
	// Sip Stuff done
	
	private boolean sipRegistrated = false;
	private boolean sipIsError = false;
	private boolean inCall = false;
	
	Context context;
	
	public Sip(Context context)	{
    	this.context = context;
		handler.post(this);
	}
	
	@Override
	public void run() {
		// Checking the preferences if there is a username and password
	    checkPreferences();
	    initializeSip();
	}
	
	public void onDestroy()	{
		sipRegistrated = false;
		closeLocalProfile();
		context.unregisterReceiver(this);
	}
	
	private void checkPreferences()	{
		PreferenceManager.setDefaultValues(context, R.xml.preferences, false);
		settings = PreferenceManager.getDefaultSharedPreferences(context);
  }
	
	/*
     * ****************************************
     * *			SIP-Framework 			  *
     * ****************************************
     */

	
	private void initializeSip()	{
		// Specify an intent filter to receive calls
		sepcifieIntentFilter();
		
		initializeManager();
	}
	
	/**
	 * Specifying an IntentFilter so we can receive calls
	 */
	public void sepcifieIntentFilter()	{
		// Specify an intent filter to receive calls
		IntentFilter filter = new IntentFilter();
		filter.addAction(ACTION_STRING);
		context.registerReceiver(this, filter);
	}
	
	private void initializeManager()	{
		if(mSipManager == null)	{
			mSipManager = SipManager.newInstance(context);
		}
		
		initializeLocalProfile();
	}
	
	public void initializeLocalProfile(){
		if(mSipManager == null){
			return;
		}
		
		if(localSipProfile != null){
			closeLocalProfile();
		}
		
		// We read the username and password out of our preferences
		// so we are able to log into the sip server
		String username = settings.getString("namePref", "");
		String password = settings.getString("passPref", "");
		String domain = settings.getString("domainPref", "");
		
		try	{
			registrationAttemptCount++;
			
			SipProfile.Builder builder = new SipProfile.Builder(username, domain);
			builder.setPassword(password);
			localSipProfile = builder.build();
			
			Intent i = new Intent();
			i.setAction(ACTION_STRING);
			PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, Intent.FILL_IN_DATA);			
			mSipManager.open(localSipProfile, pi, null);
			
			
			// This listener must be added AFTER manager.open is called,
            // Otherwise the methods aren't guaranteed to fire.
			// -Google
			
			mSipManager.setRegistrationListener(localSipProfile.getUriString(), new SipRegistrationListener() {
                public void onRegistering(String localProfileUri) {
                }

                public void onRegistrationDone(String localProfileUri, long expiryTime) {
                    registrationAttemptCount = 0;
                    
                    sipRegistrated = true;
                    sipIsError = false;
                }

                public void onRegistrationFailed(String localProfileUri, int errorCode,
                        String errorMessage) {
                	// If registration fails we will try it xxx times again
                	if(registrationAttemptCount < 100)	{
                		initializeManager();
                	} else	{
                	}
                	
                	sipRegistrated = false;
                	sipIsError = true;
                }
            });
		} catch (ParseException e) {
			Log.d(TAG, "ParseException in method: sipProfileCreator()", e);
		} catch (SipException e) {
		}
	}
	
	
	/**
	 * Closes local SIP Profile, freeing associated objects into memory
	 */
	public void closeLocalProfile()	{
		if(mSipManager == null)	{
			return;
		}
		try	{
			if(localSipProfile != null){
				mSipManager.close(localSipProfile.getUriString());
			}
		} catch(Exception e)	{
			Log.d(TAG, "Exception in method: closeLocalProfile()", e);
		}
	}
	
	
	
	/*
	 * *********************************
	 * *       Making Audio Call       *
	 * *********************************
	 */
	
	
	/**
	 * Make an outgoing call
	 */
	
	public void initiateAudioCall()	{
		// Acquiring the typed in address to call
		String buddyName = settings.getString("nameBuddyPref", "1001");
		peerSipAddress = buddyName +"@" + localSipProfile.getSipDomain();
		
		try	{
			SipAudioCall.Listener listener = getSipAudioCallListener();
			audioCall = mSipManager.makeAudioCall(localSipProfile.getUriString(), peerSipAddress, listener, 30);
			
		} catch(Exception e)	{
			Log.i(TAG, "Exception in method: initiateAudioCall()", e);
			if(localSipProfile != null){
				try	{
					mSipManager.close(localSipProfile.getUriString());
				} catch(Exception ee){
					Log.i(TAG, "Error when trying to close manager inside method: initiateAudioCall()", ee);
					ee.printStackTrace();
				}
			}
			if(audioCall != null)	{
				audioCall.close();
			}
		}
	}
	
	
	public void endAudioCall()	{
		try	{
			audioCall.endCall();
			audioCall.close();
			audioCall = null;
			inCall = false;
		} catch(SipException e)	{
			//do something
		}
	}
	
	
	// Implementation of SipAudioCall.Listener
	public SipAudioCall.Listener getSipAudioCallListener()	{
		SipAudioCall.Listener sipAudioCallListener = new SipAudioCall.Listener()	{
			// Much of the client's interaction with the SIP Stack will
	        // happen via listeners.  Even making an outgoing call, don't
	        // forget to set up a listener to set things up once the call is established.
			// -Google
			
			@Override
			public void onCallEstablished(SipAudioCall call)	{
				call.startAudio();
				call.setSpeakerMode(true);
				inCall = true;
			}
			
			@Override
			public void onCalling(SipAudioCall call){
				inCall = true;
			}
			
			@Override
			public void onCallEnded(SipAudioCall call){
				//updateStatus("Ready");
				call = null;
				inCall = false;
			}
			
			// here you can setup whatever happens when somebody is calling you
			// like playing ringtone...
			@Override
			public void onRinging(SipAudioCall call, SipProfile caller)	{
				super.onRinging(call, caller);
				answerCall();
			}
		};
		return sipAudioCallListener;
	}
	
	
	public void incomingCall(Intent intent){		
		try	{
			SipAudioCall.Listener listener = getSipAudioCallListener();
			audioCall = mSipManager.takeAudioCall(intent, listener);
			
			// Due to the shitty implementation of SIP I have to call the callback method onRinging()
			// myself <.< 
			listener.onRinging(audioCall, audioCall.getPeerProfile());
		} catch(Exception e)	{
			if(audioCall != null)	{
				audioCall.close();
				return;
			}
		}
	}

	
	public void answerCall()	{
		try	{
			audioCall.answerCall(TIMEOUT);
			audioCall.startAudio();
			audioCall.setSpeakerMode(true);
		} catch(Exception e)	{
			if(audioCall != null)	{
				audioCall.close();
			}
		}
	}
	
	
	
	/*
	 * *********************************
	 * *       		DONE			   *
	 * *       Making Audio Call       *
	 * *********************************
	 */
	
	public boolean isSipRegistrated()	{
		return sipRegistrated;
	}
	
	public boolean isInCall()	{
		return inCall;
	}
	
	public boolean isError(){
		return sipIsError;
	}

	/**
	 * BroadcastReceiver
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
		try {
			this.audioCall = this.mSipManager.takeAudioCall(intent, getSipAudioCallListener());
			((MasterActivity) context).openIncomingCallDialog();
		} catch (SipException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
