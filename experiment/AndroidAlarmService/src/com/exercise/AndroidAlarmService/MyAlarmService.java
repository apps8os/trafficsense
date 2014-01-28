package com.exercise.AndroidAlarmService;

import android.app.IntentService;
import android.content.Intent;

public class MyAlarmService extends IntentService {

	public MyAlarmService() {
		super("IntentService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
        System.out.println("DBG alarm onHandleIntent "+System.currentTimeMillis());
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(ResponseReceiver.ACTION_RESP);
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        sendBroadcast(broadcastIntent);
    }
	
}




