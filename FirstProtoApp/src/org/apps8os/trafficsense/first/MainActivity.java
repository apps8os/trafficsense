package org.apps8os.trafficsense.first;

import org.apps8os.contextlogger.android.integration.MonitoringFrameworkAgent;
import org.apps8os.trafficsense.first.GmailReader.EmailException;

import android.os.Bundle;
import android.app.Activity;
import android.content.res.Resources;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends Activity {

	Resources mRes;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		TextView textview1 = (TextView) findViewById(R.id.textView1);
		textview1.setMovementMethod(new ScrollingMovementMethod());
		mRes = getResources();
		// Start ContextLogger3
		// Get instance of MonitoringFrameworkAgent
		MonitoringFrameworkAgent mfAgent = MonitoringFrameworkAgent.getInstance();

		// Start Monitoring Framework using an instance of android.content.Context
		mfAgent.start(this);
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
				TextView view = (TextView) findViewById(R.id.textView4);
				final String emailText=email.toString();
				System.out.println(emailText);
				textview.post(new Runnable(){
					public void run(){
						if(emailText!=null){
							textview.setText(emailText);
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
    	JsonParser parser = new JsonParser();
    	parser.parseString(mRes.getString(R.string.hard_coded_json));
    	TextView view = (TextView) findViewById(R.id.textView2);
    	view.setText(parser.getJSONText());
	}

    public void onClick_activate(View v) {
    	System.out.println("DBG onClick_activate");
    	// TODO: Javier
    }
    
    public void onClick_send(View v) {
    	System.out.println("DBG onClick_send");
		// TODO: Atte
	}

}
