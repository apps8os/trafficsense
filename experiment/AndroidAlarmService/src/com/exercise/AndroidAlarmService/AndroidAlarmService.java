package com.exercise.AndroidAlarmService;

import java.util.Calendar;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class AndroidAlarmService extends Activity {

private PendingIntent pendingIntent;
private ResponseReceiver receiver;

 /** Called when the activity is first created. */
 @Override
 public void onCreate(Bundle savedInstanceState) {
     super.onCreate(savedInstanceState);
     setContentView(R.layout.main);
     
     IntentFilter filter = new IntentFilter(ResponseReceiver.ACTION_RESP);
     filter.addCategory(Intent.CATEGORY_DEFAULT);
     receiver = new ResponseReceiver();
     registerReceiver(receiver, filter);
 }
 
 @Override
 public void onDestroy() {
     this.unregisterReceiver(receiver);
     super.onDestroy();
 }
 
 public void onClick_start(View v) {
	  Intent myIntent = new Intent(AndroidAlarmService.this, MyAlarmService.class);
	  pendingIntent = PendingIntent.getService(AndroidAlarmService.this, 0, myIntent, 0);
      AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
	  Calendar calendar = Calendar.getInstance();
	  calendar.setTimeInMillis(System.currentTimeMillis());
	  calendar.add(Calendar.SECOND, 10);
	  alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
	  System.out.println("DBG alarm onClick_start");
	  Toast.makeText(AndroidAlarmService.this, "Start Alarm", Toast.LENGTH_LONG).show();
 }
 
 public void onClick_cancel(View v) {
	  AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
	  alarmManager.cancel(pendingIntent);
	  System.out.println("DBG alarm onClick_cancel");
      Toast.makeText(AndroidAlarmService.this, "Cancel!", Toast.LENGTH_LONG).show();
  }
 
}