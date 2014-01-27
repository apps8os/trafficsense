package com.example.ctxlogtest;

import org.apps8os.contextlogger.android.integration.MonitoringFrameworkAgent;
import org.apps8os.contextlogger.android.integration.UsageMonitoringAgent;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		System.out.println("DBG app onCreate: starting CtxLog");
		// Get instance of MonitoringFrameworkAgent
		MonitoringFrameworkAgent mfAgent = MonitoringFrameworkAgent.getInstance();
		// Start Monitoring Framework using an instance of android.content.Context
		mfAgent.start(this);

	}
	
	@Override
	protected void onDestroy() {
		
		System.out.println("DBG app onDestroy: stopping CtxLog");
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
	
	public void btn_archive_onClick(View v) {
		System.out.println("DBG btn_archive clicked");
		// Get instance of MonitoringFrameworkAgent
		MonitoringFrameworkAgent mfAgent = MonitoringFrameworkAgent.getInstance();
		// Archive collected data
		mfAgent.archive(this);
	}
	
	public void btn_sendAppIntent_onClick(View v) {
		// TODO
		// this is one of the two ways we can record custom event
		System.out.println("DBG btn_send_appintent clicked");
		// Get instance of UsageMonitoringAgent
		UsageMonitoringAgent umAgent = UsageMonitoringAgent.getInstance();
		// Usage monitoring for application event
		umAgent.onEvent(this, "btn_send_appintent clicked");

	}

}
