package de.virtualcompanion.helper;

import java.text.ParseException;

import android.app.Activity;
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
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.WindowManager;
import android.widget.TextView;

/*** Listens also for incoming SIP calls and intercepts
*/

class Sip extends BroadcastReceiver	{
	private final static String TAG = "CLASS_SIP_VIRTUAL_COMPANION";
	private final static String ACTION_STRING = "android.VirtualCompanion.INCOMING_CALL";
	public final static int TIMEOUT = 30;
	public static final String PREFS_NAME = "preferences";
	
	private Context callerContext = null;
	private Activity callerActivity = null;
	
	private String peerSipAddress = null;
	
	public SipManager mSipManager = null;
	private SipProfile localSipProfile = null;
	public SipAudioCall audioCall = null;
	
		
	private int registrationAttemptCount = 0;	
	
	
	Sip(Context context)	{
		callerContext = context;
		callerActivity = (Activity) callerContext;
		
		// Specify an intent filter to receive calls
		sepcifieIntentFilter();
		
		
		// Prevent the screen from turning off
       callerActivity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
       
       initializeManager();
	}
	
	/**
	 * This method is part of the BroadcastReceiver functionality of Sip.class
	 */
	
	@Override
	public void onReceive(Context context, Intent intent)	{
		try	{
			/*
			SipAudioCall.Listener listener = new SipAudioCall.Listener()	{
				@Override
				public void onRinging(SipAudioCall call, SipProfile caller)	{
					try	{
						call.answerCall(TIMEOUT);
					} catch(Exception e)	{
						e.printStackTrace();
					}
				}
			};
			*/
			SipAudioCall.Listener listener = getSipAudioCallListener();
			
			// this sets the takeAudioCall object of Sip.class to incomingCall object
			audioCall = mSipManager.takeAudioCall(intent, listener);
			incomingCall();	// Florian Zorn: further handling inside of MainActivity is possible!
		} catch(Exception e)	{
			if(audioCall != null)	{
				audioCall.close();
			}
		}
	}
	
	
	public void close()	{
		if(audioCall != null)	{
			audioCall.close();
		}
		
		closeLocalProfile();
	}
	
	
	/*
	 * *************************
	 * *       Framework       *
	 * *************************
	 */
	
	public void initializeManager()	{
		if(mSipManager == null)	{
			//mSipManager = SipManager.newInstance(getApplicationContext());
			mSipManager = SipManager.newInstance(callerContext);
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
		
		String domain = "bb-projects.de";
		
		// We read the username and password out of our preferences
		// so we are able to log into the sip server
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(callerActivity);
		String username = settings.getString("pref_username", "");
		String password = settings.getString("pref_password", "");
		
		
		if(username.length() == 0 || domain.length() == 0 || password.length() == 0){
			//TODO: not for the hardcoded 3 strings above, they are for testing purpose only.
			//finished application should have an UI for setting up username, domain and password!
			return;
		}
		
		try	{
			registrationAttemptCount++;
			
			SipProfile.Builder builder = new SipProfile.Builder(username, domain);
			builder.setPassword(password);
			localSipProfile = builder.build();
			
			Intent i = new Intent();
			i.setAction(ACTION_STRING);
			PendingIntent pi = PendingIntent.getBroadcast(callerContext, 0, i, Intent.FILL_IN_DATA);			
			mSipManager.open(localSipProfile, pi, null);
			
			
			// This listener must be added AFTER manager.open is called,
           // Otherwise the methods aren't guaranteed to fire.
			// -Google
			
			mSipManager.setRegistrationListener(localSipProfile.getUriString(), new SipRegistrationListener() {
               public void onRegistering(String localProfileUri) {
                   //updateStatus(callerContext.getString(R.string.sipStatusRegistering));
               }

               public void onRegistrationDone(String localProfileUri, long expiryTime) {
                   //updateStatus(callerContext.getString(R.string.sipStatusRegistrationDone));
                   registrationAttemptCount = 0;
               }

               public void onRegistrationFailed(String localProfileUri, int errorCode,
                       String errorMessage) {
               	// If registration fails we will try it xxx times again
               	if(registrationAttemptCount < 100)	{
               		updateStatus(String.valueOf(registrationAttemptCount) + " . try...");
               		initializeManager();
               	} else	{
               		//updateStatus(callerContext.getString(R.string.sipStatusRegistrationFailed));
               	}
               }
           });
		} catch (ParseException e) {
			Log.d(TAG, "ParseException in method: sipProfileCreator()", e);
		} catch (SipException e) {
			updateStatus("Connection Error");
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
	
	
	/**
	 * Specifying an IntentFilter so we can receive calls
	 */
	public void sepcifieIntentFilter()	{
		// Specify an intent filter to receive calls
		IntentFilter filter = new IntentFilter();
		filter.addAction(ACTION_STRING);
		callerActivity.registerReceiver(this, filter);
	}
	
	/*
	 * ******************************
	 * *       Framework Done       *
	 * ******************************
	 */
	
	
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
		//EditText dialInput = (EditText) callerActivity.findViewById(R.id.dialInput);
		//peerSipAddress = dialInput.getText().toString() +"@" + localSipProfile.getSipDomain();
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(callerActivity);
		String buddyName = settings.getString("pref_buddyName", "");
		peerSipAddress = buddyName +"@" + localSipProfile.getSipDomain();
		
		
		updateStatus(peerSipAddress);
		
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
		if(audioCall.isInCall()){
			try	{
				audioCall.endCall();
				audioCall = null;
			} catch(SipException e)	{
				//do something
			}
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
			}
			
			/*
			@Override
			public void onReadyToCall (SipAudioCall call)	{
				//TODO: Maybe this method is called when the caller is hanging up before we answer
				updateStatus("schlabba");
			}
			*/
			
			@Override
			public void onCallEnded(SipAudioCall call){
				updateStatus("Ready");
				audioCall = null;
			}
			
			@Override
			public void onRinging(SipAudioCall call, SipProfile caller)	{
				try	{
					call.answerCall(TIMEOUT);
				} catch(Exception e)	{
					e.printStackTrace();
				}
			}
		};
		return sipAudioCallListener;
	}
	
	
	/**
	 * This method is called by IncomingCallReceiver class
	 * Here we handle what's happening if someone's calling us
	 */
	public void incomingCall(){
		//SipProfile callerProfile = audioCall.getPeerProfile();
		//String callerName = callerProfile.getUserName();
		//String callerDomain = callerProfile.getSipDomain();
		
		//Intent intent = new Intent(callerContext, IncomingCallActivity.class);
		
		try	{
			// This updates our TextView so we can see who is calling us
			updateStatus(audioCall);
			/*
			audioCall.answerCall(30);
			audioCall.startAudio();
			audioCall.setSpeakerMode(true);
			*/
			
			//callerContext.startActivity(intent);
		} catch(Exception e)	{
			if(audioCall != null)	{
				audioCall.close();
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
	
	/*
	 * *********************************
	 * *		Miscellaneous		   *
	 * *********************************
	 */
	
	
	public void updateStatus(final String status)	{
		/*
		callerActivity.runOnUiThread(new Runnable()	{
			public void run()	{
				TextView statusView = (TextView) callerActivity.findViewById(R.id.connectionStatusView);
				statusView.setText(status);
			}
		});
		*/
	}
	
	public void updateStatus(final SipAudioCall call)	{
		/*
		SipProfile peerProfile = call.getPeerProfile();
		final String callerUserName = peerProfile.getUserName() + "@" + peerProfile.getSipDomain();
		callerActivity.runOnUiThread(new Runnable()	{
			public void run()	{
				TextView statusView = (TextView) callerActivity.findViewById(R.id.connectionStatusView);
				statusView.setText(callerUserName);
			}
		});
		*/
	}
}
