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

//service that follows our route by always scheduling the nextwaypoint with the alarm manager. 
public class TimeOnlyService extends Service {

	private TrafficsenseContainer mContainer;
	private Route mRoute;
	private NextWaypointReceiver mNextWaypointReceiver;
	private VibrateAndMakeNotificationReceiver mMakeAlertReceiver;
	private Context mContext;
	private AlarmManager mAM;
	private PendingIntent mNextWaypointIntent;
	private PendingIntent mGetOffIntent;
	boolean errorOnStart;

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

	// the service is stopped when: 1) the journey ends 2) Android kills it 3) error on start
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
		} else {
			System.out.println("DBG TimeOnlyService.onStartCommand cp1");
			mContainer.getPebbleUiController().initializeList();

			registerReceiver(mNextWaypointReceiver, new IntentFilter(
					Constants.ACTION_NEXT_WAYPOINT));
			registerReceiver(mMakeAlertReceiver, new IntentFilter(
					Constants.ACTION_GET_OFF));

			// gets the time of the first waypoint
			long timeToNextWaypoint = timeStringToDate(
					mRoute.getDate() + " " +
					mRoute.getSegmentList().get(0).getWaypointList().get(0).getWaypointTime()
					).getTime();

			// TODO do not check this at the moment
			/*
			if(timeToNextWaypoint < System.currentTimeMillis()) {
				System.out.println("DBG next waypoint in the past"); Toast
				toast = Toast.makeText(mContext, "Error:next waypoint is in the past", Toast.LENGTH_SHORT);
				toast.show(); errorOnStart = true;
			} else {
			*/
			scheduleNextAlarm(timeToNextWaypoint);
			//}
		}
		
		System.out.println("DBG TimeOnlyService.onStartCommand cp2");
		
		if (errorOnStart) {
			this.stopSelf();
		}
		// We are currently unable to resume operation, so do not re-create automatically
		return START_NOT_STICKY;
	}

	// TODO: not always called when force-stop
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

	private void scheduleNextAlarm(long atMillis) {
		// mAM.set(AlarmManager.RTC_WAKEUP, atMillis, mNextWaypointIntent);
		// TODO testing use
		mAM.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()
				+ Constants.TEST_TIME, mNextWaypointIntent);
	}

	private void scheduleGetOffAlarm(long atMillis) {
		mAM.set(AlarmManager.RTC_WAKEUP, atMillis, mGetOffIntent);
		System.out.println("DBG TimeOnlyService scheduled GetOff alarm");
	}

	private Date timeStringToDate(String timeStr) {
		Date date = null;
		try {
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

			// get the next waypointindex
			Waypoint nextWaypoint = mContainer.getRoute().getCurrentSegment()
					.setNextWaypoint();
			// if waypoint is null then get the next segment
			if (nextWaypoint == null) {
				Segment nextSegment = mContainer.getRoute().setNextSegment();
				// if the nextSegment is null then we have reached the end of
				// the route
				if (nextSegment == null) {
					message = "Journey ended.";
					fJourneyEnded = true;
				} else {
					message = "Segment ended.";
					nextWaypoint = nextSegment.getCurrentWaypoint();
					if (mContainer.getPebbleUiController() == null)
						System.out.println("DBG TimeOnlyService uicontroller is null");
					mContainer.getPebbleUiController().initializeList();
					int secondLastWpIndex = nextSegment.getWaypointList()
							.size() - 2;
					// TODO: Test with real time
					// long timeToAlarm =
					// timeStringToDate(mContainer.getRoute().getDate() +
					// " "+nextSegment.getWaypointList().get(secondLastWpIndex).getWaypointTime()).getTime();
					long timeToAlarm = (secondLastWpIndex + 1)
							* Constants.TEST_TIME + System.currentTimeMillis();
					System.out.println("DBG TimeOnlyService scheduling getOffAlarm");
					scheduleGetOffAlarm(timeToAlarm);
				}
			}

			if (nextWaypoint != null) {
				// set the alarm for the next waypoint
				mContainer.getPebbleUiController().updateList();
				long timeToNextWaypoint = timeStringToDate(
						mContainer.getRoute().getDate() + " "
								+ nextWaypoint.getWaypointTime()).getTime();
				scheduleNextAlarm(timeToNextWaypoint);
				message = "Next waypoint is: " + nextWaypoint.getWaypointName();
			}

			System.out.println("DBG TimeOnlyService NextWaypointReceiver.onReceive cp");

			// send an Intent to MainActivity
			Intent vi = new Intent();
			vi.putExtra(Constants.ACTION_ROUTE_EVENT_EXTRA_MESSAGE, message);
			vi.setAction(Constants.ACTION_ROUTE_EVENT);
			sendBroadcast(vi);

			Toast toast = Toast.makeText(mContext, message, Toast.LENGTH_SHORT);
			toast.show();
			
			// stop the service when the journey ends
			if (fJourneyEnded == true) {
				((TimeOnlyService)mContext).stopSelf();
			}
		}

	}

	// causes the phone to vibrate.
	class VibrateAndMakeNotificationReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			System.out.println("DBG TimeOnlyService received getoffalarm");
			mContainer.getPebbleUiController().alarmGetOff();
			Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
			vibrator.vibrate(250);

		}

	}

	@Override
	public IBinder onBind(Intent intent) {
		// no bind to this service
		return null;
	}

}
