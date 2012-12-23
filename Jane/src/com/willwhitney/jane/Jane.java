package com.willwhitney.jane;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class Jane extends Activity implements OnClickListener {

	public final int RECEIVED_SPEECH_CODE = 0;
	Button speakButton;
	public static Jane instance;
	MediaButtonIntentReceiver receiver;
	AudioManager am;
	ComponentName mediaButtonResponder;
	public static String foursquareToken;
	public static final int FOURSQUARE_FETCHER_CODE = 10;
	
	final Messenger uiMessenger = new Messenger(new UIMessageHandler(this));
	public static LocalBroadcastManager localBroadcastManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.activity_main);
		Button loginButton = (Button)findViewById(R.id.buttonLogin);
		loginButton.setOnClickListener(this);
		
        instance = this;

        speakButton = (Button) findViewById(R.id.speakbutton);

        speakButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				listen();
			}
		});
        
        localBroadcastManager = LocalBroadcastManager.getInstance(this);

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
    
	@Override
	public void onClick(View loginScreen) {
		EditText username = (EditText)findViewById(R.id.editUsername);
		EditText password = (EditText)findViewById(R.id.editPassword);
		username.setText("wfwhitney.test@gmail.com");
		password.setText("willtest");
		String user = username.getText().toString();
		String pass = password.getText().toString();
		
		if(user.equals("") || pass.equals("")) {
			Toast.makeText(this, "Please enter a username and password.", Toast.LENGTH_SHORT).show();
			return;
		}
		
		Intent startChatService = new Intent(this, ChatService.class);
		startChatService.putExtra("username", user);
		startChatService.putExtra("password", pass);
		startChatService.putExtra("messenger", uiMessenger);
		startService(startChatService);				
	}
    
	private static class UIMessageHandler extends Handler {
		private Context context;
		
		public UIMessageHandler(Context c) {
			context = c;
		}
		
		@Override
		public void handleMessage(Message msg) {
			switch(msg.what) {
			case ChatService.LOGIN_FAILED:
				Toast.makeText(context, "Login failed.", Toast.LENGTH_SHORT).show();
				break;
			case ChatService.LOGIN_SUCCESSFUL:
				Toast.makeText(context, "Login successful.", Toast.LENGTH_SHORT).show();
				break;
			default:
				super.handleMessage(msg);
			}
		}
	}

}
