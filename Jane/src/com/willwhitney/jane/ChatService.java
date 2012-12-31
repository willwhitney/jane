package com.willwhitney.jane;

import java.util.HashMap;
import java.util.Map;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.XMPPConnection;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

public class ChatService extends Service {
	
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
	
	protected Chat activeChat;

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startid) {
		
		String username = intent.getExtras().getString("username");
		String password = intent.getExtras().getString("password");
		uiMessenger = intent.getExtras().getParcelable("messenger");
		
		nameCache = new HashMap<String, String>();
		
		org.jivesoftware.smack.SmackAndroid.init(this);		
		
		LoginThread login = new LoginThread(username, password, this);
		login.start();		
		
		return START_STICKY;
	}
	
	public void loginCallback(android.os.Message result, XMPPConnection connection) {
		try {
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
	
	public void setActiveChatByName(String name) {
		Roster roster = connection.getRoster();
		for(RosterEntry entry : roster.getEntries()) {
			if(entry.getName().startsWith(name)) {
				activeChat = connection.getChatManager().createChat(entry.getUser(), null);
			}
		}
	}
	
	public String getNameForEmail(String email) {
		Log.i("Chat", "Getting name for " + email);
		if(nameCache.containsKey(email)) {
			return nameCache.get(email);
		} else {
			Roster roster = connection.getRoster();
			for(RosterEntry entry : roster.getEntries()) {
				if(entry.getUser().equals(email)) {
					String name = entry.getName();
					nameCache.put(email, name);
					return name;
				}
			}
			return email;
		}
	}

}
