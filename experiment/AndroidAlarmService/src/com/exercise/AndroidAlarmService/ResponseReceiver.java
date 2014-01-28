package com.exercise.AndroidAlarmService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class ResponseReceiver extends BroadcastReceiver{
	 public static final String ACTION_RESP = "MESSAGE_PROCESSED";
		   @Override
		    public void onReceive(Context context, Intent intent) {
			   System.out.println("DBG alarm onReceive");
			   Toast.makeText(context, "Message", Toast.LENGTH_LONG).show();
		   }
}
