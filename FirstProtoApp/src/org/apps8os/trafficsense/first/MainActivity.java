package org.apps8os.trafficsense.first;


import android.os.Bundle;
import android.os.Vibrator;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

import org.apps8os.contextlogger.android.integration.MonitoringFrameworkAgent;
import org.apps8os.trafficsense.first.GmailReader.EmailException;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


public class MainActivity extends Activity {

	final public static String ACTION_ROUTE_EVENT = "trafficsense.RouteEventUpdateUi";
	final public static String ACTION_ROUTE_EVENT_EXTRA_MESSAGE = "trafficsense.RouteEventUpdateUi.Extras.Message";
	
	private PebbleCommunication mPebbleCommunication;
	private Route mRoute;
	private String mJourneyText;
	private Resources mRes;
	private JourneyParser mJourneyParser;
	private PebbleUiController mPebbleUi;
	private RouteServiceEventReceiver mRecv;
	
	
	private class RouteServiceEventReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			System.out.println("DBG RouteServiceEventReceiver onReceive");
			// TODO update UI here
			TextView view = (TextView) findViewById(R.id.textView4);
			if (intent.hasExtra(ACTION_ROUTE_EVENT_EXTRA_MESSAGE)) {
				view.setText(intent.getStringExtra(ACTION_ROUTE_EVENT_EXTRA_MESSAGE));
			}
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
		mRecv = new RouteServiceEventReceiver();
		mRoute = new Route();

		// Start ContextLogger3
		// Get instance of MonitoringFrameworkAgent
		MonitoringFrameworkAgent mfAgent = MonitoringFrameworkAgent.getInstance();

		// Start Monitoring Framework using an instance of android.content.Context
		mfAgent.start(this);
		mPebbleCommunication = new PebbleCommunication(getApplicationContext());
		mPebbleCommunication.startAppOnPebble();
		// mPebbleUi is initialized in onClick_activate()
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		IntentFilter filter = new IntentFilter(ACTION_ROUTE_EVENT);
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
	
	//called by button that fetches emails.
	//email address and password are hardcoded and the last received email is always gotten
	public void onClick_fetch(View v) {
		System.out.println("DBG onClick_fetch");
		final TextView textview = (TextView) findViewById(R.id.textView1);
		//start new thread because network activities cant run on main ui thread
		new Thread(new Runnable(){
			public void run(){
				//make email and gmailreader object. email is datacontainer and gmailreader 
				//the email from the account
				Email email = new Email();
				GmailReader reader = new GmailReader();

				try {
					// initialize the mailbox that gmail reader reads by
					//giving it the email address and password
					reader.initMailbox("trafficsense.aalto@gmail.com","ag47)h(58P");
					//get the next email. This is first time called so it gets the latest email
					email = reader.getNextEmail();	  	
				} catch (EmailException e) {
					textview.setText(e.getMessage());
					mJourneyText = "";
				}
				//get the email content
				mJourneyText = email.getContent();
				//cant manipulate ui from the thread this part of the code is running
				//so have to use the way shown below. 
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
    	
    	// TODO
    	// read journey text from assets/ line-by-line and put them into a long
    	// string with line breaks and then parse them line-by-line (done in parseString() )
    	// kind of redundant work. but, for the moment...
    	/*
    	StringBuilder buf = new StringBuilder();
    	try {
    		InputStream journeyFile =
    				getAssets().open(mRes.getString(R.string.hard_coded_journey));
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
    	*/
    	
    	mJourneyParser.parseString(mJourneyText);
    	view.setText(mJourneyParser.getJsonText());
	}

    public void onClick_activate(View v) {
    	System.out.println("DBG onClick_activate");
    	
    	mRoute.setRoute(mJourneyParser.getJsonObj());
    	mPebbleUi = new PebbleUiController(getApplicationContext(), mRoute);

    	
    	TrafficsenseContainer tsContainer = TrafficsenseContainer.getInstance();
    	tsContainer.setPebbleUiController(mPebbleUi);
    	tsContainer.setRoute(mRoute);
    	
    	Intent rsIntent = new Intent(this, RouteService.class);
    	startService(rsIntent);
	  
    	TextView view = (TextView) findViewById(R.id.textView3);
    	// TODO : for debug: dump mRoute object
    	Gson gson = new Gson();
    	view.setText(gson.toJson(mRoute));
    }
    
    public void onClick_send(View v) {
    	System.out.println("DBG onClick_send");
    	// not in use
	}

}
