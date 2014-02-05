package org.apps8os.trafficsense.first;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Vibrator;

public class MyBroadcastReceiver extends BroadcastReceiver {
	
	public MyBroadcastReceiver(PebbleUiController controller) {
		
	}
	
	private PebbleCommunication mPebbleCommunication;
	@Override
	public void onReceive(Context context, Intent intent) {
		// Vibrate the mobile phone
		Vibrator vibrator = (Vibrator) context
				.getSystemService(Context.VIBRATOR_SERVICE);
		vibrator.vibrate(2000);
		
		int segment = intent.getExtras().getInt(EXTRA_SEGMENT);
		...stop
		
		controller.
		
	}

	

}