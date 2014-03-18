package org.apps8os.trafficsense.android;

import java.util.ArrayList;
import java.util.List;

import org.apps8os.trafficsense.TrafficsenseContainer;
import org.apps8os.trafficsense.core.OutputLogic;
import org.apps8os.trafficsense.core.Route;
import org.apps8os.trafficsense.core.Segment;
import org.apps8os.trafficsense.core.Waypoint;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationClient.OnAddGeofencesResultListener;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Vibrator;


public class LocationOnlyService extends Service implements 
		ConnectionCallbacks,
		OnConnectionFailedListener,
		OnAddGeofencesResultListener{

	private int GEOFENCE_RADIUS = 100;
	//container that contains route information and else-
	private TrafficsenseContainer mContainer; 
	//index of which segment we are in
	private int mRouteSegmentIndex=0;
	//index of which waypoint we are on
	private int mSegmentWaypointIndex=0;
	//holds a geofrence
	private Geofence mNextBusStopGeofence;
	//holds a locationClient
	private LocationClient mLocationClient;
	//action that happens when geofence tranistion detected by locationClient
	private String ACTION_NEXT_GEOFENCE_REACHED = "trafficesense.nextGeofenceAlarm";
	private Context mContext;
	//callbacks by geofence will be made to these classes 
	private LocationClient.OnAddGeofencesResultListener mOnAddGeofencesListener;
	private EnteredWaypointAlertReceiver mEnteredWaypointAlertReceiver = new EnteredWaypointAlertReceiver();
	//the id of the notification we are giving to the users


	
	
	/**
	 * Called when the service is created
	 */
	@Override
	public void onCreate(){
		super.onCreate();
		mContainer=TrafficsenseContainer.getInstance();
		
		mLocationClient=new LocationClient(this, this, this);
		mContext=this;
		//this class implements the onAddGeofenceListener
		mOnAddGeofencesListener=this;
		registerReceiver(mEnteredWaypointAlertReceiver, new IntentFilter(ACTION_NEXT_GEOFENCE_REACHED));
		//TODO: need to check that google play service are available

	}
	/**
	 * Called when the service is started
	 * @param intent
	 * @param flags
	 * @param startId
	 * @return
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		//successfully connecting to the client also adds all the geofences
		// TODO: should check return value!!
		mContainer.serviceAttach(getApplicationContext());
		mLocationClient.connect();
		// TODO: check that we can indeed handle service restart.
		//return START_STICKY;
		return START_NOT_STICKY;
	}
	
	/**
	 * called when the service is destroyed
	 */
	@Override
	public void onDestroy(){
		super.onDestroy();
		//need to detach from container
		mContainer.serviceDetach();
		//need to unregister receivers
		unregisterReceiver(mEnteredWaypointAlertReceiver);
	}

	/**
	 * adds a geofence to the location client. The action taken when 
	 * geofence transition is made is currently hardcoded.
	 * @param newFence
	 */
	private void addGeofence(ArrayList<Geofence> list){
		Intent i = new Intent();
		i.setAction(ACTION_NEXT_GEOFENCE_REACHED);
		PendingIntent pi = PendingIntent.getBroadcast(mContext, 1, i, 
				PendingIntent.FLAG_CANCEL_CURRENT);
		//if the location client is connected send the request
		if(mLocationClient.isConnected()){
			System.out.println("DBG adding geofence list");
			mLocationClient.addGeofences(list, pi, mOnAddGeofencesListener);
		}
		else{
			//TODO: figure out what happens if location client is not connected
		}
	}
	
	/**
	 * Returns a Geofence made from a waypoint
	 * 
	 * @param busStop -get the longitude and latitude of the waypoint
	 * @param id -id of the geofence
	 * @param radius -the radius of the geofence
	 * @param expiryDuration -how long the geofence exists
	 * @param transition -what type of transition triggers alert
	 * @return a geofence created using the method parameters
	 */
	private Geofence createGeofence(Waypoint busStop,String id, float radius, 
			long expiryDuration, int transition){
		System.out.println("DBG longitude: "+ busStop.getLongitude()+"latitude: "+ busStop.getLatitude());
		return new Geofence.Builder()
			.setRequestId(id)
			.setTransitionTypes(transition)
			.setCircularRegion(busStop.getLatitude(), busStop.getLongitude(), radius)
			.setExpirationDuration(expiryDuration)
			.build();
	}
	

	
	@Override
	/**
	 * When we connect to the locationClient we need to add the current geofence
	 */
	public void onConnected(Bundle connectionHint) {
		
		//System.out.println("DBG LocationOnlyService GeoFencing disabled");
		setGeofencesForRoute();
		sendNextWaypointIntent(null);
		mContainer.getPebbleUiController().initializeList();
		Intent coordsReadyIntent = new Intent().setAction(Constants.ACTION_COORDS_READY);
		this.sendBroadcast(coordsReadyIntent);
		
	}

	/**
	 * Sets the geofences for all the waypoints on the route
	 */
	public void setGeofencesForRoute(){
		System.out.println("DBG location service received intent");
		Route currentRoute = mContainer.getRoute();
		ArrayList<Geofence> listOfFences = new ArrayList<Geofence>();
		for(int segmentIndex=0;;segmentIndex++){
			Segment currentSegment = currentRoute.getSegment(segmentIndex);
			if(currentSegment == null){
				break;
			}
			//TODO: check if the segment is a walking segment and if it is only set the last waypoint in it 
			
			//skip the first waypoint because it it also the last one in the last segment
			for(int waypointIndex=0;;waypointIndex++){
				
				Waypoint nextWaypoint = currentSegment.getWaypoint(waypointIndex);
				if(nextWaypoint==null){
					break;
				}
				System.out.println("DBG making geofence for " + segmentIndex +"," + waypointIndex);
				String id = (new Integer(segmentIndex)).toString()+","+(new Integer(waypointIndex)).toString();
				mNextBusStopGeofence = createGeofence(nextWaypoint, id, GEOFENCE_RADIUS,
						Geofence.NEVER_EXPIRE, Geofence.GEOFENCE_TRANSITION_ENTER);
				listOfFences.add(mNextBusStopGeofence);
				
			}
			
		}
		addGeofence(listOfFences);		
	}
	
	//TODO: add check to status code
	public void onAddGeofencesResult(int statusCode, String[] geofenceRequestIds) {
		System.out.println("DBG: geofence status code: "+ statusCode);
		String dbg="";
		for(int i=0;i<geofenceRequestIds.length; i++){
			dbg= dbg + " " + geofenceRequestIds[i];
		}
		System.out.println("DBG: geofences added: " + dbg);
	}
	
	@Override
	/**
	 * called if we fail to connect to googlplay client
	 */
	public void onConnectionFailed(ConnectionResult result) {
		// TODO Auto-generated method stub
	}

	@Override
	/**
	 * called if locationClient is disconnected
	 */
	public void onDisconnected() {
		// TODO Auto-generated method stub
	}
	
	/**
	 * A receiver for receiving alerts. Meant to receive enteredWaypointAlerts
	 * @author traffisense
	 *
	 */
	class EnteredWaypointAlertReceiver extends BroadcastReceiver{

		@Override
		/**
		 * Receives intents when a geofence is entered
		 * @param arg0
		 * @param arg1
		 */
		public void onReceive(Context arg0, Intent arg1) {
			//the intent could also have been sent to indicate an error
			if(mLocationClient.hasError(arg1)==true){
				return; //TODO: again figure out what happens on error
			}
			Geofence curGeofence = mLocationClient.getTriggeringGeofences(arg1).get(0);
			//get the id of the geofence that triggered the alert and increment it to get the next index
			String id = curGeofence.getRequestId();
			String parts[] = id.split(",");
			mRouteSegmentIndex=Integer.parseInt(parts[0]);
			mSegmentWaypointIndex=Integer.parseInt(parts[1])+1;
			
			Segment currentSegment = mContainer.getRoute().setNextSegment(mRouteSegmentIndex);
			Waypoint nextWaypoint = currentSegment.setNextWaypoint(mSegmentWaypointIndex);
			if (mSegmentWaypointIndex == 1) {
				// Update pebble when at first waypoint
				mContainer.getPebbleUiController().initializeList();
			}
			if(nextWaypoint == null){
				
				mRouteSegmentIndex++;
				currentSegment = mContainer.getRoute().setNextSegment(mRouteSegmentIndex);
				
				
				if(currentSegment==null){
					mRouteSegmentIndex=-1;
					mSegmentWaypointIndex=-1;
					//inform clients that waypoint has changed
					sendNextWaypointIntent("");
					return; //the route has ended
				}
				mContainer.getPebbleUiController().initializeList();
				mContainer.getPebbleUiController().alarmGetOff();
				mSegmentWaypointIndex=0;
				nextWaypoint = currentSegment.setNextWaypoint(0);
			} else {
				// If we are not at the last waypoint, only update the list on pebble
				mContainer.getPebbleUiController().updateList();
			}

			
			//inform clients that the next waypoint has changed. 
			if(nextWaypoint.getWaypointName()!=null)
				System.out.println("DBG Next position is " + nextWaypoint.getWaypointName());
			makeNotificationAndAlert();
			sendNextWaypointIntent("");
		}	
	}
	

	/**
	 * sends an intent that indicates current waypoint information has been updated. 
	 * It can contain a message but it is currently unused. 
	 * @param message
	 */
	
	protected void sendNextWaypointIntent(String message){
		Intent vi = new Intent();
		if(message == null){
			message ="";
		}
		vi.putExtra(Constants.ACTION_ROUTE_EVENT_EXTRA_MESSAGE, message);
		vi.setAction(Constants.ACTION_ROUTE_EVENT);
		sendBroadcast(vi);
	}
	
	/**
	 * Shows and updates the notification and if we are on the second last waypoint makes the phone vibrate. 
	 */
	
	protected void makeNotificationAndAlert(){
		
		//display last notification
		String msg = OutputLogic.getOutput();                 //TODO: I think we need to add an icon here to make this work. 
		Notification noti = new Notification.Builder(mContext)
				.setContentTitle("Trafficsense Route Tracking")
				.setContentText(msg)
				.build();
		NotificationManager mNotificationManager =(NotificationManager) getSystemService(mContext.NOTIFICATION_SERVICE);
		mNotificationManager.notify(Constants.NOTIFICATION_ID, noti);
		
		//vibrate if user needs to get off at next waypoint
		Segment curSegment = mContainer.getRoute().getCurrentSegment();
		int curWaypointIndex = mContainer.getRoute().getCurrentSegment().getCurrentIndex();
		List<Waypoint> waypointList = mContainer.getRoute().getCurrentSegment().getWaypointList();
		
		if((curWaypointIndex == waypointList.size() -1) & !curSegment.isWalking()){
			Vibrator vib = (Vibrator) getSystemService(mContext.VIBRATOR_SERVICE);
			vib.vibrate(Constants.VIBRATOR_DURATION);
		}
	}
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

}


