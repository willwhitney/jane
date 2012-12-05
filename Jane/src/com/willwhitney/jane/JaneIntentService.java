package com.willwhitney.jane;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.support.v4.app.NotificationCompat;

public class JaneIntentService extends IntentService {
	private NotificationManager mNM;
	MediaButtonIntentReceiver receiver;
	AudioManager am;
	ComponentName mediaButtonResponder;
	private int NOTIFICATION = 19935;

	public JaneIntentService(String name) {
		super(name);

		mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

		am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mediaButtonResponder = new ComponentName(getPackageName(), MediaButtonIntentReceiver.class.getName());
        am.registerMediaButtonEventReceiver(mediaButtonResponder);

	}

	@Override
	protected void onHandleIntent(Intent intent) {
		// TODO Auto-generated method stub

	}

	private void showNotification() {
        Notification notification = new NotificationCompat.Builder(this)
        		.setSmallIcon(R.drawable.ic_launcher)
        		.setContentTitle("Jane")
        		.setContentText("Ready and waiting.")
        		.setOngoing(true)
        		.build();

        // Send the notification.
        mNM.notify(NOTIFICATION, notification);
    }

}
