package com.willwhitney.jane;

import org.jivesoftware.smack.AndroidConnectionConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;

import android.os.Message;

public class LoginThread extends Thread {

	private String username, password;
	private Message result;
	private AndroidConnectionConfiguration config;
	private XMPPConnection connection;
	private JaneService callback;

	public LoginThread(String username, String password, JaneService callback) {
		this.username = username;
		this.password = password;
		this.callback = callback;
	}

	@Override
	public void run() {
		try {
			config = new AndroidConnectionConfiguration("talk.google.com", 5222, "gmail.com");
			config.setSASLAuthenticationEnabled(true);
			connection = new XMPPConnection(config);
			connection.connect();
			connection.login(username, password);
			result = android.os.Message.obtain(null, JaneService.LOGIN_SUCCESSFUL);
		} catch (XMPPException e) {
			result = android.os.Message.obtain(null, JaneService.LOGIN_FAILED);
		}

		callback.loginCallback(result, connection);
	}

}
