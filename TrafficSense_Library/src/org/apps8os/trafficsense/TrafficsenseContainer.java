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


/**
 * The singleton object for TrafficSense journey tracker.
 * 
 * Life-cycle: constructor -> open -> close -> open -> close ... 
 */
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

	/**
	 * Singleton class, invoke {@link #getInstance()} instead.
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

	/**
	 * Returns the shared instance of this singleton object.
	 * Instantiates it on first invocation.
	 * 
	 * @return	the shared singleton instance.
	 */
	public static TrafficsenseContainer getInstance() {
		if(instance == null) {
			instance = new TrafficsenseContainer();
		}
		return instance;
	}

	/**
	 * Check if the shared instance should be initialized.
	 * The caller should invoke {@link #open(Context)} if this is the case.
	 * @return true if it should be initialized.
	 */
	private boolean shouldInit() {
		if (mRunningServices < 0 || mAttachedActivities < 0) {
			System.out.println("DBG Containter: init: attached count below zero");
		}
		if (mRunningServices == 0 && mAttachedActivities == 0) {
			return true;
		}
		return false;
	}
	
	/**
	 * Check if we are the last to detach from the singleton.
	 * The caller should call {@link #close()} if this is the case.
	 * @return true if we are the last one.
	 */
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
	
	/**
	 * Attach an Activity to the singleton.
	 * Should generally be invoked from an Activity before any other operations.
	 * If the singleton is uninitialized, it is initialized and the Context supplied is used
	 * for all further operations. Otherwise, the Context is ignored.
	 * 
	 * @param ctx the Context to be associated.
	 */
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
	
	/**
	 * Detach an Activity from the singleton.
	 * Should be invoked in onPause().
	 * Release resources if there is no one else attached.
	 */
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
	
	/**
	 * Attach a Service and a Context to the singleton.
	 * A Service should attach to the singleton before any other operations.
	 * The singleton cannot be initialized by a Service since services can be restarted
	 * automatically by Android. A Service receiving false from this method should
	 * not perform any further operations with the singleton.
	 * 
	 * @param ctx (currently unused).
	 * @return true if successfully attached.
	 */
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
	
	/**
	 * Detach the Service from the singleton.
	 * Should be invoked in onDestroy().
	 * Release resources if there is no one else attached.
	 */
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
	
	/**
	 * Initialized the singleton.
	 * Starts ContextLogger, Pebble communication and Pebble app.
	 * @param ctx the Context for all further operations.
	 */
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
	
	/**
	 * Release resources.
	 * Stops ContextLogger, Pebble communication.
	 */
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
	
	/**
	 * Start the journey tracker.
	 * This is expected to be invoked from an Activity.
	 * 
	 * @param serviceType type of journey tracker service desired.
	 * @param credential account details for accessing mailbox.
	 * @see #startTrackerService(int) for supported serviceTypes.
	 */
	public void startJourneyTracker(final int serviceType, final EmailCredential credential) {
		new Thread(new Runnable() {
			public void run() {
				/**
				 * This keeps our work running in case the calling Activity becomes
				 * invisible before the services is started.
				 */
				activityAttach(mContext.getApplicationContext());
				mJourneyText = retrieveJourneyBlockingPart(credential);
				parseJourney();
				startTrackerService(serviceType);
				activityDetach();
			}
		}).start();
	}

	/**
	 * Launch the journey tracker service selected.
	 * Route must have been set before invoking this method. (such as via
	 * {@link #parseJourney()} or {@link #setRoute(Route)}.
	 * Types currently supported are: SERVICE_TIME_ONLY and SERVICE_LOCATION_ONLY.
	 * Pebble UI is initialized here.
	 * @param serviceType type of journey tracker service to launch.
	 * @see org.apps8os.trafficsense.android.Constants
	 */
	public void startTrackerService(int serviceType) {
		Intent serviceIntent = null;
		
		if (mRunningServices != 0) {
			System.out.println("DBG startLocationOnly: trying to start multiple services?");
			return;
		}
		switch (serviceType) {
		case Constants.SERVICE_TIME_ONLY:
			serviceIntent = new Intent(mContext, TimeOnlyService.class);
			break;
		case Constants.SERVICE_LOCATION_ONLY:
			serviceIntent = new Intent(mContext, LocationOnlyService.class);
			break;
		default:
			System.out.println("DBG invalid serviceType");
			break;
		}
		if (serviceIntent == null) {
			return;
		}
		
		/**
		 * Populate Pebble UI.
		 */
		mPebbleUi = new PebbleUiController(mContext, mRoute);
		
		mContext.startService(serviceIntent);
	}

	/**
	 * Retrieve the journey text from the last message in the inbox of the given account.
	 * This method perform possibly long network operations.
	 * Should be run in an AsyncTask or a separate Thread.
	 * @param credential the e-mail account to be accessed.
	 * @return the journey text as a String.
	 */
	public static String retrieveJourneyBlockingPart(EmailCredential credential) {
		String journeyText = null;
		Email email = null;
		GmailReader reader = new GmailReader();

		try {
			reader.initMailbox(credential.getAddress(), credential.getPassword());
			/**
			 * The first invocation gets the last (newest) message.
			 */
			email = reader.getNextEmail();
		} catch (EmailException e) {
			// TODO: do something here?
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
	

	/**
	 * Retrieves a journey in plain text from the given e-mail account.
	 * The result is stored in {@link #mJourneyText}.
	 * May optionally update an UI element after completion.
	 *  
	 * @param credential e-mail account to be accessed. 
	 * @param update UI element to be updated. (optional)
	 * @param after Runnable task to be posted to update. (optional)
	 */
	public void retrieveJourney(final EmailCredential credential,
			final View update, final Runnable after) {

		new Thread(new Runnable() {
			public void run() {
				
				mJourneyText =
						TrafficsenseContainer.retrieveJourneyBlockingPart(credential);
				
				if (update != null && after != null) {
					update.post(after);
				}
			}
		}).start();
	}
	
	/**
	 * Return the plain text journey.
	 * @return journey in plain text.
	 */
	public String getJourneyText() {
		return mJourneyText;
	}
	
	/**
	 * Assign a string as the plain text journey.
	 * @param journey the journey in plain text with line breaks.
	 * @see #mJourneyText
	 */
	public void setJourneyText(String journey) {
		mJourneyText = journey;
	}
	
	/**
	 * Parse plain text journey and set up internal Route object.
	 * Do nothing if the journey is empty.
	 */
	public void parseJourney() {
		if (mJourneyText == null) {
			return;
		}
		mJourneyParser.parseString(mJourneyText);
		mRoute.setRoute(getJourneyObject());
	}
	
	/**
	 * Return current journey as a Gson JsonObject.
	 * Must call {@link #parseJourney()} before this.
	 * @return the journey.
	 */
	public JsonObject getJourneyObject() {
		return mJourneyParser.getJsonObj();
	}
	
	/**
	 * (Debug Only) Launch time-based journey tracker.
	 * @deprecated use {@link #startTrackerService(int)}
	 */
	public void startTimeOnlyService() {
		startTrackerService(Constants.SERVICE_TIME_ONLY);
	}

	/**
	 * (Debug Only) Launch location-based journey tracker.
	 * @deprecated use {@link #startTrackerService(int)}
	 */
	public void startLocationOnlyService() {
		startTrackerService(Constants.SERVICE_LOCATION_ONLY);
	}
	
	/**
	 * Assign a Pebble UI Controller object for use.
	 * @param pebbleUi controller object to be used.
	 */
	public void setPebbleUiController(PebbleUiController pebbleUi) {
		mPebbleUi = pebbleUi;
	}

	/**
	 * Assign a Route object for use.
	 * @param route object to be used.
	 */
	public void setRoute(Route route) {
		mRoute = route;
	}

	/**
	 * Return current Pebble UI Controller object.
	 * @return current Pebble UI Controller object.
	 * @see #setPebbleUiController(PebbleUiController)
	 */
	public PebbleUiController getPebbleUiController() {
		return mPebbleUi;
	}

	/**
	 * Return current Route object.
	 * @return current Route object.
	 * @see #setRoute(Route)
	 */
	public Route getRoute() {
		return mRoute;
	}
}
