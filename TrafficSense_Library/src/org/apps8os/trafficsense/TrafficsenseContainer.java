package org.apps8os.trafficsense;

import org.apps8os.contextlogger.android.integration.MonitoringFrameworkAgent;
import org.apps8os.trafficsense.android.TimeOnlyService;
import org.apps8os.trafficsense.core.Route;
import org.apps8os.trafficsense.pebble.PebbleCommunication;
import org.apps8os.trafficsense.pebble.PebbleUiController;
import org.apps8os.trafficsense.util.Email;
import org.apps8os.trafficsense.util.GmailReader;
import org.apps8os.trafficsense.util.JourneyParser;
import org.apps8os.trafficsense.util.GmailReader.EmailException;

import com.google.gson.JsonObject;

import android.content.Context;
import android.content.Intent;
import android.view.View;


public class TrafficsenseContainer {
	private static TrafficsenseContainer instance = null;
	private Context mContext;
	private PebbleCommunication mPebbleCommunication;
	private PebbleUiController mPebbleUi;
	private Route mRoute;
	private String mJourneyText;
	private JourneyParser mJourneyParser;
	private boolean mIsServiceRunning;
	private Intent mRunningService;

	/*
	 * This is a singleton, only one instance allowed.
	 */
	protected TrafficsenseContainer() { }

	public static TrafficsenseContainer getInstance() {
		if(instance == null) {
			instance = new TrafficsenseContainer();
		}
		return instance;
	}

	public void start(Context ctx) {
		if (mContext != null) {
			System.out.println("DBG multiple application trying to start TrafficSense container");
			return;
		}
		mContext = ctx;
		
		// Start ContextLogger3
		// Get instance of MonitoringFrameworkAgent
		MonitoringFrameworkAgent mfAgent = MonitoringFrameworkAgent.getInstance();
		// Start Monitoring Framework using an instance of android.content.Context
		mfAgent.start(mContext);

		mPebbleCommunication = new PebbleCommunication(mContext);
		mPebbleCommunication.startAppOnPebble();
		
		mJourneyParser = new JourneyParser();
		mRoute = new Route();
		
		mIsServiceRunning = false;
		mRunningService = null;
	}
	
	public void stop() {
		// Stop ContextLogger3
		// Get instance of MonitoringFrameworkAgent
		MonitoringFrameworkAgent mfAgent = MonitoringFrameworkAgent.getInstance();
		// Stop Monitoring Framework
		mfAgent.stop(mContext);
		
		mPebbleCommunication.stop();
		mPebbleCommunication = null;
		mJourneyText = null;
		mJourneyParser = null;
		mRoute = null;
		
		if (mIsServiceRunning == true) {
			mContext.stopService(mRunningService);
		}
		mIsServiceRunning = false;
		
		mContext = null;
	}
	
	public void retrieveJourney(final String account, final String password) {
		retrieveJourney(account, password, null, null);
	}
	
	public void retrieveJourney(final String account, final String password,
			final View update, final Runnable after) {
		// start new thread because network activities cant run on main ui
		// thread
		new Thread(new Runnable() {
			public void run() {
				// make email and gmailreader object. email is datacontainer and
				// gmailreader
				// the email from the account
				Email email = new Email();
				GmailReader reader = new GmailReader();

				try {
					// initialize the mailbox that gmail reader reads by
					// giving it the email address and password
					reader.initMailbox(account, password);
					// get the next email. This is first time called so it gets
					// the latest email
					email = reader.getNextEmail();
				} catch (EmailException e) {
					mJourneyText = null;
				}
				// get the email content
				// TODO: filter out trailing HTML text
				mJourneyText = email.getContent();
				
				if (update != null && after != null) {
					update.post(after);
				}
			}
		}).start();
	}
	
	public String getJourneyText() {
		return mJourneyText;
	}
	
	public void setJourneyText(String journey) {
		mJourneyText = journey;
	}
	
	public void parseJourney() {
		if (mJourneyText == null) {
			return;
		}
		mJourneyParser.parseString(mJourneyText);
	}
	
	public JsonObject getJourneyObject() {
		return mJourneyParser.getJsonObj();
	}
	
	public void startTimeOnlyService() {
		if (mIsServiceRunning != false) {
			System.out.println("DBG TimeOnly: trying to start multiple services?");
			return;
		}
		mRoute.setRoute(getJourneyObject());
		mPebbleUi = new PebbleUiController(mContext, mRoute);
		mRunningService = new Intent(mContext, TimeOnlyService.class);
		mContext.startService(mRunningService);
		mIsServiceRunning = true;
	}

	public void startLocationOnlyService() {
	}
	
	public void setPebbleUiController(PebbleUiController pebbleUi) {
		mPebbleUi = pebbleUi;
	}

	public void setRoute(Route route) {
		mRoute = route;
	}

	public PebbleUiController getPebbleUiController() {
		return mPebbleUi;
	}

	public Route getRoute() {
		return mRoute;
	}
}
