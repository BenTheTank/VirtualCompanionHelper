package de.virtualcompanion.helper;

import java.text.ParseException;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.sip.SipAudioCall;
import android.net.sip.SipException;
import android.net.sip.SipManager;
import android.net.sip.SipProfile;
import android.net.sip.SipRegistrationListener;
import android.os.Binder;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Process;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class SipService extends Service {	
	
	// Service Stuff
	//private Looper mServiceLooper;
	//private ServiceHandler mServiceHandler;
	NotificationManager notificationManager = null;
	Notification notification = null;
	private static final int NOTIFICATION_ID = 1;
	
	private final static String EXTRA_MESSAGE_PREFS = "PREFERENCES";
	private final static int EXTRA_PREFS = 94873;
	private final static int EXTRA_CALL = 38956;
	private final static int ACCOUNT = 0;
	private final static int BUDDY = 1;
	
	public final static String EXTRA_MESSAGE = "de.thnuernberg.virtualcompanion.MESSAGE";
	// Service Stuff done
	
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
	
	private IncomingCallReceiver receiver = null;
		
	private int registrationAttemptCount = 0;
	// Sip Stuff done
	
	private boolean sipRegistrated = false;
	private boolean inCall = false;
	
	/*
	 * ***************************************
	 * *				Binder				 *
	 * ***************************************
	 */
	// Binder given to clients
    private final IBinder mBinder = new SipBinder();
    
    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class SipBinder extends Binder {
    	SipService getService() {
            // Return this instance of LocalService so clients can call public methods
            return SipService.this;
        }
    }
    
    
    /*
     * ****************************************
     * *			SIP-Service 			  *
     * ****************************************
     */
	
    /*
     * Florian Zorn: This is probably not useful for us
     */
    /*
	// Handler that receives messages from the thread
	  private final class ServiceHandler extends Handler {
	      public ServiceHandler(Looper looper) {
	          super(looper);
	      }
	      @Override
	      public void handleMessage(Message msg) {
	          // Normally we would do some work here, like download a file.
	          // For our sample, we just sleep for 5 seconds.
	          long endTime = System.currentTimeMillis() + 5*1000;
	          while (System.currentTimeMillis() < endTime) {
	              synchronized (this) {
	                  try {
	                      wait(endTime - System.currentTimeMillis());
	                  } catch (Exception e) {
	                  }
	              }
	          }
	          // Stop the service using the startId, so that we don't stop
	          // the service in the middle of handling another job
	          stopSelf(msg.arg1);
	      }
	  }
	  */
	  
	  
	  @Override
	  public void onCreate() {
	    // Start up the thread running the service.  Note that we create a
	    // separate thread because the service normally runs in the process's
	    // main thread, which we don't want to block.  We also make it
	    // background priority so CPU-intensive work will not disrupt our UI.
	    HandlerThread thread = new HandlerThread("ServiceStartArguments", Process.THREAD_PRIORITY_BACKGROUND);
	    thread.start();
	    
	    // Get the HandlerThread's Looper and use it for our Handler 
	    //mServiceLooper = thread.getLooper();
	    //mServiceHandler = new ServiceHandler(mServiceLooper);
	    
	    
	    //notification = new Notification(R.drawable.ic_launcher, getString(R.string.app_name), System.currentTimeMillis());
	    
	    // Checking the preferences if there is a username and password
	    checkPreferences();
	    
	    startForegroundService();
	    initializeSip();
	  }
	  
	  @Override
	  public int onStartCommand(Intent intent, int flags, int startId) {
	      //Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show();

	      // For each start request, send a message to start a job and deliver the
	      // start ID so we know which request we're stopping when we finish the job
	      //Message msg = mServiceHandler.obtainMessage();
	      //msg.arg1 = startId;
	      //mServiceHandler.sendMessage(msg);
	      
	      // If we get killed, after returning from here, restart
	      return START_STICKY;
	  }
	  
	  @Override
	  public void onDestroy() {
	    //Toast.makeText(this, "service done", Toast.LENGTH_SHORT).show();
	    
	    closeLocalProfile();
	    
	    stopForeground(true);
	  }
	  
	  
	  @Override
	  public IBinder onBind(Intent intent) {
		  updateNotification("Bind");
		  return mBinder;
	  }
	 
	  
	  private void startForegroundService()	{
		  notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		  
		  // Sets an ID for the notification, so it can be updated
		  
		  // With this we generate a notification bar entry
		  // so our service doesn't get killed
		  notification = new NotificationCompat.Builder(this)
		    				.setSmallIcon(R.drawable.icon_greyscale)
		    				.setContentTitle(getString(R.string.app_name))
		    				.build();
		  //notificationManager.notify(NOTIFICATION_ID, notification);
		  startForeground(NOTIFICATION_ID, notification);
	  }
	  
	  private void updateNotification(String text)	{
		  notification = new NotificationCompat.Builder(this)
			.setSmallIcon(R.drawable.icon_greyscale)
			.setContentTitle(getString(R.string.app_name))
			.setContentText(text)
			.build();
		  notificationManager.notify(NOTIFICATION_ID, notification);
	  }
	  
	  
	  private void checkPreferences()	{
		  settings = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
		  String username = settings.getString("namePref", "");
		  String password = settings.getString("passPref", "");
		  String domain = settings.getString("domainPref", "");
		  /*
		  switch(choice)	{
		  case ACCOUNT:
			  if(username.length() == 0 || domain.length() == 0 || password.length() == 0){
				  Intent intent = new Intent(this, SipServiceHelperActivity.class);
				  //intent.putExtra(EXTRA_MESSAGE, EXTRA_MESSAGE_PREFS);
				  intent.putExtra(EXTRA_MESSAGE, EXTRA_PREFS);
				  intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				  startActivity(intent);
			  }
			  
			  break;
			  
		  default:
			  break;
		  }
		  */
	  }
	  
	  
	  /*
	     * ****************************************
	     * *		  SIP-Service Done 			  *
	     * ****************************************
	     */
	  
	  /*
	     * ****************************************
	     * *			SIP-Framework 			  *
	     * ****************************************
	     */

		
		private void initializeSip()	{
			/*
			// Specify an intent filter to receive calls
			sepcifieIntentFilter();
			*/
			
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
			receiver = new IncomingCallReceiver();
			registerReceiver(receiver, filter);
		}
		
		private void initializeManager()	{
			if(mSipManager == null)	{
				//mSipManager = SipManager.newInstance(getApplicationContext());
				mSipManager = SipManager.newInstance(this);
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
			
			//String domain = "bb-projects.de";
			
			// We read the username and password out of our preferences
			// so we are able to log into the sip server
			String username = settings.getString("namePref", "");
			String password = settings.getString("passPref", "");
			String domain = settings.getString("domainPref", "");
			
			/*
			if(username.length() == 0 || domain.length() == 0 || password.length() == 0){
				//TODO: not for the hardcoded 3 strings above, they are for testing purpose only.
				//finished application should have an UI for setting up username, domain and password!
				return;
			}
			*/
			
			try	{
				registrationAttemptCount++;
				
				SipProfile.Builder builder = new SipProfile.Builder(username, domain);
				builder.setPassword(password);
				localSipProfile = builder.build();
				
				Intent i = new Intent();
				i.setAction(ACTION_STRING);
				PendingIntent pi = PendingIntent.getBroadcast(this, 0, i, Intent.FILL_IN_DATA);			
				mSipManager.open(localSipProfile, pi, null);
				
				
				// This listener must be added AFTER manager.open is called,
	            // Otherwise the methods aren't guaranteed to fire.
				// -Google
				
				mSipManager.setRegistrationListener(localSipProfile.getUriString(), new SipRegistrationListener() {
	                public void onRegistering(String localProfileUri) {
	                    //updateStatus(callerContext.getString(R.string.sipStatusRegistering));
	                	//updateNotification(getString(R.string.sipStatusRegistering));
	                	updateNotification("onRegistering");
	                }

	                public void onRegistrationDone(String localProfileUri, long expiryTime) {
	                    //updateStatus(callerContext.getString(R.string.sipStatusRegistrationDone));
	                	//updateNotification(getString(R.string.sipStatusRegistrationDone));
	                	updateNotification("onRegisteringDone");
	                    registrationAttemptCount = 0;
	                    
	                    sipRegistrated = true;
	                }

	                public void onRegistrationFailed(String localProfileUri, int errorCode,
	                        String errorMessage) {
	                	// If registration fails we will try it xxx times again
	                	if(registrationAttemptCount < 100)	{
	                		//updateStatus(String.valueOf(registrationAttemptCount) + " . try...");
	                		updateNotification(String.valueOf(registrationAttemptCount) + " . try...");
	                		initializeManager();
	                	} else	{
	                		//updateStatus(callerContext.getString(R.string.sipStatusRegistrationFailed));
	                		//updateNotification(getString(R.string.sipStatusRegistrationFailed));
	                		updateNotification("onRegisteringFailed");
	                	}
	                	
	                	sipRegistrated = false;
	                }
	            });
			} catch (ParseException e) {
				Log.d(TAG, "ParseException in method: sipProfileCreator()", e);
			} catch (SipException e) {
				//updateStatus("Connection Error");
				updateNotification("Connection Error");
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
			//EditText dialInput = (EditText) callerActivity.findViewById(R.id.dialInput);
			//peerSipAddress = dialInput.getText().toString() +"@" + localSipProfile.getSipDomain();
			String buddyName = settings.getString("nameBuddyPref", "1001");
			peerSipAddress = buddyName +"@" + localSipProfile.getSipDomain();
			
			
			//updateStatus(peerSipAddress);
			
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
			/*
			if(audioCall.isInCall()){
				try	{
					audioCall.endCall();
					audioCall.close();
					audioCall = null;
				} catch(SipException e)	{
					//do something
				}
			}
			*/
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
				
				/*
				@Override
				public void onReadyToCall (SipAudioCall call)	{
					//TODO: Maybe this method is called when the caller is hanging up before we answer
					updateStatus("schlabba");
				}
				*/
				
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
			//SipProfile callerProfile = audioCall.getPeerProfile();
			//String callerName = callerProfile.getUserName();
			//String callerDomain = callerProfile.getSipDomain();
			
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
			
			//Intent intent = new Intent(callerContext, IncomingCallActivity.class);
			/*
			Intent i = new Intent(this, SipServiceHelperActivity.class);
			i.putExtra(EXTRA_MESSAGE, EXTRA_CALL);
			i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(i);
			*/
			
			/*
			try	{
				// This updates our TextView so we can see who is calling us
				//updateStatus(audioCall);
				
				audioCall.answerCall(30);
				audioCall.startAudio();
				audioCall.setSpeakerMode(true);
				//audioCall.toggleMute();
				
				
				//callerContext.startActivity(intent);
			} catch(Exception e)	{
				if(audioCall != null)	{
					audioCall.close();
				}
			}
			*/
			//answerCall();
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
}


