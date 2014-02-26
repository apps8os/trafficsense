package org.apps8os.trafficsense;

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

import edu.mit.media.funf.FunfManager;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.view.View;


public class TrafficsenseContainer {
	final static String CTXLOG_PIPELINE_NAME = "default";
	
	private static TrafficsenseContainer instance = null;
	private Context mContext;
	private ServiceConnection mCtxLogFunfManagerConn;
	private FunfManager mCtxLogFunfManager;
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
	protected TrafficsenseContainer() {
		mCtxLogFunfManagerConn = new ServiceConnection () {

			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				mCtxLogFunfManager = ((FunfManager.LocalBinder)service).getManager();
				mCtxLogFunfManager.enablePipeline(CTXLOG_PIPELINE_NAME);
			}

			@Override
			public void onServiceDisconnected(ComponentName name) {
				mCtxLogFunfManager = null;
			}
			
		};
	}

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
		System.out.println("DBG Container: isLast: Ser:"+mRunningServices+"Act:"+mAttachedActivities);
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
		System.out.println("DBG activityDetach");
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
		System.out.println("DBG serviceDetach");
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
		System.out.println("DBG Container open");
		mContext = ctx;
		
		// Start ContextLogger3
		Intent ctxlogIntent = new Intent(mContext, FunfManager.class);
		mContext.bindService(ctxlogIntent, mCtxLogFunfManagerConn, Service.BIND_AUTO_CREATE);

		mPebbleCommunication = new PebbleCommunication(mContext);
		mPebbleCommunication.startAppOnPebble();
		
		mJourneyParser = new JourneyParser();
		mRoute = new Route();
	}
	
	// clean up the container, stop ContextLogger, close Pebble connection
	private void close() {
		System.out.println("DBG Container close");
		// Stop ContextLogger3
		mCtxLogFunfManager.disablePipeline(CTXLOG_PIPELINE_NAME);
		mContext.unbindService(mCtxLogFunfManagerConn);
		
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
	
	public void startJourneyTracker(final int serviceType, final EmailCredential credential) {
		new Thread(new Runnable() {
			public void run() {
				// The UI may become invisible once this method is called
				// Do this to retain the container
				activityAttach(mContext.getApplicationContext());
				mJourneyText = retrieveJourneyBlockingPart(credential);
				parseJourney();
				startTrackerService(serviceType);
				activityDetach();
			}
		}).start();
	}

	private void startTrackerService(int serviceType) {
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
	
	// Assumption: UI will not be hidden.
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
