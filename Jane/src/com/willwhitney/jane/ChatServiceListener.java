package com.willwhitney.jane;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.packet.Message;

import android.content.Intent;
import android.util.Log;

public class ChatServiceListener implements ChatManagerListener, MessageListener  {

	private JaneService service;

	public ChatServiceListener(JaneService service) {
		this.service = service;
	}

	@Override
	public void chatCreated(Chat chat, boolean local) {
		if(service.activeChat == null) {
			service.activeChat = chat;
			Log.i("Chat", "New active chat with " + chat.getParticipant());
		}

		/*Intent newChat = new Intent(ChatService.NEW_CHAT);
		newChat.putExtra("participant", chat.getParticipant());
		sendLocalIntent(newChat);*/

//		try {
//			chat.sendMessage("Using a dodgy voice client. Misstatements will be frequent.");
//		} catch (XMPPException e) {
//			Log.e("Chat", "Error sending the automatic preface message.");
//		}

		chat.addMessageListener(this);
	}

	@Override
	public void processMessage(Chat chat, Message msg) {
		if(service.activeChat.equals(chat)) {
			Intent newMessage = new Intent(JaneService.NEW_MESSAGE);
			newMessage.putExtra("participant", chat.getParticipant());
			newMessage.putExtra("xmppchat", msg.getBody());
			sendLocalIntent(newMessage);
		} else {
			String interruptEmail = chat.getParticipant();
			interruptEmail = interruptEmail.substring(0, interruptEmail.indexOf("/"));

			String interruptName = service.getNameForEmail(interruptEmail);

			StringBuilder sb = new StringBuilder();
			sb.append(interruptName);
			sb.append(" says ");
			sb.append(msg.getBody());

			Log.i("Chat", "Interrupting msg: " + sb.toString());

			Intent interruptMsg = new Intent(JaneService.NEW_MESSAGE);
			interruptMsg.putExtra("participant", interruptName);
			interruptMsg.putExtra("xmppchat", sb.toString());
			sendLocalIntent(interruptMsg);
		}
	}

	private void sendLocalIntent(Intent intent) {
		service.localBroadcastManager.sendBroadcast(intent);
	}

}
