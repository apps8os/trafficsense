package org.apps8os.trafficsense.first;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

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
public class RouteService extends Service{
	
	final static String ACTION_NEXT_WAYPOINT = "trafficsense.NextWaypointAlarm";
	TrafficsenseContainer mContainer;
	Route mRoute;
	NextWaypointReceiver mReceiver;
	Context mContext;
	
	public void onCreate(){
		super.onCreate();
		System.out.println("DBG RouteService.onCreate");
		mContext=this;
		mContainer=TrafficsenseContainer.getInstance();
		mRoute = mContainer.getRoute();
		mReceiver=new NextWaypointReceiver();
		
		if (mRoute == null) {
			System.out.println("DBG route = null");
			this.stopSelf();
		}
		//PebbleUiController pebbleUi = container.getPebbleUiController();
		//gets the time of the first waypoint

		// TODO do not check this at the moment
		/*
		long timeToNextWaypoint = timeStringToDate(mRoute.getDate() + " "+ mRoute.getSegmentList().get(0).getWaypointList().get(0).getWaypointTime()).getTime();
		if(timeToNextWaypoint<System.currentTimeMillis()){
			System.out.println("DBG next waypoint in the past");
			Toast toast = Toast.makeText(context, "Error:next waypoint is in the past", 5);
			toast.show();
			this.stopSelf();
		}
		*/
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		registerReceiver(mReceiver, new IntentFilter(ACTION_NEXT_WAYPOINT));

		long timeToNextWaypoint = timeStringToDate(mRoute.getDate() + " "+ mRoute.getSegmentList().get(0).getWaypointList().get(0).getWaypointTime()).getTime();
		scheduleNextAlarm(timeToNextWaypoint);

		//Segment currentSegment = mContainer.getRoute().getCurrentSegment();
		//mContainer.getPebbleUiController().initializeList(currentSegment);
		
		System.out.println("DBG RouteService.onStartCommand cp");
	    // We want this service to continue running until it is explicitly
	    // stopped, so return sticky.
	    return START_STICKY;
	}
	
	public void onDestr2500oy(){
		System.out.println("DBG RouteService.onDestroy");
		unregisterReceiver(mReceiver);
	}

	private void scheduleNextAlarm(long atMillis) {
		Intent i = new Intent();
		i.setAction(ACTION_NEXT_WAYPOINT);
		PendingIntent pi = PendingIntent.getBroadcast(mContext, 0, i, PendingIntent.FLAG_CANCEL_CURRENT);
		AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
		//am.set(AlarmManager.RTC_WAKEUP,  atMillis, pi);
		// TODO testing use
		am.set(AlarmManager.RTC_WAKEUP,  System.currentTimeMillis()+5000, pi);
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
			System.out.println("DBG NextWaypointReceiver.onReceive");
			String message = "ERROR: No message set in onReceive()";
			
			//get the next waypoint
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
			vi.putExtra(MainActivity.ACTION_ROUTE_EVENT_EXTRA_MESSAGE, message);
			vi.setAction(MainActivity.ACTION_ROUTE_EVENT);
			sendBroadcast(vi);
						
			Toast toast = Toast.makeText(mContext, message, Toast.LENGTH_SHORT);
			toast.show();			
		}
		
	}
	
	//causes the phone to vibrate. 
	class vibrateAndMakeNotification extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			
			Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
			vibrator.vibrate(250);
			
			//make notification needs to be implemented here
			
		}
		
	}
	

	@Override
	public IBinder onBind(Intent intent) {
		// no bind to this service
		return null;
	}

	

}


