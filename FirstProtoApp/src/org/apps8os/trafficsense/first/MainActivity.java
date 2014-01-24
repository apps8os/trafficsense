package org.apps8os.trafficsense.first;

import org.apps8os.contextlogger.android.integration.MonitoringFrameworkAgent;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
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
		// TODO: Jussi
	}
	
    public void onClick_parse(View v) {
    	System.out.println("DBG onClick_parse");
		// TODO: Catarina
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
