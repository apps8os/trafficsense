package org.apps8os.trafficsense.first;

import java.util.ArrayList;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationClient.OnAddGeofencesResultListener;
import com.google.android.gms.location.LocationClient.OnRemoveGeofencesResultListener;

import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.IBinder;

public class LocationService extends Service implements 
		ConnectionCallbacks,
		OnConnectionFailedListener,
		OnAddGeofencesResultListener,
		OnRemoveGeofencesResultListener{

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
	//action that happens when geofence tranistion detected
	private String ACTION_NEXT_GEOFENCE_REACHED = "trafficesense.nextGeofenceAlarm";
	private Context mContext;
	//callbacks by geofence will be made to this class
	private LocationClient.OnAddGeofencesResultListener mOnAddGeofencesListener;
	private LocationClient.OnRemoveGeofencesResultListener mOnRemoveGeofencesListener;
	private EnteredWaypointAlertReceiver mEnteredWaypointAlertReceiver = new EnteredWaypointAlertReceiver();

	
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
	/**
	 * Called when the service is created
	 */
	public void onCreate(){
		mContainer=TrafficsenseContainer.getInstance();
		mLocationClient=new LocationClient(this, this, this);
		mContext=this;
		//this class implements the onAddGeofenceListener
		mOnAddGeofencesListener=this;
		mOnRemoveGeofencesListener=this;
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
	public int onStartCommmand(Intent intent, int flags, int startId){
		//successfully connecting to the client also adds the geofence for the next position
		mLocationClient.connect();
		return super.onStartCommand(intent,flags,startId);
	}
	
	/**
	 * called when the service is destroyed
	 */
	public void onDestroy(){
		//need to unregister receivers
		unregisterReceiver(mEnteredWaypointAlertReceiver);
	}

	/**
	 * adds a geofence to the location client. The action taken when 
	 * geofence transition made is currently hardcoded.
	 * @param newFence
	 */
	private void addGeofence(Geofence newFence){
		Intent i = new Intent();
		i.setAction(ACTION_NEXT_GEOFENCE_REACHED);
		PendingIntent pi = PendingIntent.getBroadcast(mContext, 1, i, 
				PendingIntent.FLAG_CANCEL_CURRENT);
		//if the location client is connected send the request
		if(mLocationClient.isConnected()){
			ArrayList<Geofence> geoList=new ArrayList<Geofence>();
			geoList.add(newFence);
			mLocationClient.addGeofences(geoList, pi, mOnAddGeofencesListener);
		}
		else{
			//TODO: figure out what happens if location client is not connected
		}
	}
	
	/**
	 * removes a geofence from the location client
	 * @param oldGeofence
	 */
	private void removeGeofence(Geofence oldGeofence){
		if(mLocationClient.isConnected()){
			ArrayList<String> geoList=new ArrayList<String>();
			geoList.add(oldGeofence.getRequestId());
			mLocationClient.removeGeofences(geoList, mOnRemoveGeofencesListener);
		}
		else{
			//TODO: figure out what happens if location client is not connected
		}
	}
	
	/**
	 * Returns a Geofence made from a waypoint
	 * @param busStop -get the longitude and latitude of the waypoint
	 * @param id -id of the geofence
	 * @param radius -the radius of the geofence
	 * @param expiryDuration -how long the geofence exists
	 * @param transition -what type of transition triggers alert
	 * @return a geofence created using the method parameters
	 */
	private Geofence createGeofence(Waypoint busStop,String id, float radius, 
			long expiryDuration, int transition){
		return new Geofence.Builder()
			.setRequestId(id)
			.setTransitionTypes(transition)
			.setCircularRegion(busStop.getLatitude(), busStop.getLongitude(), radius)
			.setExpirationDuration(expiryDuration)
			.build();
	}


	/**
	 * A receiver for receiving alerts. Meant to receive enteredWaypointAlerts
	 * @author traffisense
	 *
	 */
	class EnteredWaypointAlertReceiver extends BroadcastReceiver{

		@Override
		/**
		 * When intent is received add the next geofence. If same id as an old one, the old
		 * geofence is replaced automatically. 
		 * @param arg0
		 * @param arg1
		 */
		public void onReceive(Context arg0, Intent arg1) {
			//the intent could also have been sent to indicate an error
			if(mLocationClient.hasError(arg1)==true){
				return; //TODO: again figure out what happens on error
			}
			
			Route currentRoute = mContainer.getRoute();
			Segment currentSegment = currentRoute.getSegment(mRouteSegmentIndex);
			//remove the current geoFence;
			mSegmentWaypointIndex++;
			Waypoint nextWaypoint = currentSegment.getWaypoint(mSegmentWaypointIndex);
			//if we are at the end of segment get the next segment
			if(nextWaypoint == null){
				mRouteSegmentIndex++;
				currentSegment = currentRoute.getSegment(mRouteSegmentIndex);
				//if no segments left then we are at end of route
				if(currentSegment == null){
					//TODO: figure out what happens when at end of route
					return;
				}
				//new segment loaded so reset the index
				mSegmentWaypointIndex=0;
				//get the new waypoint
				nextWaypoint = currentSegment.getWaypoint(mSegmentWaypointIndex);
			}
			//make a new geofence
			//NOTE: Figure out what are reasonable parameters here
			mNextBusStopGeofence = createGeofence(nextWaypoint, "nextWaypoint", 20,
						Geofence.NEVER_EXPIRE, Geofence.GEOFENCE_TRANSITION_ENTER);
			addGeofence(mNextBusStopGeofence);	
			
		}
		
	}
	
	@Override
	/**
	 * When we connect to the locationClient we need to add the current geofence
	 */
	public void onConnected(Bundle connectionHint) {
		Route currentRoute=mContainer.getRoute();
		Segment currentSegment = currentRoute.getSegment(mRouteSegmentIndex);
		Waypoint nextWaypoint = currentSegment.getWaypoint(mSegmentWaypointIndex);
		Geofence newFence = createGeofence(nextWaypoint, "nextWaypoint", 20,
				Geofence.NEVER_EXPIRE, Geofence.GEOFENCE_TRANSITION_ENTER);
		addGeofence(newFence);
		
	}

	@Override
	public void onRemoveGeofencesByPendingIntentResult(int statusCode,
			PendingIntent pendingIntent) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onRemoveGeofencesByRequestIdsResult(int statusCode,
			String[] geofenceRequestIds) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void onAddGeofencesResult(int statusCode, String[] geofenceRequestIds) {
		// TODO Auto-generated method stub
		
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
	
}
