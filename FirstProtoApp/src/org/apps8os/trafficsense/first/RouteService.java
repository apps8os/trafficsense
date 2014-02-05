package org.apps8os.trafficsense.first;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;




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
		long alarmTime = timeStringToTime(route.getSegmentList().get(0).getWaypointList().get(0).getWaypointTime()).getTime();
	}

	private Date timeStringToTime(String timeStr){
		Date date = null;
		try {
			date = new SimpleDateFormat("EEEE dd.M.yyyy kk:mm", Locale.ENGLISH).parse(timeStr);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return date;
	}

}
