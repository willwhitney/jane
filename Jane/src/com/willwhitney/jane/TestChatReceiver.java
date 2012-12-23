package com.willwhitney.jane;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class TestChatReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		String msg = intent.getExtras().getString("xmppchat");
		if(msg != null) {
			Log.i("Chat Received", msg);
		}
	}

}
