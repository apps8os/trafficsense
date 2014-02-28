package org.apps8os.trafficsense.second;

import java.util.ArrayList;

import org.apps8os.trafficsense.*;
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

import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;


public class LocationService extends Service implements 
		ConnectionCallbacks,
		OnConnectionFailedListener,
		OnAddGeofencesResultListener{

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
	
	//messenger used to communicate between client and service
	private Messenger mMessenger = new Messenger (new IncomingHandler());
		static final int MSG_REGISTER_CLIENT = 1;
		static final int MSG_UNREGISTER_CLIENT = 2;
		static final int MSG_GET_CURRENT_WAYPOINT = 3;
		static final int MSG_CURRENT_WAYPOINT = 4;
	private ArrayList<Messenger> mClients =new ArrayList<Messenger>();
	
	@Override
	public IBinder onBind(Intent arg0) {
		return mMessenger.getBinder();
	}
	
	/**
	 * Class to handle incoming message to the service
	 * @author traffisense
	 *
	 */
	class IncomingHandler extends Handler { // Handler of incoming messages from clients.
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MSG_REGISTER_CLIENT:
                mClients.add(msg.replyTo);
                break;
            case MSG_UNREGISTER_CLIENT:
                mClients.remove(msg.replyTo);
                break;
            case MSG_GET_CURRENT_WAYPOINT:
                try {
                	//send current segment and waypoint index to the requestee. 
					msg.replyTo.send(Message.obtain(null, MSG_CURRENT_WAYPOINT, mRouteSegmentIndex, mSegmentWaypointIndex));
				} catch (RemoteException e) {
					//the client is dead so remove them
					mClients.remove(msg.replyTo);
				}
                break;
            default:
                super.handleMessage(msg);
            }
        }
    }
	
	/**
	 * Send segment and waypoint index to clients. Meant to be used to
	 * inform clients a new waypoint has been reached
	 * @param segmentIndex
	 * @param waypointIndex
	 */
	private void sendNextWaypointMessage(int segmentIndex, int waypointIndex){
		for (int i= 0; i<mClients.size();i++){
			try {
				mClients.get(i).send(Message.obtain(null, MSG_CURRENT_WAYPOINT, segmentIndex, waypointIndex));
			} catch (RemoteException e) {
				mClients.remove(i);
			}
		}
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
		return START_STICKY;
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
	 * geofence transition is made is currently hardcoded.
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
		Route currentRoute=mContainer.getRoute();
		Segment currentSegment = currentRoute.getSegment(mRouteSegmentIndex);
		Waypoint nextWaypoint = currentSegment.getWaypoint(mSegmentWaypointIndex);
		Geofence newFence = createGeofence(nextWaypoint, "nextWaypoint", 20,
				Geofence.NEVER_EXPIRE, Geofence.GEOFENCE_TRANSITION_ENTER);
		addGeofence(newFence);
		
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
					//If route has ended we set segment and waypoint indexes to -1 and send that to clients. 
					mRouteSegmentIndex=-1;
					mSegmentWaypointIndex=-1;
					//inform clients that the next waypoint has changed. 
					sendNextWaypointMessage(mRouteSegmentIndex, mSegmentWaypointIndex);
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
			//inform clients that the next waypoint has changed. 
			sendNextWaypointMessage(mRouteSegmentIndex, mSegmentWaypointIndex);
			
		}
		
	}

	
}


