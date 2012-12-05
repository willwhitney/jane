package com.willwhitney.jane;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.net.Uri;
import android.os.AsyncTask;
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
import android.util.Log;
import android.util.Patterns;
import android.widget.Toast;

public class JaneService extends Service implements OnUtteranceCompletedListener {
    private NotificationManager mNM;
	MediaButtonIntentReceiver receiver;
	AudioManager am;
	ComponentName mediaButtonResponder;
	TextToSpeech tts;
	JaneState state = JaneState.NONE;
	PowerManager powerManager;
	WakeLock lock;
	public static JaneService instance;

    // Unique Identification Number for the Notification.
    // We use it on Notification start, and to cancel it.
    private int NOTIFICATION = 19935;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
    	Log.d("Jane", "Intent from onStartCommand: " + intent);
    	if (intent.hasExtra("utterance_completed")) {
    		utteranceCompletedThreadsafe();
    		return START_STICKY;
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


//        new XMLFetcher().execute("san francisco");

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

        // Display a notification while Jane is awake.
        showNotification();
    }

    public void speak(String words) {
    	HashMap<String, String> speechParams = new HashMap<String, String>();
    	speechParams.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "WORDS");
    	tts.speak(words, TextToSpeech.QUEUE_FLUSH, speechParams);
    }

    public void handleSpeech(List<String> matches) {
    	switch (state) {
    		case NONE:
    			for (String match : matches) {
            		if (match.equals("take a note")) {
            			state = JaneState.AWAITING_NOTE;
            			speak("Go ahead, sir.");
            			return;
            		} else if (match.startsWith("tell me about")) {
            			new XMLFetcher().execute(match.replaceFirst("tell me about *", ""));
            			return;
            		}
            	}
    			break;
    		case AWAITING_NOTE:
    			Log.d("Jane", "Took a note: " + matches.get(0));

    			Pattern emailPattern = Patterns.EMAIL_ADDRESS; // API level 8+
    			Account[] accounts = AccountManager.get(this).getAccounts();
    			String possibleEmail = "";
    			for (Account account : accounts) {
    			    if (emailPattern.matcher(account.name).matches()) {
    			        possibleEmail = account.name;
    			        break;
    			    }
    			}

    			Intent send = new Intent(Intent.ACTION_SENDTO);
    			String uriText;

    			uriText = "mailto:" + possibleEmail +
    			          "?subject=Note from Jane" +
    			          "&body=" + matches.get(0);
    			uriText = uriText.replace(" ", "%20");
    			Uri uri = Uri.parse(uriText);

    			send.setData(uri);
    			send = Intent.createChooser(send, "Send mail...");
    			send.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    			startActivity(send);

    			state = JaneState.NONE;
    			break;
    	}
    }

    @Override
	public void onUtteranceCompleted(String utteranceId) {
    	Log.d("Jane", "Some utterance was completed with id " + utteranceId);
    	Log.d("Jane", "I am in state " + state);
    	Log.d("Jane", "Running startService...");

    	Intent utteranceCompletedIntent = new Intent(this, JaneService.class);
    	utteranceCompletedIntent.putExtra("utterance_completed", true);
        startService(utteranceCompletedIntent);
	}

    public void utteranceCompletedThreadsafe() {
    	Log.d("Jane", "Received startService in utteranceCompletedThreadsafe");
    	switch (state) {
			case NONE:
				break;
			case AWAITING_NOTE:
				listen();
				break;
    	}
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
        mNM.cancel(NOTIFICATION);

        // Tell the user we stopped.
        Toast.makeText(this, "Jane service stopped.", Toast.LENGTH_SHORT).show();
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

    private class XMLFetcher extends AsyncTask<String, Void, String> {
		@Override
		protected String doInBackground(String... params) {
			try {
				String url = new URI(
						"http",
						"lookup.dbpedia.org",
						"/api/search.asmx/KeywordSearch",
						"QueryClass=place&QueryString=" + params[0],
						null).toASCIIString();
//				Document d = WebClient.loadXMLFromString(
//						WebClient.getURLContents(
//						"http://lookup.dbpedia.org/api/search.asmx/KeywordSearch?QueryClass=place&QueryString=" + params[0]));
				Document d = WebClient.loadXMLFromString(WebClient.getURLContents(url));
				NodeList nl = d.getElementsByTagName("Description");
				Node n = nl.item(0);
				return n.getTextContent();
//				Log.d("Jane", n.getTextContent());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}
		@Override
		protected void onPostExecute(String description) {
			speak(description);
		}
	}


}