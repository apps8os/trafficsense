package org.apps8os.trafficsense.android;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.apps8os.trafficsense.core.Route;
import org.apps8os.trafficsense.core.Segment;
import org.apps8os.trafficsense.core.TrafficsenseContainer;
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
public class TimeOnlyService extends Service{

	private TrafficsenseContainer mContainer;
	private Route mRoute;
	private NextWaypointReceiver mNextWaypointReceiver;
	private VibrateAndMakeNotificationReceiver mMakeAlertReceiver;
	private Context mContext;
	private AlarmManager mAM;

	public void onCreate(){
		super.onCreate();
		System.out.println("DBG RouteService.onCreate");
		mContext=this;
		mContainer=TrafficsenseContainer.getInstance();
		mRoute = mContainer.getRoute();
		mNextWaypointReceiver= new NextWaypointReceiver();
		mMakeAlertReceiver = new VibrateAndMakeNotificationReceiver();
		mAM = (AlarmManager) getSystemService(ALARM_SERVICE);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		boolean errorOnStart = false;

		registerReceiver(mNextWaypointReceiver, new IntentFilter(Constants.ACTION_NEXT_WAYPOINT));
		registerReceiver(mMakeAlertReceiver, new IntentFilter(Constants.ACTION_MAKE_ALERT));

		if (mRoute == null) {
			System.out.println("DBG route = null");
			errorOnStart = true;
		} else {
			//gets the time of the first waypoint
			long timeToNextWaypoint = timeStringToDate(mRoute.getDate() + " "+ mRoute.getSegmentList().get(0).getWaypointList().get(0).getWaypointTime()).getTime();

			// TODO do not check this at the moment
			/*
			if(timeToNextWaypoint < System.currentTimeMillis()){
				System.out.println("DBG next waypoint in the past");
				Toast toast = Toast.makeText(mContext, "Error:next waypoint is in the past", Toast.LENGTH_SHORT);
				toast.show();
				errorOnStart = true;
			} else {
			 */
			scheduleNextAlarm(timeToNextWaypoint);
			//}
		}


		// TODO do we still need these?
		//Segment currentSegment = mContainer.getRoute().getCurrentSegment();
		mContainer.getPebbleUiController().initializeList();

		System.out.println("DBG RouteService.onStartCommand cp");
		// We want this service to continue running until it is explicitly
		// stopped, so return sticky.

		if (errorOnStart) {
			this.stopSelf();
			return START_NOT_STICKY;
		}
		return START_STICKY;
	}

	public void onDestroy(){
		PendingIntent pi;

		System.out.println("DBG RouteService.onDestroy");
		Intent i = new Intent();
		i.setAction(Constants.ACTION_NEXT_WAYPOINT);
		pi = PendingIntent.getBroadcast(mContext, 0, i, PendingIntent.FLAG_CANCEL_CURRENT);
		mAM.cancel(pi);
		i.setAction(Constants.ACTION_MAKE_ALERT);
		sendBroadcast(i);
		pi = PendingIntent.getBroadcast(mContext, 1, i, PendingIntent.FLAG_CANCEL_CURRENT);
		mAM.cancel(pi);
		unregisterReceiver(mNextWaypointReceiver);
		unregisterReceiver(mMakeAlertReceiver);
	}

	private void scheduleNextAlarm(long atMillis) {
		Intent i = new Intent();
		i.setAction(Constants.ACTION_NEXT_WAYPOINT);
		PendingIntent pi = PendingIntent.getBroadcast(mContext, 0, i, PendingIntent.FLAG_CANCEL_CURRENT);
		//mAM.set(AlarmManager.RTC_WAKEUP,  atMillis, pi);
		// TODO testing use
		mAM.set(AlarmManager.RTC_WAKEUP,  System.currentTimeMillis()+Constants.TEST_TIME, pi);
	}

	private void scheduleGetOffAlarm(long atMillis) {
		Intent i = new Intent();
		i.setAction(Constants.ACTION_MAKE_ALERT);
		sendBroadcast(i);
		PendingIntent pi = PendingIntent.getBroadcast(mContext, 1, i, PendingIntent.FLAG_CANCEL_CURRENT);
		mAM.set(AlarmManager.RTC_WAKEUP,  atMillis, pi);
		System.out.println("DBG scheduled getOffAlarm with intent" + pi);
	}

	private Date timeStringToDate(String timeStr){
		Date date = null;
		try {
			date = new SimpleDateFormat("EEEE dd.M.yyyy kk:mm", Locale.ENGLISH).parse(timeStr);
		} catch (ParseException e) {
			System.out.println("DBG timeStringToDate error: "+e.getMessage());
		}
		return date;
	}

	/**
	 * Broadcast receiver that receives intents indicating that next stop has been reached.
	 *If it is the last waypoint it set the next segment as the current segment and set the timer
	 *for it. In any case it gets the next waypoint
	 *in the segment and sets the alarm to indicate when we are there. 
	 */
	class NextWaypointReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context arg0, Intent arg1) {
			System.out.println("DBG NextWaypointReceiver.onReceive" + arg1.getAction());
			String message = "ERROR: No message set in onReceive()";

			//get the next waypointindex
			Waypoint nextWaypoint = mContainer.getRoute().getCurrentSegment().setNextWaypoint();
			//if waypoint is null then get the next segment
			if(nextWaypoint==null) {
				Segment nextSegment = mContainer.getRoute().setNextSegment();
				//if the nextSegment is null then we have reached the end of the route
				if(nextSegment == null){
					message = "Journey ended.";
				} else {
					message = "Segment ended.";
					nextWaypoint = nextSegment.getCurrentWaypoint();
					if (mContainer.getPebbleUiController() == null)
						System.out.println("DBG uicontroller is null");
					mContainer.getPebbleUiController().initializeList();
					int secondLastWpIndex = nextSegment.getWaypointList().size() - 2;
					// TODO: Test with real time
					//long timeToAlarm = timeStringToDate(mContainer.getRoute().getDate() + " "+nextSegment.getWaypointList().get(secondLastWpIndex).getWaypointTime()).getTime();
					long timeToAlarm = (secondLastWpIndex + 1) * Constants.TEST_TIME + System.currentTimeMillis();
					System.out.println("DBG scheduling getOffAlarm");
					scheduleGetOffAlarm(timeToAlarm);

				}
			}

			if (nextWaypoint != null) {
				//set the alarm for the next waypoint
				mContainer.getPebbleUiController().updateList();
				long timeToNextWaypoint=timeStringToDate(mContainer.getRoute().getDate() + " "+nextWaypoint.getWaypointTime()).getTime();
				scheduleNextAlarm(timeToNextWaypoint);
				message = "Next waypoint is: " + nextWaypoint.getWaypointName();
			}

			System.out.println("DBG NextWaypointReceiver.onReceive cp");

			// send an Intent to MainActivity
			Intent vi = new Intent();
			vi.putExtra(Constants.ACTION_ROUTE_EVENT_EXTRA_MESSAGE, message);
			vi.setAction(Constants.ACTION_ROUTE_EVENT);
			sendBroadcast(vi);

			Toast toast = Toast.makeText(mContext, message, Toast.LENGTH_SHORT);
			toast.show();
		}

	}

	//causes the phone to vibrate. 
	class VibrateAndMakeNotificationReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			System.out.println("DBG received getoffalarm");
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


