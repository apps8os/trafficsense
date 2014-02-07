package org.apps8os.trafficsense.first;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.example.timerwithnotification.AlertActivity;
import com.example.timerwithnotification.ConfigActivity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;




public class RouteService extends Service{

	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public void onCreate(){
		super.onCreate();
		ServiceSingleton container=ServiceSingleton.getInstance();
		Route route = container.getRoute();
		PebbleUiController pebbleUi = container.getPebbleUiController();
		//gets the time of the first waypoint
		long timeToNextWaypoint = timeStringToDate(route.getSegmentList().get(0).getWaypointList().get(0).getWaypointTime()).getTime();
		long timeToGetOfBus = timeStringToDate(route.getSegmentList().get(0).getLastWaypoint().getWaypointTime()).getTime();
		long timeToDestination=timeStringToDate(route.getArrivalTime()).getTime();
		
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
	 *If it is the last waypoint in a segmenet it does nothing. Otherwise it gets the next waypoint
	 *in the segment and sets the alarm to indicate when we are there. 
	 */
	class NextWaypointReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context arg0, Intent arg1) {
			ServiceSingleton container=ServiceSingleton.getInstance();
			//get the next waypoint
			Waypoint nextWaypoint = container.getRoute().getCurrentSegment().setNextWaypoint();
			if(nextWaypoint==null){
				return;
			}
			long timeToNextWaypoint=timeStringToDate(nextWaypoint.getWaypointTime()).getTime();
			
			//make the intent that is sent to this service once alarm expires
			Intent i = new Intent(getApplicationContext(),RouteService.class);
			i.setAction("trafficsense.NextWaypointAlarm");
			PendingIntent o = PendingIntent.getActivity(getBaseContext(), 0, i, Intent.FLAG_ACTIVITY_NEW_TASK);
			//set the alarm manager
			AlarmManager am = (AlarmManager) getBaseContext().getSystemService(ALARM_SERVICE);
			am.set(AlarmManager.RTC_WAKEUP,  timeToNextWaypoint, o);
		}	
	}
	/**
	 * Broadcast receiver for receiving timer updates that indicate current segment has ended.
	 * It sets the next segment along with timers to indicate when it ends.
	 *
	 */
	class NextSegmentReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context arg0, Intent arg1) {
			ServiceSingleton container=ServiceSingleton.getInstance();
			//get the next segment
			Segment nextSegment = container.getRoute().setNextSegment();
			//return if there is no next segment
			if(nextSegment==null){
				return;		
			}
			//get the time the next segment ends
			long timeToNextSegment = timeStringToDate(nextSegment.getLastWaypoint().getWaypointTime()).getTime();
			Intent i = new Intent(getApplicationContext(),RouteService.class);
			i.setAction("trafficsense.NextSegmentAlarm");
			PendingIntent o = PendingIntent.getActivity(getBaseContext(), 0, i, Intent.FLAG_ACTIVITY_NEW_TASK);
			//set the alarm manager
			AlarmManager am = (AlarmManager) getBaseContext().getSystemService(ALARM_SERVICE);
			am.set(AlarmManager.RTC_WAKEUP,  timeToNextSegment, o);
		}
		
	}

}


