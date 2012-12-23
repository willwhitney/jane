package com.gtank.janexmpp;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;

import android.content.Context;
import android.content.Intent;


public class ChatMessageListener implements PacketListener {
	
	private Context context;
	
	public ChatMessageListener(Context context) {
		this.context = context;
	}

	@Override
	public void processPacket(Packet packet) {
		//PacketTypeFilter giving us only Messages
		Message msg = (Message)packet;
		String body = msg.getBody();
		Intent intent = new Intent("android.content.Intent.ACTION_SEND");
		intent.putExtra("xmppchat", body);
		context.sendBroadcast(intent);
	}

}
