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
		//container.getPebbleUiController().initializeList(currentSegment);
		System.out.println("DBG RouteService.onStartCommand cp");
	    // We want this service to continue running until it is explicitly
	    // stopped, so return sticky.
	    return START_STICKY;
	}
	
	public void onDestroy(){
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
			// TODO Auto-generated catch block
			e.printStackTrace();
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
			// vibrate the phone
			Intent vi = new Intent();
			vi.setAction("myaction");
			sendBroadcast(vi);
			
			System.out.println("DBG NextWaypointReceiver.onReceive");
			//get the next waypoint
			Waypoint nextWaypoint = mContainer.getRoute().getCurrentSegment().setNextWaypoint();
			//if waypoint is null then get the next segment
			if(nextWaypoint==null){
				Segment nextSegment = mContainer.getRoute().setNextSegment();
				//if the nextSegment is null then we have reached the end of the route
				if(nextSegment == null){
					return;
				}
				
				Toast toast = Toast.makeText(mContext, "Segment ended", Toast.LENGTH_SHORT);
				toast.show();
				
				nextWaypoint=nextSegment.getCurrentWaypoint();
				if (mContainer.getPebbleUiController() == null)
					System.out.println("DBG uicontroller is null");
				mContainer.getPebbleUiController().initializeList();
			}
			mContainer.getPebbleUiController().updateList();
			System.out.println("DBG NextWaypointReceiver.onReceive cp1");
			//set the alarm for the next waypoint
			long timeToNextWaypoint=timeStringToDate(mContainer.getRoute().getDate() + " "+nextWaypoint.getWaypointTime()).getTime();
			scheduleNextAlarm(timeToNextWaypoint);
			System.out.println("DBG NextWaypointReceiver.onReceive cp2");
			
			Toast toast = Toast.makeText(mContext, "Next waypoint is: "+nextWaypoint.getWaypointName(), Toast.LENGTH_SHORT);
			toast.show();
			
		}	
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	

}


