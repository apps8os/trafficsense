package org.apps8os.trafficsense.first;


import android.os.Bundle;
import android.os.Vibrator;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

import org.apps8os.contextlogger.android.integration.MonitoringFrameworkAgent;
import org.apps8os.trafficsense.first.GmailReader.EmailException;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class MainActivity extends Activity {
	
	private PebbleCommunication mPebbleCommunication;
	
	Resources mRes;
	String emailContent;
	Route objectRoute = new Route();
	String mJourneyText;
	JourneyParser mJourneyParser;
	// TODO: change this to the format agreed by Javier & Atte
	String mStatusMessage;
	MyReceiver mRecv;
	
	private class MyReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context arg0, Intent arg1) {
			System.out.println("DBG MyReceiver onReceive");
			TextView view = (TextView) findViewById(R.id.textView3);
			view.setText("alarm fired");
			Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
			vibrator.vibrate(250);
		}
		
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		mRes = getResources();
		mJourneyText = new String("");
		mJourneyParser = new JourneyParser();
		mRecv = new MyReceiver();

		// Start ContextLogger3
		// Get instance of MonitoringFrameworkAgent
		MonitoringFrameworkAgent mfAgent = MonitoringFrameworkAgent.getInstance();

		// Start Monitoring Framework using an instance of android.content.Context
		mfAgent.start(this);
		mPebbleCommunication = new PebbleCommunication(getApplicationContext());
		mPebbleCommunication.startAppOnPebble();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		IntentFilter filter = new IntentFilter("myaction");
		registerReceiver(mRecv, filter);
	}
	
	@Override
	protected void onPause() {
		unregisterReceiver(mRecv);
		super.onPause();
	}
	@Override
	protected void onDestroy() {
		
		// Stop ContextLogger3
		// Get instance of MonitoringFrameworkAgent
		MonitoringFrameworkAgent mfAgent = MonitoringFrameworkAgent.getInstance();
		// Stop Monitoring Framework
		mfAgent.stop(this);
		
		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	public void onClick_fetch(View v) {
		System.out.println("DBG onClick_fetch");
		final TextView textview = (TextView) findViewById(R.id.textView1);
		new Thread(new Runnable(){
			public void run(){
				Email email = new Email();
				GmailReader reader = new GmailReader();

				try {
					// TODO: hard-coded credentials
					reader.initMailbox("trafficsense.aalto@gmail.com","ag47)h(58P");
					email = reader.getNextEmail();	  	
				} catch (EmailException e) {
					textview.setText(e.getMessage());
					mJourneyText = "";
				}
				mJourneyText = email.getContent();
				textview.post(new Runnable(){
					public void run(){
						if(mJourneyText != null){
							textview.setText(mJourneyText);
						}
						else{
							textview.setText("Unable to retrieve journey text");
						}
					}
				});
			}
		}).start();
		
		
	}
	
    public void onClick_parse(View v) {
    	System.out.println("DBG onClick_parse");
    	TextView view = (TextView) findViewById(R.id.textView2);
    	/**
    	// TODO
    	// read journey text from assets/ line-by-line and put them into a long
    	// string with line breaks and then parse them line-by-line (done in parseString() )
    	// kind of redundant work. but, for the moment...
    	StringBuilder buf = new StringBuilder();
    	try {
    		InputStream journeyFile =
    				getAssets().open(mRes.getString(R.string.hard_coded_json));
    		BufferedReader rd = new BufferedReader(new InputStreamReader(journeyFile));
    		String str;
    		while ((str = rd.readLine()) != null) {
    			buf.append(str+"\n");
    		}
    		rd.close();
    	} catch (IOException ex) {
    		Log.d(getLocalClassName(), "IOEx", ex);
    	}
    	
    	mJourneyParser.parseString(buf.toString());
    	**/
    	mJourneyParser.parseString(mJourneyText);
    	view.setText(mJourneyParser.getJsonText());
	}

    public void onClick_activate(View v) {
    	System.out.println("DBG onClick_activate");
    	JSONconverter();
    	
    	String input = objectRoute.getDepartureTime();
	    Date date = new Date();
		try {
			date = new SimpleDateFormat("EEEE dd.M.yyyy kk:mm", Locale.ENGLISH).parse(input);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("date:" + date);
	  
    	TextView view = (TextView) findViewById(R.id.textView3);

    	view.setText("waiting for alarm");
    	// sets an alarm which expires x seconds later.
    	int x = 5;
    	int reqCode = 0;
    	// TODO This is the trick: Intent action must be set like this ...
    	Intent intent = new Intent("myaction");
    	// TODO: flags should be something but zero, check docs.
    	// TODO: if we send the Intent to a BroadcastReceiver in this Activity,
    	//        which is able to update the UI elements easily, then the Context
    	//        here should be the Activity, not the whole application.
		PendingIntent pendingIntent = PendingIntent.getBroadcast(
				this, reqCode, intent, PendingIntent.FLAG_CANCEL_CURRENT);
   		AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
   		alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()
   				+ (x * 1000), pendingIntent);
    }
    
    public void onClick_send(View v) {
    	System.out.println("DBG onClick_send");
    	TextView view = (TextView) findViewById(R.id.textView4);
    	// TODO: Send the actual stops received from email (Atte)
    	// Now: send stops with dummy data
    	mPebbleCommunication.sendStop("Kemisti", "E1234", "13:40", 0);
    	mPebbleCommunication.sendStop("Alva", "E1235", "13:41", 1);
    	mPebbleCommunication.sendStop("Konemies", "E1236", "13:42", 2);
	}
    
    
    
    public void JSONconverter(){
		String jsonstring = null;
		JSONObject json =  null;
	    try {
	        InputStream is = getAssets().open("sample.js");
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
	    
	    objectRoute.FillRoute(json);
	    
	    	    
	}

}
