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
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Vibrator;


/**
 * TODO: Documentation.
 */
public class LocationOnlyService extends Service implements 
		ConnectionCallbacks,
		OnConnectionFailedListener,
		OnAddGeofencesResultListener{

	/**
	 * The TrafficSense service container. 
	 */
	private TrafficsenseContainer mContainer; 
	/**
	 * Index of the segment we are currently in.
	 */
	private int mRouteSegmentIndex=0;
	/**
	 * Index of the waypoint we are currently at.
	 */
	private int mSegmentWaypointIndex=0;
	/**
	 * The GeoFence of our next stop.
	 */
	private Geofence mNextBusStopGeofence;
	/**
	 * Google Location Service client instance.
	 */
	private LocationClient mLocationClient;
	/**
	 * The context in which we are operating.
	 */
	private Context mContext;
	/**
	 * The journey we are following.
	 */
	private Route mRoute;
	/**
	 * Callbacks from GeoFence will be made to these classes. 
	 */
	private LocationClient.OnAddGeofencesResultListener mOnAddGeofencesListener;
	private EnteredWaypointAlertReceiver mEnteredWaypointAlertReceiver;
	/**
	 * Did we encounter an error during onStartCommand().
	 * If true, the service failed to start.
	 * onStartCommand() should stopSelf() and onDestroy() should
	 * not do the clean up.
	 * This would be true if the service is automatically restarted
	 * by Android.
	 */
	private boolean errorOnStart;

	
	/**
	 * Instantiate various resources.
	 */
	@Override
	public void onCreate() {
		super.onCreate();
		mContext = this;
		mContainer = TrafficsenseContainer.getInstance();
		mRoute = mContainer.getRoute();
		mLocationClient=new LocationClient(this, this, this);
		mEnteredWaypointAlertReceiver = new EnteredWaypointAlertReceiver();
		//this class implements the onAddGeofenceListener
		mOnAddGeofencesListener = this;

		errorOnStart = false;
	}

	/**
	 * Start rolling the service.
	 * 
	 * The service stops when:
	 * 1) Android kills it.
	 * 2) {@link #errorOnStart} is true
	 * 3) When the journey ends.
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO: need to check that google play service are available
		if (mContainer.serviceAttach(getApplicationContext()) == false) {
			System.out.println("DBG LocationOnlyService: unable to attach container");
			errorOnStart = true;
		} else if (mRoute == null) {
			System.out.println("DBG LocationOnlyService: mRoute = null");
			mContainer.serviceDetach();
			errorOnStart = true;
		} else if (mContainer.getPebbleUiController() == null) {
			System.out.println("DBG LocationOnlyService: Pebble UI not set up properly");
			mContainer.serviceDetach();
			errorOnStart = true;
		} else {
			registerReceiver(mEnteredWaypointAlertReceiver, new IntentFilter(
					Constants.ACTION_NEXT_GEOFENCE_REACHED));
			// Successfully connecting to the client also adds all the GeoFences.
			mLocationClient.connect();
		}

		if (errorOnStart) {
			this.stopSelf();
		}
		
		/**
		 * We are currently unable to resume operation, so do not re-create
		 * automatically.
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

		if (errorOnStart == false) {
			// remove any notifications
			NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			mNotificationManager.cancel(Constants.NOTIFICATION_ID);
			// need to unregister receivers
			unregisterReceiver(mEnteredWaypointAlertReceiver);
			// tell anyone listening that the route has ended
			Intent vi = new Intent();
			vi.putExtra(Constants.ROUTE_STOPPED, "");
			vi.setAction(Constants.ACTION_ROUTE_EVENT);
			sendBroadcast(vi);
			
			mContainer.serviceDetach();
		}
	}

	/**
	 * Adds a GeoFence to the location client.
	 * The action taken when GeoFence transition is made is currently hard coded.
	 * 
	 * @param list  (TODO what is this)
	 */
	private void addGeofence(ArrayList<Geofence> list){
		Intent i = new Intent();
		i.setAction(Constants.ACTION_NEXT_GEOFENCE_REACHED);
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
	
	
	/**
	 * When we connect to the locationClient we need to add the current geofence
	 */
	@Override
	public void onConnected(Bundle connectionHint) {
		
		//System.out.println("DBG LocationOnlyService GeoFencing disabled");
		setGeofencesForRoute();
		sendNextWaypointIntent(null);
		mContainer.getPebbleUiController().update();
		Intent coordsReadyIntent = new Intent().setAction(Constants.ACTION_COORDS_READY);
		sendBroadcast(coordsReadyIntent);
		
	}

	/**
	 * Set the GeoFences for all the waypoints on the route.
	 */
	private void setGeofencesForRoute(){
		System.out.println("DBG location service received intent");
		ArrayList<Geofence> listOfFences = new ArrayList<Geofence>();
		for(int segmentIndex=0;;segmentIndex++){
			Segment currentSegment = mRoute.getSegment(segmentIndex);
			if(currentSegment == null){
				break;
			}
			
			//skip the first waypoint because it it also the last one in the last segment
			for(int waypointIndex=0;;waypointIndex++){
				
				Waypoint nextWaypoint = currentSegment.getWaypoint(waypointIndex);
				if(nextWaypoint==null){
					break;
				}
				System.out.println("DBG making geofence for " + segmentIndex +"," + waypointIndex);
				String id = Integer.toString(segmentIndex)+","+Integer.toString(waypointIndex);
				mNextBusStopGeofence = createGeofence(nextWaypoint, id, Constants.GEOFENCE_RADIUS,
						Geofence.NEVER_EXPIRE, Geofence.GEOFENCE_TRANSITION_ENTER);
				listOfFences.add(mNextBusStopGeofence);
				
			}
			
		}
		addGeofence(listOfFences);		
	}
	
	/**
	 * TODO: documentation.
	 */
	public void onAddGeofencesResult(int statusCode, String[] geofenceRequestIds) {
		System.out.println("DBG: geofence status code: "+ statusCode);

		//TODO: check statusCode
		
		StringBuffer dbgBuf = new StringBuffer();
		for(int i=0;i<geofenceRequestIds.length; i++){
			dbgBuf.append(" ");
			dbgBuf.append(geofenceRequestIds[i]);
		}
		String dbg = dbgBuf.toString();
		
		System.out.println("DBG geofences added: " + dbg);
		if(statusCode == 1000){
			sendErrorAndExit("Error: some android setting prevents usage");
		}
		else if(statusCode != 0){
			sendErrorAndExit("Error: error adding geofences");
		}
	}
	
	/**
	 * Called if we are unable to connect to Google Play client.
	 */
	@Override
	public void onConnectionFailed(ConnectionResult result) {
		sendErrorAndExit("Error: error connecting to google play client");
	}

	/**
	 * Called if mLocationClient is disconnected.
	 */
	@Override
	public void onDisconnected() {
		sendErrorAndExit("Error: gps signal lost");
	}
	
	/**
	 * A receiver for receiving Entered Waypoint Alerts.
	 * This is triggered when we enter a GeoFence.
	 */
	private class EnteredWaypointAlertReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context arg0, Intent arg1) {
			// the intent could also have been sent to indicate an error
			if (LocationClient.hasError(arg1) == true) {
				return; // TODO: again figure out what happens on error
			}
			Geofence curGeofence = LocationClient.getTriggeringGeofences(arg1)
					.get(0);
			// get the id of the geofence that triggered the alert and increment
			// it to get the next index
			String id = curGeofence.getRequestId();
			String parts[] = id.split(",");
			mRouteSegmentIndex = Integer.parseInt(parts[0]);
			mSegmentWaypointIndex = Integer.parseInt(parts[1]) + 1;

			Segment currentSegment = mContainer.getRoute().setNextSegment(
					mRouteSegmentIndex);
			Waypoint nextWaypoint = currentSegment
					.setNextWaypoint(mSegmentWaypointIndex);
			
			// segment had ended
			if (nextWaypoint == null) {

				mRouteSegmentIndex++;
				currentSegment = mContainer.getRoute().setNextSegment(
						mRouteSegmentIndex);

				// The journey is ended.
				if (currentSegment == null) {
					mRouteSegmentIndex = -1;
					mSegmentWaypointIndex = -1;
					mRoute.setJourneyEnded(true);

					// inform clients that waypoint has changed
					((LocationOnlyService) mContext).sendNextWaypointIntent("");

					// Stop the service.
					((LocationOnlyService) mContext).stopSelf();

					return;
				}
				mSegmentWaypointIndex = 0;
				nextWaypoint = currentSegment.setNextWaypoint(0);
			}
			
			// Update the pebble UI
			mContainer.getPebbleUiController().update();

			// inform clients that the next waypoint has changed.
			if (nextWaypoint.getWaypointName() != null)
				System.out.println("DBG Next position is "
						+ nextWaypoint.getWaypointName());
			((LocationOnlyService) mContext).makeNotificationAndAlert();
			((LocationOnlyService) mContext).sendNextWaypointIntent("");
		}
	}
	

	/**
	 * Sends an Intent that indicates current waypoint information has been updated. 
	 * It may contain a message but it is currently unused. 
	 * 
	 * Protected instead of private beacase {@link EnteredWaypointAlertReceiver} needs this.
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
	 * Protected instead of private beacase {@link EnteredWaypointAlertReceiver} needs this. 
	 */
	protected void makeNotificationAndAlert(){
		
		//display last notification
		String msg = OutputLogic.getOutput();             
		int resID = getResources().getIdentifier("bus" , "drawable", getPackageName());
		
		Notification notification = null;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN ) {
			/**
			 * Notification.Builder.build() requires API level >= 16.
			 */
			notification = new Notification.Builder(mContext)
			.setContentTitle("Trafficsense Route Tracking")
			.setContentText(msg)
			.setSmallIcon(resID)
			.build();
    	} else {
    		notification = new Notification.Builder(mContext)
			.setContentTitle("Trafficsense Route Tracking")
			.setContentText(msg)
			.setSmallIcon(resID)
			.getNotification();
    	}
		
		NotificationManager mNotificationManager =(NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		mNotificationManager.notify(Constants.NOTIFICATION_ID, notification);
		
		//vibrate if user needs to get off at next waypoint
		Segment curSegment = mRoute.getCurrentSegment();
		int curWaypointIndex = mRoute.getCurrentSegment().getCurrentIndex();
		List<Waypoint> waypointList = mRoute.getCurrentSegment().getWaypointList();
		
		if((curWaypointIndex == waypointList.size() -1) & !curSegment.isWalking()){
			Vibrator vib = (Vibrator) getSystemService(VIBRATOR_SERVICE);
			vib.vibrate(Constants.VIBRATOR_DURATION);
		}
	}
	
	/**
	 * No bind to this service.
	 */
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
	
	/**
	 * Send an Intent indicating an error to listening services and stop the service.
	 * @param msg error message
	 */
	private void sendErrorAndExit(String msg){
		Intent vi = new Intent();
		vi.putExtra(Constants.ERROR, msg);
		vi.setAction(Constants.ACTION_ROUTE_EVENT);
		sendBroadcast(vi);
		this.stopSelf();
	}

}


