package org.apps8os.trafficsense.first;

import android.os.Bundle;
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

import org.apps8os.trafficsense.TrafficsenseContainer;
import org.apps8os.trafficsense.android.Constants;
import org.apps8os.trafficsense.util.EmailCredential;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class MainActivity extends Activity {

	private RouteServiceEventReceiver mRecv;
	private TrafficsenseContainer mContainer;
	private EmailCredential mCred;

	private class RouteServiceEventReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			System.out.println("DBG RouteServiceEventReceiver onReceive");
			// TODO update UI here
			TextView view = (TextView) findViewById(R.id.textView3);
			if (intent.hasExtra(Constants.ACTION_ROUTE_EVENT_EXTRA_MESSAGE)) {
				view.setText(intent
						.getStringExtra(Constants.ACTION_ROUTE_EVENT_EXTRA_MESSAGE));
			}
		}

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mRecv = new RouteServiceEventReceiver();
		mContainer = TrafficsenseContainer.getInstance();
		// TODO: hard-coded
		mCred = new EmailCredential("trafficsense.aalto@gmail.com", "ag47)h(58P");
	}

	@Override
	protected void onResume() {
		super.onResume();
		IntentFilter filter = new IntentFilter(Constants.ACTION_ROUTE_EVENT);
		registerReceiver(mRecv, filter);
		mContainer.activityAttach(getApplicationContext());
	}

	@Override
	protected void onPause() {
		unregisterReceiver(mRecv);
		mContainer.activityDetach();
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	// called by button that fetches emails.
	// email address and password are hardcoded and the last received email is
	// always gotten
	public void onClick_fetch(View v) {
		System.out.println("DBG onClick_fetch");
		
		final TextView textview = (TextView) findViewById(R.id.textView1);

		Runnable after = new Runnable() {
			public void run() {
				if (mContainer.getJourneyText() != null) {
					textview.setText(mContainer.getJourneyText());
				} else {
					textview.setText("Unable to retrieve journey text");
				}
			}
		};

		mContainer.retrieveJourney(mCred, textview, after);

	}

	private String getHardCodedJourneyText() {
		Resources mRes = getResources();
		StringBuilder buf = new StringBuilder();
		try {
			InputStream journeyFile = getAssets().open(
					mRes.getString(R.string.hard_coded_journey));
			BufferedReader rd = new BufferedReader(new InputStreamReader(
					journeyFile));
			String str;
			while ((str = rd.readLine()) != null) {
				buf.append(str + "\n");
			}
			rd.close();
		} catch (IOException ex) {
			Log.d(getLocalClassName(), "IOEx", ex);
		}
		return buf.toString();
	}

	public void onClick_parse(View v) {
		System.out.println("DBG onClick_parse");
		TextView view = (TextView) findViewById(R.id.textView2);

		//mContainer.setJourneyText(getHardCodedJourneyText());
		mContainer.parseJourney();
		if (mContainer.getJourneyObject() != null) {
			view.setText(mContainer.getJourneyObject().toString());
		}
	}

	public void onClick_activate(View v) {
		System.out.println("DBG onClick_activate");
		mContainer.startTrackerService(Constants.SERVICE_TIME_ONLY);
	}
	
	public void onClick_automatic(View v) {
		System.out.println("DBG onClick_automatic");
		mContainer.startJourneyTracker(Constants.SERVICE_TIME_ONLY, mCred);
	}
	
	public void onClick_stop(View v) {
		System.out.println("DBG onClick_stop");
		mContainer.stopJourney();
	}

}
