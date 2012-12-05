package com.willwhitney.jane;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.KeyEvent;
import android.widget.Toast;

public class MediaButtonIntentReceiver extends BroadcastReceiver {


	@Override
	public void onReceive(Context context, Intent intent) {
//		Toast.makeText(context, "BUTTON PRESSED!", Toast.LENGTH_SHORT).show();
//		Log.d("Jane", "Some button was received by IntentReceiver");
	    String intentAction = intent.getAction();
	    if (!Intent.ACTION_MEDIA_BUTTON.equals(intentAction)) {
	        return;
	    }
	    KeyEvent event = (KeyEvent)intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
	    if (event == null) {
	        return;
	    }
	    int action = event.getAction();
	    if (action == KeyEvent.ACTION_DOWN) {
	    	JaneService.instance.listen();
	        Toast.makeText(context, "BUTTON PRESSED!", Toast.LENGTH_SHORT).show();
	    }
	    abortBroadcast();
	}
}