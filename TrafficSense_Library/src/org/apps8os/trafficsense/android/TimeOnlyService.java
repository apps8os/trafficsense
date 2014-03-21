package org.apps8os.trafficsense.android;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.apps8os.trafficsense.TrafficsenseContainer;
import org.apps8os.trafficsense.core.Route;
import org.apps8os.trafficsense.core.Segment;
import org.apps8os.trafficsense.core.Waypoint;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.os.Vibrator;
import android.widget.Toast;

/**
 * Service that follows our route based on time. 
 */
public class TimeOnlyService extends Service {

	/**
	 * The TrafficSense service container. 
	 */
	private TrafficsenseContainer mContainer;
	/**
	 * The journey we are following.
	 */
	private Route mRoute;
	/**
	 * Handler for next-stop events.
	 */
	private NextWaypointReceiver mNextWaypointReceiver;
	/**
	 * Handler for actual alert generation.
	 */
	private VibrateAndMakeNotificationReceiver mMakeAlertReceiver;
	/**
	 * The context in which we are operating.
	 */
	private Context mContext;
	/**
	 * The clock/alarm event generator.
	 */
	private AlarmManager mAM;
	/**
	 * PendingIntent for next stop events.
	 * Use a common instance so we can cancel the pending one (if any) before
	 * we stop the service.
	 */
	private PendingIntent mNextWaypointIntent;
	/**
	 * PendingIntent for get off notifications.
	 * @see #mNextWaypointIntent
	 */
	private PendingIntent mGetOffIntent;
	/**
	 * Did we encounter an error during onStartCommand().
	 * If true, the service failed to start.
	 * onStartCommand() should stopSelf() and onDestroy() should
	 * not do the clean up.
	 * This would be true if the service is automatically restarted
	 * by Android.
	 */
	boolean errorOnStart;

	/**
	 * Create common resources for the service.
	 */
	@Override
	public void onCreate() {
		super.onCreate();
		System.out.println("DBG TimeOnlyService.onCreate");
		mContext = this;
		mContainer = TrafficsenseContainer.getInstance();
		mRoute = mContainer.getRoute();
		mNextWaypointReceiver = new NextWaypointReceiver();
		mMakeAlertReceiver = new VibrateAndMakeNotificationReceiver();
		Intent intentWaypoint = new Intent();
		intentWaypoint.setAction(Constants.ACTION_NEXT_WAYPOINT);
		mNextWaypointIntent = PendingIntent.getBroadcast(mContext, 0,
				intentWaypoint, PendingIntent.FLAG_CANCEL_CURRENT);
		Intent intentGetOff = new Intent();
		intentGetOff.setAction(Constants.ACTION_GET_OFF);
		mGetOffIntent = PendingIntent.getBroadcast(mContext, 0, intentGetOff,
				PendingIntent.FLAG_CANCEL_CURRENT);
		mAM = (AlarmManager) getSystemService(ALARM_SERVICE);
		errorOnStart = false;
	}

	/**
	 * Start rolling the service.
	 * 
	 * The service is stopped when:
	 * 1) the journey ends
	 * 2) Android kills it
	 * 3) {@link #errorOnStart} is true
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		System.out.println("DBG TimeOnlyService.onStartCommand");
		errorOnStart = false;
		if (mContainer.serviceAttach(getApplicationContext()) == false) {
			System.out.println("DBG TimeOnlyService: unable to attach container");
			errorOnStart = true;
		} else if (mRoute == null) {
			System.out.println("DBG TimeOnlyService: mRoute = null");
			mContainer.serviceDetach();
			errorOnStart = true;
		} else if (mContainer.getPebbleUiController() == null) {
			System.out.println("DBG TimeOnlyService: Pebble UI not set up properly");
			mContainer.serviceDetach();
			errorOnStart = true;
		} else {
			System.out.println("DBG TimeOnlyService.onStartCommand cp1");
			mContainer.getPebbleUiController().update();

			registerReceiver(mNextWaypointReceiver, new IntentFilter(
					Constants.ACTION_NEXT_WAYPOINT));
			registerReceiver(mMakeAlertReceiver, new IntentFilter(
					Constants.ACTION_GET_OFF));

			// gets the time of the first waypoint
			long timeToNextWaypoint = timeStringToDate(
					mRoute.getDate() + " " +
					mRoute.getSegmentList().get(0).getWaypointList().get(0).getWaypointTime()
					).getTime();

			if (Constants.useWallClock == true
					&& timeToNextWaypoint < System.currentTimeMillis()) {
				System.out.println("DBG next waypoint in the past");
				Toast toast = Toast.makeText(mContext,
						"Error:next waypoint is in the past",
						Toast.LENGTH_SHORT);
				toast.show();
				errorOnStart = true;
			} else {
				scheduleNextAlarm(timeToNextWaypoint);
			}
		}
		
		System.out.println("DBG TimeOnlyService.onStartCommand cp2");
		
		if (errorOnStart) {
			this.stopSelf();
		}
		/**
		 * We are currently unable to resume operation, so do not re-create automatically.
		 */
		return START_NOT_STICKY;
	}

	/**
	 * Clean up.
	 * Note that invocation is not guaranteed. 
	 */
	@Override
	public void onDestroy() {
		super.onDestroy();

		System.out.println("DBG TimeOnlyService.onDestroy");
		if (errorOnStart == false) {
			mAM.cancel(mNextWaypointIntent);
			mAM.cancel(mGetOffIntent);
			unregisterReceiver(mNextWaypointReceiver);
			unregisterReceiver(mMakeAlertReceiver);
			mContainer.serviceDetach();
		}
	}

	/**
	 * Schedule a next stop alarm.
	 * 
	 * @param atMillis time in milliseconds that the alarm should go off.
	 */
	private void scheduleNextAlarm(long atMillis) {
		if (Constants.useWallClock == true) {
			mAM.set(AlarmManager.RTC_WAKEUP, atMillis, mNextWaypointIntent);
		} else {
			mAM.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()
					+ Constants.TEST_TIME, mNextWaypointIntent);
		}
	}

	/**
	 * Schedule a get off alarm.
	 * 
	 * @param atMillis time in milliseconds that the alarm should go off.
	 */
	private void scheduleGetOffAlarm(long atMillis) {
		mAM.set(AlarmManager.RTC_WAKEUP, atMillis, mGetOffIntent);
		System.out.println("DBG TimeOnlyService scheduled GetOff alarm");
	}

	/**
	 * TODO: documentation
	 * @param timeStr
	 * @return
	 */
	private Date timeStringToDate(String timeStr) {
		Date date = null;
		try {
			// TODO: Locale problem
			date = new SimpleDateFormat("EEEE dd.M.yyyy kk:mm", Locale.ENGLISH)
			.parse(timeStr);
		} catch (ParseException e) {
			System.out.println("DBG TimeOnlyService timeStringToDate error: " + e.getMessage());
		}
		return date;
	}

	/**
	 * Broadcast receiver that receives intents indicating that next stop has
	 * been reached. If it is the last waypoint it set the next segment as the
	 * current segment and set the timer for it. In any case it gets the next
	 * waypoint in the segment and sets the alarm to indicate when we are there.
	 */
	class NextWaypointReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context arg0, Intent arg1) {
			boolean fJourneyEnded = false;
			System.out.println("DBG TimeOnlyService NextWaypointReceiver.onReceive"
					+ arg1.getAction());
			String message = "ERROR: No message set in onReceive()";

			/**
			 * Advance to next waypoint and get its index.
			 */
			Waypoint nextWaypoint = mContainer.getRoute().getCurrentSegment()
					.setNextWaypoint();
			/**
			 * If nextWaypoint is null then we have finished this Segment.
			 */
			if (nextWaypoint == null) {
				Segment nextSegment = mContainer.getRoute().setNextSegment();
				/**
				 * If there is no more Segment remaining we have finished this journey.
				 */
				if (nextSegment == null) {
					message = "Journey ended.";
					fJourneyEnded = true;
				} else {
					message = "Segment ended.";
					nextWaypoint = nextSegment.getCurrentWaypoint();
					if (mContainer.getPebbleUiController() == null)
						System.out.println("DBG TimeOnlyService uicontroller is null");
					int secondLastWpIndex = nextSegment.getWaypointList()
							.size() - 2;
					long timeToAlarm = 0;
					if (Constants.useWallClock == true) {
						timeToAlarm = timeStringToDate(
								mContainer.getRoute().getDate()
										+ " "
										+ nextSegment.getWaypointList()
												.get(secondLastWpIndex)
												.getWaypointTime()).getTime();
					} else {
						timeToAlarm = (secondLastWpIndex + 1)
								* Constants.TEST_TIME
								+ System.currentTimeMillis();
					}
					System.out.println("DBG TimeOnlyService scheduling getOffAlarm");
					scheduleGetOffAlarm(timeToAlarm);
				}
			}

			/**
			 * Set the alarm for the next waypoint.
			 */
			if (nextWaypoint != null) {
				long timeToNextWaypoint = timeStringToDate(
						mContainer.getRoute().getDate() + " "
								+ nextWaypoint.getWaypointTime()).getTime();
				scheduleNextAlarm(timeToNextWaypoint);
				message = "Next waypoint is: " + nextWaypoint.getWaypointName();
			}

			System.out.println("DBG TimeOnlyService NextWaypointReceiver.onReceive cp");

			/**
			 * Notify the UI.
			 */
			Intent vi = new Intent();
			vi.putExtra(Constants.ACTION_ROUTE_EVENT_EXTRA_MESSAGE, message);
			vi.setAction(Constants.ACTION_ROUTE_EVENT);
			sendBroadcast(vi);

			/**
			 * Show a Toast. This shows even if the UI is not visible.
			 */
			Toast toast = Toast.makeText(mContext, message, Toast.LENGTH_SHORT);
			toast.show();
			
			/**
			 * Stop the service when the journey ends.
			 */
			if (fJourneyEnded == true) {
				((TimeOnlyService)mContext).stopSelf();
			}
			
			// Update pebble UI
			mContainer.getPebbleUiController().update();
		}

	}

	/**
	 * Handler for get off events.
	 * Vibrate the hand set and send a notification to Pebble.
	 * This lives in a Service, so it works without the UI being visible.
	 */
	private class VibrateAndMakeNotificationReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			System.out.println("DBG TimeOnlyService received getoffalarm");
			Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
			vibrator.vibrate(Constants.VIBRATOR_DURATION);

		}

	}

	/**
	 * No bind to this service. 
	 */
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

}
