package cc.gtank.bt;

import com.willwhitney.jane.JaneService;

import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BluetoothState extends BroadcastReceiver implements BluetoothProfile.ServiceListener {

	public static enum VOICE_STATE { DISCONNECTED, CONNECTING, CONNECTED };
	
	private VOICE_STATE state = VOICE_STATE.DISCONNECTED;
	private BluetoothHeadset bluetoothHeadset = null;
	
	@Override
	public void onServiceConnected(int profile, BluetoothProfile proxy) {
		if(profile == BluetoothProfile.HEADSET) {
			bluetoothHeadset = (BluetoothHeadset)proxy;
		}
	}

	@Override
	public void onServiceDisconnected(int profile) {
		if(profile == BluetoothProfile.HEADSET) {
			bluetoothHeadset = null;
		}
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d("Jane", "BS received intent " + intent.getAction());
		if(intent.getAction().equals(BluetoothHeadset.ACTION_AUDIO_STATE_CHANGED)) {
			Object state = intent.getExtras().get(BluetoothHeadset.EXTRA_STATE);
			if(state.equals(BluetoothHeadset.STATE_AUDIO_CONNECTING)) {
				state = VOICE_STATE.CONNECTING;
			} else if(state.equals(BluetoothHeadset.STATE_AUDIO_CONNECTED)) {
				state = VOICE_STATE.CONNECTED;
				Intent jane = new Intent(context, JaneService.class);
				jane.putExtra("bluetooth_connected", true);
				context.startService(jane);
			} else {
				state = VOICE_STATE.DISCONNECTED;
			}
		}
	}
	
	public boolean isAvailable() {
		return bluetoothHeadset != null;
	}
	
	public BluetoothHeadset getProxy() {
		return bluetoothHeadset;
	}
	
	public VOICE_STATE getVoiceState() {
		return state;
	}

}
