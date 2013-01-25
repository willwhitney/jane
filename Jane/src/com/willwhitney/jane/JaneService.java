package com.willwhitney.jane;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Messenger;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.RemoteException;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnUtteranceCompletedListener;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

public class JaneService extends Service implements OnUtteranceCompletedListener {

	// service, voice, and notification variables
	private final static int JANE_NOTIFICATION_CODE = 0;
	public static JaneService instance;

    private NotificationManager notificationManager;
	private TextToSpeech tts;

	// Chat variables
	public final static int LOGIN_FAILED = 0;
	public final static int LOGIN_SUCCESSFUL = 1;
	public final static String NEW_MESSAGE = "jane.xmpp.NEW_MESSAGE";
	public final static String NEW_CHAT = "jane.xmpp.NEW_CHAT";
	public final static String SET_ACTIVE_CHAT = "jane.xmpp.SET_ACTIVE_CHAT";
	public final static String CHAT_RESPONSE = "jane.xmpp.CHAT_RESPONSE";

	private Messenger uiMessenger;
	private XMPPConnection connection;

	private ChatServiceListener chatServiceListener;
	private Map<String, String> nameCache;

	public static LocalBroadcastManager localBroadcastManager;
	public static BroadcastReceiver localChatReceiver;

	protected Chat activeChat;


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
    	Log.d("Jane", "Intent from onStartCommand: " + intent);
    	if (intent != null) {
    		if (intent.hasExtra("utterance_completed")) {
        		utteranceCompletedThreadsafe();
        		return START_STICKY;
        	} else if (intent.hasExtra("start_listening")) {
        		listen();
        		return START_STICKY;
        	}
    	}

    	instance = this;
        Log.i("Jane", "Received start id " + startId + ": " + intent);
        Log.d("Jane", "Service received start command.");

        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {

			@Override
				public void onInit(int status) {
			}

        });
        tts.setOnUtteranceCompletedListener(this);

        AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        ComponentName mediaButtonResponder = new ComponentName(getPackageName(), MediaButtonIntentReceiver.class.getName());
        am.registerMediaButtonEventReceiver(mediaButtonResponder);

        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        WakeLock lock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "JaneLock");
        lock.acquire(10 * 60 * 1000);

        localBroadcastManager = LocalBroadcastManager.getInstance(this);
        localChatReceiver = new ChatReceiver(this);

        String username = intent.getExtras().getString("username");
		String password = intent.getExtras().getString("password");
		uiMessenger = intent.getExtras().getParcelable("messenger");

		nameCache = new HashMap<String, String>();

		org.jivesoftware.smack.SmackAndroid.init(this);

		LoginThread login = new LoginThread(username, password, this);
		login.start();


        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void loginCallback(android.os.Message result, XMPPConnection connection) {
		try {
			switch(result.what) {
			case JaneService.LOGIN_FAILED:
				speak("Login failed.");
				break;
			case JaneService.LOGIN_SUCCESSFUL:
				speak("Login successful..");
				break;
			}
			uiMessenger.send(result);
		} catch (RemoteException re) {
			re.printStackTrace();
		}

		this.connection = connection;
		activeChat = null;
		chatServiceListener = new ChatServiceListener(this);

		connection.getChatManager().addChatListener(chatServiceListener);

		JaneService.localBroadcastManager.registerReceiver(JaneService.localChatReceiver,
				new IntentFilter(NEW_MESSAGE));
		JaneService.localBroadcastManager.registerReceiver(chatServiceListener,
				new IntentFilter(CHAT_RESPONSE));
		JaneService.localBroadcastManager.registerReceiver(chatServiceListener,
				new IntentFilter(SET_ACTIVE_CHAT));
	}

	//TODO: figure out what determines the presence of a RosterEntry's name field
	//TODO: Intent-based race condition. probably shouldn't send message if "tell X" fails
	public void setActiveChatByName(String name) {
		Roster roster = connection.getRoster();
		for(RosterEntry entry : roster.getEntries()) {
			String potentialName = entry.getName();
			Log.i("Chat", "Checking desired recipient: " + name + " against: " + potentialName);
			if(potentialName != null && potentialName.regionMatches(true, 0, name, 0, name.length())) {
				Log.i("Chat", "Setting active chat to " + potentialName);
				speak("Now talking to " + potentialName);
				activeChat = connection.getChatManager().createChat(entry.getUser(), null);
				return;
			} else if(entry.getUser().contains(name)) { //maybe emails will be ok...
				Log.i("Chat", "Setting active chat to " + entry.getUser());
				speak("Now talking to " + entry.getUser());
				activeChat = connection.getChatManager().createChat(entry.getUser(), null);
				return;
			}
		}
		Log.i("Chat", "No friend matches " + name);
		speak("Sorry, but I couldn't find a friend named " + name);
	}

	public String getNameForEmail(String email) {
		Log.i("Chat", "Getting name for " + email);
		if(nameCache.containsKey(email)) {
			return nameCache.get(email);
		} else {
			Roster roster = connection.getRoster();
			for(RosterEntry entry : roster.getEntries()) {
				Log.i("Chat", entry.getUser() + "," + entry.getName());
				if(entry.getUser().equals(email)) {
					String name = entry.getName();
					Log.i("Chat", "Got name for " + email + " as " + name);
					if(name == null || name.equals("")) {
						return email;
					} else {
						nameCache.put(email, name);
						return name;
					}
				}
			}
			return email;
		}
	}

    @Override
    public void onCreate() {
        notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

//      Display a notification while Jane is awake.
        showNotification();
    }

    public void speak(String words) {
    	HashMap<String, String> speechParams = new HashMap<String, String>();
    	speechParams.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "WORDS");
    	tts.speak(words, TextToSpeech.QUEUE_FLUSH, speechParams);
    }

    public void handleSpeech(List<String> matches) {
    	String response = matches.get(0);
    	if (response.startsWith("talk to")) { 					// "talk to <person>"
    		String name = response.split(" ")[2].toLowerCase();
    		setActiveChatByName(name);
    	} else {												// sends "<message>" to the active chat
    		if (activeChat == null) {
    			Log.i("Chat", "Attempted to send chat with no active recipient.");
    			speak("No chat active. Start one by saying talk to , then a name.");
    		} else {
    			try {
        			Log.i("Chat", "Sending message to " + activeChat.getParticipant() + ": " + response);
        			activeChat.sendMessage(response);
        		} catch (XMPPException e) {
        			e.printStackTrace();
        		}
    		}
    	}
    }

    @Override
	public void onUtteranceCompleted(String utteranceId) {
    	Log.d("Jane", "Some utterance was completed with id " + utteranceId);
    	Log.d("Jane", "Passing this to utteranceCompletedThreadsafe...");

    	Intent utteranceCompletedIntent = new Intent(this, JaneService.class);
    	utteranceCompletedIntent.putExtra("utterance_completed", true);
        startService(utteranceCompletedIntent);
	}

    public void utteranceCompletedThreadsafe() {
    	Log.d("Jane", "Received startService in utteranceCompletedThreadsafe");
    }

    public void listen() {

    	Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
    	intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
    	intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, "com.willwhitney.jane");

    	SpeechRecognizer recognizer = SpeechRecognizer.createSpeechRecognizer(this.getApplicationContext());
    	RecognitionListener listener = new RecognitionListener() {

    		@Override
    	    public void onResults(Bundle results) {
    	        ArrayList<String> voiceResults = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
    	        if (voiceResults == null) {
    	            Log.e("Jane", "No voice results");
    	        } else {
    	            Log.d("Jane", "Printing matches: ");
    	            for (String match : voiceResults) {
    	                Log.d("Jane", match);
    	            }
    	        }
    	        JaneService.this.handleSpeech(voiceResults);
    	    }

    	    @Override
    	    public void onReadyForSpeech(Bundle params) {
    	        Log.d("Jane", "Ready for speech");
    	    }

    	    @Override
    	    public void onError(int error) {
    	        Log.d("Jane",
    	                "Error listening for speech: " + error);
    	    }

    	    @Override
    	    public void onBeginningOfSpeech() {
    	        Log.d("Jane", "Speech starting");
    	    }

			@Override
			public void onBufferReceived(byte[] buffer) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onEndOfSpeech() {
				Log.d("Jane", "Speech ended.");

			}

			@Override
			public void onEvent(int eventType, Bundle params) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onPartialResults(Bundle partialResults) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onRmsChanged(float rmsdB) {
				// TODO Auto-generated method stub

			}
    	};
    	recognizer.setRecognitionListener(listener);
    	recognizer.startListening(intent);
    }



    @Override
    public void onDestroy() {
        // Cancel the persistent notification.
        notificationManager.cancel(JANE_NOTIFICATION_CODE);

        // Tell the user we stopped.
        Toast.makeText(this, "Jane service stopped.", Toast.LENGTH_SHORT).show();
        super.onDestroy();
    }

    /**
     * Show a notification while this service is running.
     */
    private void showNotification() {
        Notification notification = new NotificationCompat.Builder(this)
        		.setSmallIcon(R.drawable.ic_launcher)
        		.setContentTitle("Jane")
        		.setContentText("Ready and waiting.")
        		.setOngoing(true)
        		.build();


        // Send the notification.
        notificationManager.notify(JANE_NOTIFICATION_CODE, notification);
    }
}