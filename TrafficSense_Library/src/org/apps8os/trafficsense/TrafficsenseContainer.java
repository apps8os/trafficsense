package org.apps8os.trafficsense;

import org.apps8os.trafficsense.android.Constants;
import org.apps8os.trafficsense.android.LocationOnlyService;
import org.apps8os.trafficsense.android.LocationService;
import org.apps8os.trafficsense.android.TimeOnlyService;
import org.apps8os.trafficsense.core.Route;
import org.apps8os.trafficsense.pebble.PebbleCommunication;
import org.apps8os.trafficsense.pebble.PebbleUiController;
import org.apps8os.trafficsense.util.Email;
import org.apps8os.trafficsense.util.EmailCredential;
import org.apps8os.trafficsense.util.EmailReader;
import org.apps8os.trafficsense.util.JourneyInfoResolver;
import org.apps8os.trafficsense.util.JourneyParser;
import org.apps8os.trafficsense.util.EmailReader.EmailException;

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
 * Singleton for TrafficSense journey tracker.
 * 
 * Application using TrafficSense must first obtain an instance of this object
 * by invoking {@link #getInstance()}. All entities such as Activity and Service
 * interacting with the singleton must invoke {@link #activityAttach(Context)} or
 * {@link #serviceAttach(Context)} before other operations. The entity must
 * invoke {@link #activityDetach()} or {@link #serviceDetach()} after it finishes
 * its work with the singleton. 
 */
public class TrafficsenseContainer {
	/**
	 * ContextLogger / Funf pipline name
	 */
	final static String CTXLOG_PIPELINE_NAME = "default";
	/**
	 * The shared instance of this singleton.
	 */
	private static volatile TrafficsenseContainer instance = null;
	/**
	 * The associated context.
	 */
	private Context mContext;
	/**
	 * Connection to the ContextLogger.
	 */
	private ServiceConnection mCtxLogFunfManagerConn;
	/**
	 * The underlying FunfManager (which is a Service) of ContextLogger.
	 */
	private FunfManager mCtxLogFunfManager;
	/**
	 * Communication handler of Pebble.
	 */
	private PebbleCommunication mPebbleCommunication;
	/**
	 * Pebble UI Controller.
	 */
	private PebbleUiController mPebbleUi;
	/**
	 * Current journey in internal data structure.
	 * @see #startTrackerService(int)
	 */
	private volatile Route mRoute;
	/**
	 * Last set/retrieved plain text journey.
	 */
	private volatile String mJourneyText;
	/**
	 * Last parsed journey as Gson JsonObject.
	 */
	private volatile JsonObject mJourneyJsonObject;
	/**
	 * Number of attached Activity.
	 * @see #activityAttach(Context)
	 * @see #activityDetach()
	 */
	private volatile int mAttachedActivities = 0;
	/**
	 * Number of attached Service.
	 * @see #serviceAttach(Context)
	 * @see #serviceDetach()
	 */
	private volatile int mRunningServices = 0;

	private volatile boolean isLoading = false;
	/**
	 * Singleton class, invoke {@link #getInstance()} instead.
	 */
	private TrafficsenseContainer() {
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
	 * An object which serves as a lock to synchronize {@link #getInstance()}.
	 */
	private static volatile Object instanceLock = new Object();
	
	/**
	 * Returns the shared instance of this singleton object.
	 * Instantiates it on first invocation.
	 * 
	 * @return	the shared singleton instance.
	 */
	public static TrafficsenseContainer getInstance() {
		synchronized (instanceLock) {
			if(instance == null) {
				instance = new TrafficsenseContainer();
			}
			return instance;
		}
	}

	/**
	 * Check if the shared instance should be initialized.
	 * The caller should invoke {@link #open(Context)} if this is the case.
	 * 
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
	 * 
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
	 * returns a boolean value indicating whether info (email, coords) is being loaded. 
	 * @return
	 */
	public boolean isLoading(){
		return(isLoading);
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
	 * Whether at least one journey tracker service is active.
	 * 
	 * @return true if at least one journey tracker service is active.
	 */
	public boolean isJourneyStarted() {
		synchronized (this) {
			if (mRunningServices > 0) {
				return true;
			}
		}
		return false;
	}
	/**
	 * Stop following current journey.
	 * Stops all running tracker services.
	 * Does not reset current progress in mRoute.
	 */
	public void stopJourney() {
		Intent serviceIntent;
		synchronized (this) {
			if (mRunningServices == 0) {
				return;
			}
		}
		serviceIntent = new Intent(mContext, TimeOnlyService.class);
		mContext.stopService(serviceIntent);
		serviceIntent = new Intent(mContext, LocationOnlyService.class);
		mContext.stopService(serviceIntent);
		serviceIntent = new Intent(mContext, LocationService.class);
		mContext.stopService(serviceIntent);
		
		
		
		// TODO: add some code here if a new Service is introduced.
	}
	
	/**
	 * Initialized the singleton.
	 * Starts ContextLogger, Pebble communication and Pebble app.
	 * Must invoke {@link #close()} afterwards to release resources.
	 * 
	 * @param ctx the Context for all further operations.
	 * @see #close()
	 */
	private void open(Context ctx) {
		System.out.println("DBG Container open");
		mContext = ctx;
		
		// Start ContextLogger3
		Intent ctxlogIntent = new Intent(mContext, FunfManager.class);
		mContext.bindService(ctxlogIntent, mCtxLogFunfManagerConn, Service.BIND_AUTO_CREATE);

		mPebbleCommunication = new PebbleCommunication(mContext);
		mPebbleCommunication.startAppOnPebble();
		
		mJourneyText = null;
		mJourneyJsonObject = null;
		mRoute = new Route();
	}
	
	/**
	 * Release resources.
	 * Stops ContextLogger, Pebble communication.
	 * 
	 * @see #open(Context)
	 */
	private void close() {
		System.out.println("DBG Container close");
		// Stop ContextLogger3
		mCtxLogFunfManager.disablePipeline(CTXLOG_PIPELINE_NAME); //ATT: throws null pointer error occasionally
		mContext.unbindService(mCtxLogFunfManagerConn);
		
		mPebbleCommunication.stop();
		mPebbleCommunication = null;
		mJourneyText = null;
		mJourneyJsonObject = null;
		mRoute = null;
		mContext = null;
		
		if (isLast() != true) {
			System.out.println("DBG Container: stop() but !isLast() ?!");
			System.out.println("DBG Container: Act:"+mAttachedActivities+" Ser:"+mRunningServices);
			// TODO: maybe throw an Exception and do a stack trace here
		}
	}
	
	/**
	 * Retrieve and start following a journey in a separate Thread.
	 * 
	 * Retrieves a journey from an e-mail account.
	 * GPS coordinates are retrieved if the desired service is location-based.
	 * Then starts the specified tracker service for the journey.
	 * This is expected to be invoked from an Activity.
	 * Starts a new Thread in order to keep working in case the calling
	 * Activity becomes invisible before the service is started.
	 * 
	 * @param serviceType type of journey tracker service desired.
	 * @param credential account details for accessing mailbox.
	 * @see #startTrackerService(int) for supported serviceTypes.
	 */
	public void startJourneyTracker(final int serviceType, final EmailCredential credential) {
		isLoading = true;
		new Thread(new Runnable() {
			public void run() {
				activityAttach(mContext.getApplicationContext());
				mJourneyText = retrieveJourneyBlockingPart(credential);
				parseJourney();
				System.out.println("DBG startJourneyTracker mJourneyText:"+mJourneyText);
				if (serviceType != Constants.SERVICE_TIME_ONLY) {
					/**
					 * TODO: Check its return value!
					 * false is returned on error.
					 * Maybe send an Intent?
					 */
					retrieveCoordinatesForStopsBlockingPart(mRoute);
				}
				startTrackerService(serviceType);
				activityDetach();
				isLoading = false;
			}
		}).start();
	}

	/**
	 * Launch the journey tracker service selected.
	 * Route must have been set before invoking this method. (such as via
	 * {@link #parseJourney()} or {@link #setRoute(Route)}.
	 * Types currently supported are: SERVICE_TIME_ONLY and SERVICE_LOCATION_ONLY.
	 * Pebble UI is initialized here.
	 * 
	 * @param serviceType type of journey tracker service to launch.
	 * @see org.apps8os.trafficsense.android.Constants
	 */
	public void startTrackerService(int serviceType) {
		Intent serviceIntent = null;
		
		if (mRoute == null) {
			System.out.println("DBG startTrackerService: route not set");
			return;
		}
		/**
		 * TODO: Currently only one service at a time is allowed.
		 */
		if (mRunningServices != 0) {
			System.out.println("DBG startTrackerService: trying to start multiple services?");
			return;
		}
		switch (serviceType) {
		case Constants.SERVICE_TIME_ONLY:
			serviceIntent = new Intent(mContext, TimeOnlyService.class);
			break;
		case Constants.SERVICE_LOCATION_ONLY:
			serviceIntent = new Intent(mContext, LocationOnlyService.class);
			break;
		case Constants.LOCATION_SERVICE:
			serviceIntent = new Intent(mContext, LocationService.class);
			break;
		default:
			System.out.println("DBG invalid serviceType");
			break;
		}
		if (serviceIntent == null) {
			return;
		}
		
		/**
		 * Bind Pebble UI controller to the communication channel.
		 */
		mPebbleUi = new PebbleUiController(mPebbleCommunication, mRoute);
		mContext.startService(serviceIntent);
	}
	
	/**
	 * Retrieve the journey text from the last message in the inbox of the given account.
	 * This method perform possibly long network operations.
	 * 
	 * Must NOT invoke this from the main/UI thread.
	 * 
	 * @param credential the e-mail account to be accessed.
	 * @return the journey text as a String.
	 */
	public static String retrieveJourneyBlockingPart(EmailCredential credential) {
		String journeyText = null;
		Email email = null;
		EmailReader reader = new EmailReader();

		try {
			reader.initMailbox(credential);
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
	 * Currently it is the last mail in inbox.
	 * @see #retrieveJourneyBlockingPart(EmailCredential)
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
	 * Retrieves GPS coordinates for all stops along the journey.
	 * 
	 * Must NOT invoke this from the main/UI thread.
	 * 
	 * @return true on success, false otherwise.
	 */
	public static boolean retrieveCoordinatesForStopsBlockingPart(Route route) {
		if (route == null) {
			// TODO error handling ?
			System.out.println("DBG retrieveCoordinatesForStopsBlockingPart null mRoute");
			return false;
		}
		JourneyInfoResolver resolver = new JourneyInfoResolver();
		/**
		 * Access HSL API to retrieve GPS coordinates for each Waypoint (if stopCode is available).
		 */
		if (resolver.retrieveCoordinatesFromHsl(route) == false) {
			// TODO: error handling.
			return false;
		}
		// TODO: what about those who do not have a stopCode (= NO_STOP_CODE) ?
		return true;
	}
	
	/**
	 * Return current plain text journey.
	 * May return null if there is none, or empty string if error.
	 * 
	 * @return journey in plain text.
	 */
	public String getJourneyText() {
		return mJourneyText;
	}
	
	/**
	 * Assign a string as the plain text journey.
	 * 
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
		JourneyParser parser = new JourneyParser();
		parser.parseString(mJourneyText);
		mJourneyJsonObject = parser.getJsonObj();
		mRoute = new Route();
		mRoute.setRoute(parser.getJsonObj());
	}
	
	/**
	 * Return current journey as a Gson JsonObject.
	 * Must call {@link #parseJourney()} before this.
	 * May return null if there is none.
	 * 
	 * @return the journey.
	 */
	public JsonObject getJourneyObject() {
		return mJourneyJsonObject;
	}

	/**
	 * Assign a Pebble UI Controller object for use.
	 * 
	 * @param pebbleUi controller object to be used.
	 */
	public void setPebbleUiController(PebbleUiController pebbleUi) {
		mPebbleUi = pebbleUi;
	}

	/**
	 * Assign a Route object for use.
	 * 
	 * @param route object to be used.
	 */
	public void setRoute(Route route) {
		mRoute = route;
	}

	/**
	 * Return current Pebble UI Controller object.
	 * 
	 * @return current Pebble UI Controller object.
	 * @see #startTrackerService(int)
	 * @see #setPebbleUiController(PebbleUiController)
	 */
	public PebbleUiController getPebbleUiController() {
		return mPebbleUi;
	}

	/**
	 * Return current Route object.
	 * 
	 * @return current Route object.
	 * @see #setRoute(Route)
	 */
	public Route getRoute() {
		return mRoute;
	}
}
