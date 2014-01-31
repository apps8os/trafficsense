package org.apps8os.trafficsense.first;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apps8os.contextlogger.android.integration.MonitoringFrameworkAgent;
import org.apps8os.trafficsense.first.GmailReader.EmailException;
import org.json.JSONArray;
import org.json.JSONException;

import android.os.Bundle;
import android.app.Activity;
import android.content.res.Resources;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends Activity {
	
	private PebbleCommunication mPebbleCommunication;
	
	Resources mRes;
	String emailContent;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		mRes = getResources();
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
					reader.initMailbox("trafficsense.aalto@gmail.com","ag47)h(58P");
					email=reader.getNextEmail();	  	
				} catch (EmailException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				emailContent=email.getContent();
				//System.out.println(emailText);
				textview.post(new Runnable(){
					public void run(){
						if(emailContent!=null){
							textview.setText(emailContent);
						}
						else{
							textview.setText("Error:reached end of mail box");
						}
					}
				});
			}
		}).start();
		
		
	}
	
    public void onClick_parse(View v) {
    	
    
    	System.out.println("DBG onClick_parse");
		// TODO: Catarina
    	TextView view = (TextView) findViewById(R.id.textView2);
    	JourneyParser parser = new JourneyParser();
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
    	
    	parser.parseString(buf.toString());
    	**/
    	parser.parseString(emailContent);
    	view.setText(parser.getJsonText());
	}

    public void onClick_activate(View v) {
    	System.out.println("DBG onClick_activate");
    	// TODO: Javier
    	TextView view = (TextView) findViewById(R.id.textView3);
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
