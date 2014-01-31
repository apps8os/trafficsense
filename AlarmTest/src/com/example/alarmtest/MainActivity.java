package com.example.alarmtest;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends Activity {

	int reqCode=0;
	private ArrayList<Route> routeList = new ArrayList<Route>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
	}
	
	public void startAlert(View view) {
		EditText text = (EditText) findViewById(R.id.time);
		int i = 10;//Integer.parseInt(text.getText().toString());

		Intent intent = new Intent(this, MyBroadcastReceiver.class);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(
				this.getApplicationContext(), reqCode, intent, 0);

		AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
		alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()
				+ (i * 1000), pendingIntent);
		Toast.makeText(this, "Alarm set in " + i + " seconds",
				Toast.LENGTH_LONG).show();
	
		//JSONconverter();
	}


	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	public void JSONconverter(){
		String jsonstring = null;
		JSONObject json =  null;
	    try {
	        InputStream is = getAssets().open("sample2.js");
	        int size = is.available();
	        byte[] buffer = new byte[size];
	        is.read(buffer);
	        is.close();
	        jsonstring = new String(buffer, "UTF-8");
	    } 
	    catch (IOException ex) {
	        ex.printStackTrace();
	    }
	    
	    try {
			json =  new JSONObject(jsonstring);
		} 
	    catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    //System.out.println(json.toString());
	    Route objectRoute = new Route();
	    try {
	    	objectRoute.setDate(json.getString("date"));
	    	objectRoute.setStart(json.getString("start"));
	    	objectRoute.setDestination("dest");
	    	objectRoute.setArrivalTime("arrivalTime");
	    	
			//object.setDate(json.getString("startTime"));
			System.out.println(json.getJSONArray("segments").getJSONObject(0).getJSONArray("waypoints"));
			//System.out.println(json.get("startTime"));
			//System.out.println(object.getDate());
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    routeList.add(objectRoute);
	    
	    
	}

}
