package com.willwhitney.jane;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnUtteranceCompletedListener;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;

public class JaneService extends Service implements OnUtteranceCompletedListener {
    private NotificationManager mNM;
    public static String WIKISERVER = "4jkn.localtunnel.com";
	MediaButtonIntentReceiver receiver;
	AudioManager am;
	ComponentName mediaButtonResponder;
	private TextToSpeech tts;
	JaneState state = JaneState.NONE;
	PowerManager powerManager;
	WakeLock lock;
	public static JaneService instance;
//	Yelp yelp;
	Gson gson = new Gson();

	public static LocalBroadcastManager localBroadcastManager;
	public static BroadcastReceiver localChatReceiver;

    // Unique Identification Number for the Notification.
    // We use it on Notification start, and to cancel it.
    private int NOTIFICATION = 19935;

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

        am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mediaButtonResponder = new ComponentName(getPackageName(), MediaButtonIntentReceiver.class.getName());
        am.registerMediaButtonEventReceiver(mediaButtonResponder);

        powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        lock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "JaneLock");
        lock.acquire(10 * 60 * 1000);

        localBroadcastManager = LocalBroadcastManager.getInstance(this);
        localChatReceiver = new ChatReceiver(this);


        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

//         Display a notification while Jane is awake.
        showNotification();
    }

    public void speak(String words) {
    	HashMap<String, String> speechParams = new HashMap<String, String>();
    	speechParams.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "WORDS");
    	tts.speak(words, TextToSpeech.QUEUE_FLUSH, speechParams);
    }

    public void handleSpeech(List<String> matches) {
    	String response = matches.get(0);
    	if (response.startsWith("talk to")) { 					// talk to <person>
    		String name = response.split(" ")[2].toLowerCase();
    		Intent newActive = new Intent(ChatService.SET_ACTIVE_CHAT);
    		newActive.putExtra("name", name);
    		localBroadcastManager.sendBroadcast(newActive);
    	} else {
    		Intent chat = new Intent(ChatService.CHAT_RESPONSE);
        	chat.putExtra("response", response);
        	localBroadcastManager.sendBroadcast(chat);
    	}
    }

    @Override
	public void onUtteranceCompleted(String utteranceId) {
    	Log.d("Jane", "Some utterance was completed with id " + utteranceId);
//    	Log.d("Jane", "I am in state " + state);
    	Log.d("Jane", "Passing this to utteranceCompletedThreadsafe...");

    	Intent utteranceCompletedIntent = new Intent(this, JaneService.class);
    	utteranceCompletedIntent.putExtra("utterance_completed", true);
        startService(utteranceCompletedIntent);
	}

    public void utteranceCompletedThreadsafe() {
    	Log.d("Jane", "Received startService in utteranceCompletedThreadsafe");
//    	switch (state) {
//			case NONE:
//				break;
//			case AWAITING_NOTE:
//				listen();
//				break;
//			case CHATTING:
//				break;
//    	}
    }

//    public void playPause() {
//    	Intent playPauseIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
//    	KeyEvent playPauseButton = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE);
//    	playPauseIntent.putExtra(Intent.EXTRA_KEY_EVENT, playPauseButton);
//    	Log.d("Jane", playPauseIntent.toString());
//    	sendBroadcast(playPauseIntent);
//    }

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
        mNM.cancel(NOTIFICATION);

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
        mNM.notify(NOTIFICATION, notification);
    }

//    private class WikiFetcher extends AsyncTask<String, Void, String> {
//		@Override
//		protected String doInBackground(String... params) {
//			try {
//				URI uri = new URI(
//						"http",
//						WIKISERVER,
//						"/" + params[0],
//						null);
//				Log.d("Jane", uri.toString());
//				Log.d("Jane", uri.toASCIIString());
//				String result = WebClient.getURLContents(uri.toASCIIString());
//				Log.d("Jane", result);
//				return result;
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//			return null;
//		}
//		@Override
//		protected void onPostExecute(String description) {
//			if (description == null || description.trim().length() == 0) {
//				speak("I don't know anything about that.");
//			} else {
//				speak(description);
//			}
//		}
//	}

//    private class YelpSearcher extends AsyncTask<String, Void, String> {
//		@Override
//		protected String doInBackground(String... params) {
//			if (yelp == null) {
//				Log.d("Jane", "Initializing a new Yelp inside YelpSearcher.");
//				 yelp = new Yelp("u75O1_PBhDnokFNF_mSqXQ", "_Izw_4AjCnKLhdOOcksJhdAQvpc",
//							"qAAd_MNlDka6JvpQXaUPUfH55wXkFCuS", "zZ4VPde4P6YX_BnZPVLMHhFZM2E");
//			}
//			LocationManager locManager = (LocationManager) JaneService.this.getSystemService(Context.LOCATION_SERVICE);
//			String locationProvider = LocationManager.NETWORK_PROVIDER;
//			Location loc = locManager.getLastKnownLocation(locationProvider);
//			String json = yelp.search(params[0], loc.getLatitude(), loc.getLongitude());
//			Log.d("Jane", json);
//			return json;
//		}
//		@Override
//		protected void onPostExecute(String json) {
//			YelpSearch search = gson.fromJson(json, YelpSearch.class);
//
//			String toSpeak = "";
//			int i = 0;
//			for (Business b : search.businesses) {
//				if (i > 4) {
//					break;
//				}
//				toSpeak += " " + b.name + ".";
//				i++;
//			}
//			Log.d("Jane", toSpeak);
//			speak(toSpeak);
//		}
//    }

//    private class YelpInitializer extends AsyncTask<Void, Void, Yelp> {
//		@Override
//		protected Yelp doInBackground(Void... params) {
//			Yelp y = new Yelp("u75O1_PBhDnokFNF_mSqXQ", "_Izw_4AjCnKLhdOOcksJhdAQvpc",
//					"qAAd_MNlDka6JvpQXaUPUfH55wXkFCuS", "zZ4VPde4P6YX_BnZPVLMHhFZM2E");
//			return y;
//		}
//		@Override
//		protected void onPostExecute(Yelp y) {
//			yelp = y;
//		}
//    }




}