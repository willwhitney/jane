package com.gtank.janexmpp;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener {
	
	final Messenger uiMessenger = new Messenger(new UIMessageHandler(this));
	public static LocalBroadcastManager localBroadcastManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		localBroadcastManager = LocalBroadcastManager.getInstance(this);
		
		AccountManager accountManager = AccountManager.get(this);
		Account[] janeAccounts = accountManager.getAccountsByType("com.gtank.janexmpp");
		
		if(janeAccounts.length == 0) {	
			//create new Jane account
			setContentView(R.layout.activity_main);
			Button loginButton = (Button)findViewById(R.id.buttonLogin);
			loginButton.setOnClickListener(this);
		} else if(janeAccounts.length > 1) {
			//TODO: select Jane account dialog
		} else {
			//TODO: account manager, start service
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
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
