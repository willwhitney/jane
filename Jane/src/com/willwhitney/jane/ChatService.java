package com.willwhitney.jane;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.PacketTypeFilter;

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
	
	private Messenger uiMessenger;
	private XMPPConnection connection;

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
		
		connection.addPacketListener(new ChatMessageListener(), 
				new PacketTypeFilter(org.jivesoftware.smack.packet.Message.class));
		
		sendHelloMsg();
	}
	
	private void sendHelloMsg() {	
		Jane.localBroadcastManager.registerReceiver(new TestChatReceiver(), new IntentFilter("android.content.Intent.ACTION_SEND"));
		Chat chat = connection.getChatManager().createChat("george.tankersley@gmail.com", new MessageListener() {
		    public void processMessage(Chat chat, org.jivesoftware.smack.packet.Message message) {
		        Log.i("Chat", "Received message: " + message.getBody());
		    }
		});
		
		try {
			chat.sendMessage("Hello, world");
		} catch (XMPPException e) {
			e.printStackTrace();
		}
	}

}
