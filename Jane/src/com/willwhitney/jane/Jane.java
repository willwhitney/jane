package com.willwhitney.jane;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class Jane extends Activity {

	public final int RECEIVED_SPEECH_CODE = 0;
	TextView text;
	Button speakButton;
	public static Jane instance;
	MediaButtonIntentReceiver receiver;
	AudioManager am;
	ComponentName mediaButtonResponder;
	public static String foursquareToken;
	public static final int FOURSQUARE_FETCHER_CODE = 10;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        instance = this;

        text = (TextView) findViewById(R.id.text);
        speakButton = (Button) findViewById(R.id.speakbutton);

        speakButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				listen();
			}
		});

        Log.d("Jane", "About to try to start a service...");
        Intent serviceIntent = new Intent(this, JaneService.class);
        startService(serviceIntent);

//        Intent getFoursquareToken = new Intent(this, FoursquareKeyFetcher.class);
//        startActivityForResult(getFoursquareToken, FOURSQUARE_FETCHER_CODE);

    }

    public void listen() {
		Intent listenIntent = new Intent(Jane.this, JaneService.class);
		listenIntent.putExtra("start_listening", true);
        startService(listenIntent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	if (requestCode == FOURSQUARE_FETCHER_CODE) {
    		if(resultCode == RESULT_OK){
    			foursquareToken = data.getStringExtra("result");
//    			Log.d("Jane", foursquareToken);
    		}
    	}
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    @Override
    public void onDestroy() {
    	super.onDestroy();
    }


    @Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
    	Log.d("Jane", "Key pressed: " + keyCode);
    	if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_HEADSETHOOK) {
    		listen();
    		return true;
    	}
    	return super.onKeyDown(keyCode, event);
    }

}
