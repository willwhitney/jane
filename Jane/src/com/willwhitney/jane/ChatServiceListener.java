package com.willwhitney.jane;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ChatServiceListener extends BroadcastReceiver implements ChatManagerListener, MessageListener  {
	
	private ChatService service;
	
	public ChatServiceListener(ChatService service) {
		this.service = service;
	}

	@Override
	public void chatCreated(Chat chat, boolean local) {		
		if(service.activeChat == null) {
			service.activeChat = chat;
			Log.i("Chat", "New active chat with " + chat.getParticipant());
			
			/*Intent newChat = new Intent(ChatService.NEW_CHAT);
			newChat.putExtra("participant", chat.getParticipant());
			sendLocalIntent(newChat);*/
		
			chat.addMessageListener(this);
		}
	}

	@Override
	public void processMessage(Chat chat, Message msg) {
		if(service.activeChat.equals(chat)) {
			if(msg.getBody().equalsIgnoreCase("/bye")) {
				service.activeChat = null;
				chat.removeMessageListener(this);
			} else {
				Intent newMessage = new Intent(ChatService.NEW_MESSAGE);
				newMessage.putExtra("participant", chat.getParticipant());
				newMessage.putExtra("xmppchat", msg.getBody());
				sendLocalIntent(newMessage);
			}			
		} else {
			String interruptName = service.getNameForEmail(chat.getParticipant());
			
			StringBuilder sb = new StringBuilder();
			sb.append(interruptName);
			sb.append(" says ");
			sb.append(msg.getBody());
			
			Log.i("Chat", "Interrupting msg: " + sb.toString());
			
			Intent interruptMsg = new Intent(ChatService.NEW_MESSAGE);
			interruptMsg.putExtra("participant", interruptName);
			interruptMsg.putExtra("xmppchat", sb.toString());
			sendLocalIntent(interruptMsg);
		}
	}

	/*
	 * Listening for ChatService.CHAT_RESPONSE and ChatService.SET_ACTIVE_CHAT
	 * See ChatService.loginCallback
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
		String type = intent.getAction();
		
		if(type.equals(ChatService.SET_ACTIVE_CHAT)) {
			String name = (String)intent.getExtras().get("name");
			service.setActiveChatByName(name);
		} else if(type.equals(ChatService.CHAT_RESPONSE)) {
			String response = intent.getExtras().getString("response");
			try {
				Log.i("Chat", "Sending message: " + response);
				service.activeChat.sendMessage(response);
			} catch (XMPPException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void sendLocalIntent(Intent intent) {
		JaneService.localBroadcastManager.sendBroadcast(intent);
	}

}
