package com.willwhitney.jane;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {

	TextToSpeech tts;
	public final int RECEIVED_SPEECH_CODE = 0;
	TextView text;
	Button speakButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        text = (TextView) findViewById(R.id.text);
        speakButton = (Button) findViewById(R.id.speakbutton);

        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {

			@Override
			public void onInit(int status) {
//				speak("Hello! I'm Jane.");
//				speak("Go fuck yourself.");
			}

        });
//        tts.setSpeechRate(0.95f);

        speakButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
	    	    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
	    	            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
	    	    intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Voice recognition demo!");
	    	    startActivityForResult(intent, RECEIVED_SPEECH_CODE);
			}
		});

    }

    public void speak(String words) {
    	tts.speak(words, TextToSpeech.QUEUE_ADD, null);
    }

    public void listen() {
    	Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
	    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
	            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
	    intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "I'm listening, sir.");
	    startActivityForResult(intent, RECEIVED_SPEECH_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == RECEIVED_SPEECH_CODE && resultCode == RESULT_OK) {
            // Populate the wordsList with the String values the recognition engine thought it heard
            ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            text.setText(matches.get(0));
            speak(matches.get(0));
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    @Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
    	Log.d("Jane", "Key pressed.");
    	if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
    		listen();
    		return true;
    	} else {
    		return super.onKeyDown(keyCode, event);
    	}
    }

}
