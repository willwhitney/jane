package com.willwhitney.jane;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class RemoteControlReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d("Jane", "Media ");
		Jane.instance.listen();

//		if (Intent.ACTION_MEDIA_BUTTON.equals(intent.getAction())) {
//            KeyEvent event = (KeyEvent) intent .getParcelableExtra(Intent.EXTRA_KEY_EVENT);
//
//            if (event == null) {
//                return;
//            }
//
//            if (event.getAction() == KeyEvent.ACTION_DOWN) {
//                context.sendBroadcast(new Intent(Intent.ACTION_PLAYER_PAUSE));
//            }
//        }

	}

}
