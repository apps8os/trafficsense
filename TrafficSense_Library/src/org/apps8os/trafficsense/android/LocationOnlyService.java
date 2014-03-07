package org.apps8os.trafficsense.android;

import java.util.ArrayList;

import org.apps8os.trafficsense.TrafficsenseContainer;
import org.apps8os.trafficsense.core.Route;
import org.apps8os.trafficsense.core.Segment;
import org.apps8os.trafficsense.core.Waypoint;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationClient.OnAddGeofencesResultListener;

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
import android.os.Vibrator;


public class LocationOnlyService extends Service implements 
		ConnectionCallbacks,
		OnConnectionFailedListener,
		OnAddGeofencesResultListener{

	private int GEOFENCE_RADIUS = 20;
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
		
		setGeofencesForRoute();
		
		Route currentRoute=mContainer.getRoute();
		Segment currentSegment = currentRoute.getSegment(mRouteSegmentIndex);
		Waypoint nextWaypoint = currentSegment.getWaypoint(mSegmentWaypointIndex);
		Geofence newFence = createGeofence(nextWaypoint, "nextWaypoint", GEOFENCE_RADIUS,
				Geofence.NEVER_EXPIRE, Geofence.GEOFENCE_TRANSITION_ENTER);
		addGeofence(newFence);
		sendNextWaypointIntent(null);
		
	}

	/**
	 * Sets the geofences for all the waypoints on the route
	 */
	public void setGeofencesForRoute(){
		Route currentRoute = mContainer.getRoute();
		for(int segmentIndex=0;;segmentIndex++){
			Segment currentSegment = currentRoute.getSegment(mRouteSegmentIndex);
			if(currentSegment == null){
				break;
			}
			//TODO: check if the segment is a walking segment and if it is only set the last waypoint in it 
			
			//skip the first waypoint because it it also the last one in the last segment
			for(int waypointIndex=1;;waypointIndex++){
				Waypoint nextWaypoint = currentSegment.getWaypoint(mSegmentWaypointIndex);
				if(nextWaypoint==null){
					break;
				}
				String id = (new Integer(segmentIndex)).toString()+","+(new Integer(waypointIndex)).toString();
				mNextBusStopGeofence = createGeofence(nextWaypoint, id, GEOFENCE_RADIUS,
						Geofence.NEVER_EXPIRE, Geofence.GEOFENCE_TRANSITION_ENTER);
				addGeofence(mNextBusStopGeofence);	
				
			}
			
		}
			
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
			//if we are at the last waypoint set indexes to -1
			
			Segment currentSegment = mContainer.getRoute().setNextSegment(mRouteSegmentIndex);
			Waypoint nextWaypoint = currentSegment.setNextWaypoint(mSegmentWaypointIndex);
			
			if(nextWaypoint == null){
				
				currentSegment = mContainer.getRoute().setNextSegment(mRouteSegmentIndex);
				mRouteSegmentIndex++;
				
				if(currentSegment==null){
					mRouteSegmentIndex=-1;
					mSegmentWaypointIndex=-1;
					//inform clients that waypoint has changed
					sendNextWaypointIntent("");
					sendNextWaypointMessage(mRouteSegmentIndex, mSegmentWaypointIndex);

					return; //the route has ended
				}
				mSegmentWaypointIndex=0;
				nextWaypoint = currentSegment.setNextWaypoint(0);
			}

			
			//inform clients that the next waypoint has changed. 
			sendNextWaypointIntent("");
			sendNextWaypointMessage(mRouteSegmentIndex, mSegmentWaypointIndex);
			
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

}


