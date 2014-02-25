package org.apps8os.trafficsense;

import org.apps8os.contextlogger.android.integration.MonitoringFrameworkAgent;
import org.apps8os.trafficsense.android.Constants;
import org.apps8os.trafficsense.android.LocationOnlyService;
import org.apps8os.trafficsense.android.TimeOnlyService;
import org.apps8os.trafficsense.core.Route;
import org.apps8os.trafficsense.pebble.PebbleCommunication;
import org.apps8os.trafficsense.pebble.PebbleUiController;
import org.apps8os.trafficsense.util.Email;
import org.apps8os.trafficsense.util.EmailCredential;
import org.apps8os.trafficsense.util.GmailReader;
import org.apps8os.trafficsense.util.JourneyParser;
import org.apps8os.trafficsense.util.GmailReader.EmailException;

import com.google.gson.JsonObject;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.view.View;


public class TrafficsenseContainer {
	private static TrafficsenseContainer instance = null;
	private Context mContext;
	private MonitoringFrameworkAgent mfAgent;
	private PebbleCommunication mPebbleCommunication;
	private PebbleUiController mPebbleUi;
	private Route mRoute;
	private String mJourneyText;
	private JourneyParser mJourneyParser;
	private int mAttachedActivities = 0;
	private int mRunningServices = 0;

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

	private boolean shouldInit() {
		if (mRunningServices < 0 || mAttachedActivities < 0) {
			System.out.println("DBG Containter: init: attached count below zero");
		}
		if (mRunningServices == 0 && mAttachedActivities == 0) {
			return true;
		}
		return false;
	}
	
	private boolean isLast() {
		if (mRunningServices < 0 || mAttachedActivities < 0) {
			System.out.println("DBG Containter: last: attached count below zero");
		}
		if (mRunningServices == 0 && mAttachedActivities == 0) {
			return true;
		}
		return false;
	}
	
	// An Activity should attach to this container before any other operations
	public void activityAttach(Context ctx) {
		boolean fDoInit = false;
		synchronized (this) {
			fDoInit = shouldInit();
			mAttachedActivities ++;
		}
		if (fDoInit == true) {
			open(ctx);
		}
	}
	
	// An Activity should detach before it is destroyed.
	public void activityDetach() {
		boolean fIsLast = false;
		synchronized (this) {
			mAttachedActivities --;
			fIsLast = isLast();
		}
		if (fIsLast == true) {
			close();
		}
	}
	
	// A Service should attach at its start. If attach failed, the Service to stop immediately.
	public boolean serviceAttach(Context ctx) {
		if (shouldInit() == true) {
			System.out.println("DBG serviceAttach no activity?");
			return false;
		}
		if (mRunningServices != 0) {
			System.out.println("DBG serviceAttach multiple services?");
			return false;
		}
		mRunningServices ++;
		return true;
	}
	
	// A Service should detach before it is destroyed.
	public void serviceDetach() {
		boolean fIsLast = false;
		synchronized (this) {
			mRunningServices --;
			fIsLast = isLast();
		}
		if (fIsLast == true) {
			close();
		}
	}
	
	// initialize the container, start ContextLogger, set up Pebble connection
	private void open(Context ctx) {
		mContext = ctx;
		
		// Start ContextLogger3
		// Get instance of MonitoringFrameworkAgent
		mfAgent = MonitoringFrameworkAgent.getInstance();
		// Start Monitoring Framework using an instance of android.content.Context
		mfAgent.start(mContext);

		mPebbleCommunication = new PebbleCommunication(mContext);
		mPebbleCommunication.startAppOnPebble();
		
		mJourneyParser = new JourneyParser();
		mRoute = new Route();
	}
	
	// clean up the container, stop ContextLogger, close Pebble connection
	private void close() {
		// Stop ContextLogger3: stop Monitoring Framework
		mfAgent.stop(mContext);
		mfAgent = null;
		
		mPebbleCommunication.stop();
		mPebbleCommunication = null;
		mJourneyText = null;
		mJourneyParser = null;
		mRoute = null;
		mContext = null;
		
		if (isLast() != true) {
			System.out.println("DBG Container: stop() but !isLast() ?!");
			System.out.println("DBG Container: Act:"+mAttachedActivities+" Ser:"+mRunningServices);
			// TODO: maybe throw an Exception and do a stack trace here
		}
	}
	
	public void startJourneyTracker(final int serviceType, EmailCredential credential) {
		final class ReadMailTask extends AsyncTask<EmailCredential, Integer, String> {
			protected String doInBackground(EmailCredential... creds) {
				return retrieveJourneyBlockingPart(creds[0]);
			}
			
			protected void onPostExecute(String journeyText) {
				if (journeyText != null) {
					mJourneyText = journeyText;
					parseJourney();
					switch (serviceType) {
					case Constants.SERVICE_TIME_ONLY:
						startTimeOnlyService();
						break;
					case Constants.SERVICE_LOCATION_ONLY:
						startLocationOnlyService();
						break;
					default:
						System.out.println("DBG invalid serviceType");
						break;
					}
				}
			}
		}
		
		new ReadMailTask().execute(credential);
	}

	public static String retrieveJourneyBlockingPart(EmailCredential credential) {
		// make email and gmailreader object. email is datacontainer and
		// gmailreader
		// the email from the account
		String journeyText = null;
		Email email = null;
		GmailReader reader = new GmailReader();

		try {
			// initialize the mailbox that gmail reader reads by
			// giving it the email address and password
			reader.initMailbox(credential.getAddress(), credential.getPassword());
			// get the next email. This is first time called so it gets
			// the latest email
			email = reader.getNextEmail();
		} catch (EmailException e) {
			System.out.println("DBG EmailException: " + e.getMessage());
		} finally {
			if (email != null) {
				journeyText = email.getContent();
				// TODO: filter out trailing HTML text
				// TODO: check for other error / format ?
			}
		}
		return journeyText;
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
				EmailCredential credential = new EmailCredential(account, password);
				mJourneyText =
						TrafficsenseContainer.retrieveJourneyBlockingPart(credential);
				
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
		mRoute.setRoute(getJourneyObject());
	}
	
	public JsonObject getJourneyObject() {
		return mJourneyParser.getJsonObj();
	}
	
	public void startTimeOnlyService() {
		if (mRunningServices != 0) {
			System.out.println("DBG startTimeOnly: trying to start multiple services?");
			return;
		}
		mPebbleUi = new PebbleUiController(mContext, mRoute);
		Intent tosIntent = new Intent(mContext, TimeOnlyService.class);
		mContext.startService(tosIntent);
	}

	public void startLocationOnlyService() {
		if (mRunningServices != 0) {
			System.out.println("DBG startLocationOnly: trying to start multiple services?");
			return;
		}
		mPebbleUi = new PebbleUiController(mContext, mRoute);
		Intent losIntent = new Intent(mContext, LocationOnlyService.class);
		mContext.startService(losIntent);
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
