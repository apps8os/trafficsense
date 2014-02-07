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

	Context context;
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public void onCreate(){
		super.onCreate();
		context=this;
		ServiceSingleton container=ServiceSingleton.getInstance();
		Route route = container.getRoute();
		PebbleUiController pebbleUi = container.getPebbleUiController();
		//gets the time of the first waypoint
		long timeToNextWaypoint = timeStringToDate(route.getDate() + " "+ route.getSegmentList().get(0).getWaypointList().get(0).getWaypointTime()).getTime();
		if(timeToNextWaypoint<System.currentTimeMillis()){
			Toast toast = Toast.makeText(context, "Error:next waypoint is in the past", 5);
			toast.show();
			this.stopSelf();
		}
		
		//register our broadcast receiver
		registerReceiver(new NextWaypointReceiver(), new IntentFilter("traffisense.NextWaypointAlarm"));
		
		//set the timer for first waypoint
		Intent i = new Intent(getApplicationContext(),RouteService.class);
		i.setAction("trafficsense.NextWaypointAlarm");
		PendingIntent o = PendingIntent.getActivity(getBaseContext(), 0, i, Intent.FLAG_ACTIVITY_NEW_TASK);
		AlarmManager am = (AlarmManager) getBaseContext().getSystemService(ALARM_SERVICE);
		am.set(AlarmManager.RTC_WAKEUP,  timeToNextWaypoint, o);
		Segment currentSegment = container.getRoute().getCurrentSegment();
		container.getPebbleUiController().initializeList(currentSegment);
	}
	
	public void onDestroy(){
		try{
			unregisterReceiver(new NextWaypointReceiver());
		}catch(Exception e){
			
		}
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
			ServiceSingleton container=ServiceSingleton.getInstance();
			//get the next waypoint
			Waypoint nextWaypoint = container.getRoute().getCurrentSegment().setNextWaypoint();
			//if waypoint is null then get the next segment
			if(nextWaypoint==null){
				Segment nextSegment = container.getRoute().setNextSegment();
				//if the nextSegment is null then we have reached the end of the route
				if(nextSegment == null){
					return;
				}
				
				Toast toast = Toast.makeText(context, "Segment ended", 5);
				toast.show();
				
				nextWaypoint=nextSegment.getCurrentWaypoint();
				container.getPebbleUiController().initializeList(nextSegment);
				
			}
			
			//set the alarm for the next waypoint
			long timeToNextWaypoint=timeStringToDate(container.getRoute().getDate() + " "+nextWaypoint.getWaypointTime()).getTime();
			Intent i = new Intent(getApplicationContext(),RouteService.class);
			i.setAction("trafficsense.NextWaypointAlarm");
			PendingIntent o = PendingIntent.getActivity(getBaseContext(), 0, i, Intent.FLAG_ACTIVITY_NEW_TASK);
			//set the alarm manager
			AlarmManager am = (AlarmManager) getBaseContext().getSystemService(ALARM_SERVICE);
			am.set(AlarmManager.RTC_WAKEUP,  timeToNextWaypoint, o);
			
			Toast toast = Toast.makeText(context, "Next waypoint is: "+nextWaypoint.getWaypointName(), 5);
			toast.show();
		}	
	}

	

}


