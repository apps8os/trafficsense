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
import com.google.android.gms.location.LocationClient.OnRemoveGeofencesResultListener;
import com.google.android.gms.location.LocationStatusCodes;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.CursorIndexOutOfBoundsException;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Vibrator;

/**
 * Service that follows a journey based on location. 
 */
public class LocationOnlyService extends Service implements 
		ConnectionCallbacks,
		OnConnectionFailedListener,
		OnAddGeofencesResultListener,
		OnRemoveGeofencesResultListener {

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
	 * PendingIntent for GeoFence.
	 */
	private PendingIntent mGeofencePendingIntent;
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
	 * For callbacks from GeoFence. 
	 */
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

		Intent i = new Intent();
		i.setAction(Constants.ACTION_NEXT_GEOFENCE_REACHED);
		mGeofencePendingIntent = PendingIntent.getBroadcast(mContext, 1, i, 
				PendingIntent.FLAG_UPDATE_CURRENT);

		errorOnStart = false;
	}

	/**
	 * Start rolling the service.
	 * 
	 * The service stops when:
	 * 1) Android kills it.
	 * 2) {@link #errorOnStart} is true
	 * 3) When the journey ends.
	 * 
	 * NOTE: Availability of Google Play/Location Services is implicitly
	 * verified in {@link #onAddGeofencesResult(int, String[])}
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
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
			stopSelf();
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
			// Remove notifications
			NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			mNotificationManager.cancel(Constants.NOTIFICATION_ID);
			// Unregister broadcast receivers
			unregisterReceiver(mEnteredWaypointAlertReceiver);
			/**
			 *  Remove GeoFences.
			 *  LocationOnlyService implements OnRemoveGeofencesResultListener.
			 */
			mLocationClient.removeGeofences(mGeofencePendingIntent, this);
			// Tell anyone listening that the route has ended
			Intent vi = new Intent();
			vi.putExtra(Constants.ROUTE_STOPPED, "");
			vi.setAction(Constants.ACTION_ROUTE_EVENT);
			sendBroadcast(vi);
			
			mContainer.serviceDetach();
		}
	}

	/**
	 * Adds a list of GeoFences to the location client.
	 * The action taken when GeoFence transition is made is currently hard coded.
	 * 
	 * @param list list of GeoFences to be added
	 */
	private void addGeofence(ArrayList<Geofence> list){
		
		// If the location client is connected send the request
		if(mLocationClient.isConnected()){
			System.out.println("DBG adding GeoFence list");
			// LocationOnlyService implements OnAddGeofencesResultListener.
			mLocationClient.addGeofences(list, mGeofencePendingIntent, this);
		}
	}
	
	/**
	 * Returns a GeoFence made from a waypoint
	 * 
	 * @param busStop get the longitude and latitude of the waypoint
	 * @param id id of the GeoFence
	 * @param radius the radius of the GeoFence
	 * @param expiryDuration how long the GeoFence exists
	 * @param transition what type of transition triggers alert
	 * @return a GeoFence created using the method parameters
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
	 * Called when we successfully connected to LocationClient.
	 */
	@Override
	public void onConnected(Bundle connectionHint) {
		//System.out.println("DBG LocationOnlyService GeoFencing enabled");
		setGeofencesForRoute();
		sendNextWaypointIntent(null);
		mContainer.getPebbleUiController().update();
		Intent coordsReadyIntent = new Intent().setAction(Constants.ACTION_COORDS_READY);
		sendBroadcast(coordsReadyIntent);
		
	}

	/**
	 * Set the GeoFences for all the waypoints on the route.
	 */
	private void setGeofencesForRoute() {
		System.out.println("DBG location service received intent");
		ArrayList<Geofence> listOfFences = new ArrayList<Geofence>();
		for (int segmentIndex = 0;
				segmentIndex < mRoute.getSegmentList().size();
				segmentIndex++) {
			Segment currentSegment = mRoute.getSegment(segmentIndex);
			/**
			 * TODO: skip the first waypoint because it it also the last one in
			 * the last segment
			 */
			for (int waypointIndex = 0;
					waypointIndex < currentSegment.getWaypointList().size();
					waypointIndex++) {
				Waypoint nextWaypoint = currentSegment
						.getWaypoint(waypointIndex);

				if (nextWaypoint.hasCoord() == false) {
					// This waypoint has no valid GPS coordinates, ignore it.
					continue;
				}
				
				System.out.println("DBG making GeoFence for " + segmentIndex + "," + waypointIndex);
				String id = Integer.toString(segmentIndex) + ","
						+ Integer.toString(waypointIndex);
				mNextBusStopGeofence = createGeofence(nextWaypoint, id,
						Constants.GEOFENCE_RADIUS,
						Geofence.NEVER_EXPIRE,
						Geofence.GEOFENCE_TRANSITION_ENTER);
				listOfFences.add(mNextBusStopGeofence);
			}
		}
		addGeofence(listOfFences);
	}
	
	/**
	 * Callback for results from adding GeoFences.
	 */
	@Override
	public void onAddGeofencesResult(int statusCode, String[] geofenceRequestIds) {
		System.out.println("DBG: addGeofences status code: " + statusCode);
		switch (statusCode) {
		case LocationStatusCodes.SUCCESS:
			break;
		case LocationStatusCodes.GEOFENCE_NOT_AVAILABLE:
			sendErrorAndExit("Error: likely some Android settings prevented usage");
			break;
		case LocationStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES:
			sendErrorAndExit("Error: too many GeoFences");
			break;
		case LocationStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS:
			sendErrorAndExit("Error: too many PendingIntents");
			break;
		default:
			sendErrorAndExit("Error: unknown GeoFence error");
			break;
		}

		StringBuffer dbgBuf = new StringBuffer();
		for (int i = 0; i < geofenceRequestIds.length; i++) {
			dbgBuf.append(" ");
			dbgBuf.append(geofenceRequestIds[i]);
		}
		String dbg = dbgBuf.toString();
		System.out.println("DBG GeoFences added: " + dbg);
	}
	
	/**
	 * Called if we are unable to connect to Google Play client.
	 */
	@Override
	public void onConnectionFailed(ConnectionResult result) {
		sendErrorAndExit("Error connecting to Google Play client");
	}

	/**
	 * Called if mLocationClient is disconnected.
	 */
	@Override
	public void onDisconnected() {
		sendErrorAndExit("Error: GPS signal lost");
	}
	
	/**
	 * A receiver for receiving Entered Waypoint Alerts.
	 * This is triggered when we enter a GeoFence.
	 */
	private class EnteredWaypointAlertReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context arg0, Intent arg1) {
			// This Intent could also indicate an error
			if (LocationClient.hasError(arg1) == true) {
				// TODO error handling
				switch (LocationClient.getErrorCode(arg1)) {
				case LocationStatusCodes.GEOFENCE_NOT_AVAILABLE:
					System.out.println("DBG EnteredWaypoint GEOFENCE_NOT_AVAILABLE");
					break;
				}
				return;
			}
			Geofence curGeofence = LocationClient.getTriggeringGeofences(arg1)
					.get(0);
			/**
			 * Get the id of the GeoFence that triggered the alert and
			 * increment it to get the next index.
			 */
			String id = curGeofence.getRequestId();
			String parts[] = id.split(",");
			mRouteSegmentIndex = Integer.parseInt(parts[0]);
			mSegmentWaypointIndex = Integer.parseInt(parts[1]) + 1;
			
			System.out.println("DBG EnteredWaypoint iSeg:" + mRouteSegmentIndex + " iWp:" + mSegmentWaypointIndex);

			Segment currentSegment = mRoute.setNextSegment(
					mRouteSegmentIndex);
			
			Waypoint nextWaypoint;
			try {
				nextWaypoint = currentSegment.setNextWaypoint(mSegmentWaypointIndex);
			} catch (CursorIndexOutOfBoundsException e) {
				nextWaypoint = null;
			}
			// Segment has ended
			if (nextWaypoint == null) {
				//System.out.println("DBG EnteredWaypoint segment ended");
				// Advance to next segment
				mRouteSegmentIndex++;
				try {
					currentSegment = mRoute.setNextSegment(mRouteSegmentIndex);
				} catch (CursorIndexOutOfBoundsException e) {
					//System.out.println("DBG EnteredWaypoint journey ended");
					// No more next segment: the journey is ended.
					mRouteSegmentIndex = -1;
					mSegmentWaypointIndex = -1;
					mRoute.setJourneyEnded(true);
					currentSegment = null;
				}
				if (currentSegment != null) {
					//System.out.println("DBG EnteredWaypoint entered next segment");
					mSegmentWaypointIndex = 0;
					nextWaypoint = currentSegment.setNextWaypoint(mSegmentWaypointIndex);
				}
			}
			
			if (nextWaypoint != null) {
				System.out.println("DBG EnteredWaypoint next waypoint: " + nextWaypoint.getWaypointName());
			}

			// Update the pebble UI
			mContainer.getPebbleUiController().update();
			
			((LocationOnlyService) mContext).makeNotificationAndAlert();
			((LocationOnlyService) mContext).sendNextWaypointIntent("");
			
			/**
			 * TODO: journey may be left un-ended if the last segment is a
			 * walking segment since we will not receive any new
			 * EnteredWaypoint event.
			 */
			if (mRoute.isJourneyEnded() == true) {
				// Stop the service.
				((LocationOnlyService) mContext).stopSelf();
			}
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
	 * Updates and shows a notification.
	 * If we are on the second last waypoint makes the phone vibrate.
	 * Protected instead of private beacase {@link EnteredWaypointAlertReceiver} needs this. 
	 */
	protected void makeNotificationAndAlert() {
		// Build the message
		String msg = OutputLogic.getJourneyProgressMessage();
		
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
		
		// Vibrate if user needs to get off at next waypoint.
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
		stopSelf();
	}

	/**
	 * Handle results of GeoFences' removal by PendingIntent.
	 */
	@Override
	public void onRemoveGeofencesByPendingIntentResult(int statusCode,
			PendingIntent pendingIntent) {
		switch (statusCode) {
		case LocationStatusCodes.SUCCESS:
			System.out.println("DBG LocationOnlyService GeoFences removed");
			break;
		case LocationStatusCodes.GEOFENCE_NOT_AVAILABLE:
			System.out.println("DBG LocationOnlyService GeoFence remove status: NOT_AVAILABLE");
			break;
		case LocationStatusCodes.ERROR:
			System.out.println("DBG LocationOnlyService GeoFence remove status: ERROR");
			break;
		}
	}

	/**
	 * Unused.
	 * @throws RuntimeException if called.
	 */
	@Override
	public void onRemoveGeofencesByRequestIdsResult(int statusCode,
			String[] geofenceRequestIds) {
		throw new RuntimeException("LocationOnlyService.onRemoveGeofencesByRequestIdsResult called ?!");
	}

}


