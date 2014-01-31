package org.apps8os.trafficsense.first;

import org.apps8os.contextlogger.android.integration.MonitoringFrameworkAgent;
import org.apps8os.trafficsense.first.GmailReader.EmailException;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.res.Resources;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends Activity {
	
	private PebbleCommunication mPebbleCommunication;
	
	Resources mRes;
	String mJourneyText;
	JourneyParser mJourneyParser;
	// TODO: change this to the format agreed by Javier & Atte
	String mStatusMessage;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		mRes = getResources();
		mJourneyText = new String("");
		mJourneyParser = new JourneyParser();

		// Start ContextLogger3
		// Get instance of MonitoringFrameworkAgent
		MonitoringFrameworkAgent mfAgent = MonitoringFrameworkAgent.getInstance();

		// Start Monitoring Framework using an instance of android.content.Context
		mfAgent.start(this);
		mPebbleCommunication = new PebbleCommunication(getApplicationContext());
		mPebbleCommunication.startAppOnPebble();
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
    	// TODO: Javier
    	TextView view = (TextView) findViewById(R.id.textView3);

    	// sets an alarm which expires x seconds later.
    	int x = 5;
    	Intent intent = new Intent(this, AlarmBroadcastReceiver.class);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(
				this.getApplicationContext(), 0, intent, 0);
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

}
