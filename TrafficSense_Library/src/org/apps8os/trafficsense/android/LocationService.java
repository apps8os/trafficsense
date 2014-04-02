package org.apps8os.trafficsense.android;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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

import android.app.AlarmManager;
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


public class LocationService extends Service implements	
	ConnectionCallbacks,OnConnectionFailedListener,OnAddGeofencesResultListener{
	
	private static final float GEOFENCE_RADIUS = 30;
	private static final String ACTION_NEXT_GEOFENCE_REACHED = "trafficesense.nextGeofenceAlarm";
	
	private TrafficsenseContainer mContainer; 
	private LocationClient mLocationClient;
	//indicates if we are using time rather than location to follow route
	private boolean timeOnlyServiceStarted = false;
	private AlarmManager mAlarmManager;
	private long mOffset = 0;
	private Context mContext=this;
	private int mSegmentIndex=0;
	private int mWaypointIndex=0;
	private NextWaypointLocationAlertReceiver mNextWaypointLocationAlert = new NextWaypointLocationAlertReceiver();
	private NextWaypointTimerReceiver mNextWaypointTimerAlert = new NextWaypointTimerReceiver();

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public void onCreate(){
		super.onCreate();
		System.out.println("DBG: service created");
		mContainer=TrafficsenseContainer.getInstance();
		mAlarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

	}
	
	public int onStartCommand(Intent intent, int flags, int startId) {
		System.out.println("DBG: service started");
		mContainer.serviceAttach(getApplicationContext());
		registerReceiver(mNextWaypointLocationAlert, new IntentFilter(ACTION_NEXT_GEOFENCE_REACHED));
		registerReceiver(mNextWaypointTimerAlert, new IntentFilter(Constants.ACTION_NEXT_WAYPOINT));
		startLocationClient();
		return START_NOT_STICKY;
	}
	
	public void onDestroy(){
		super.onDestroy();
		unregisterReceiver(mNextWaypointLocationAlert);
		unregisterReceiver(mNextWaypointTimerAlert);
		//need to detach from container
		mContainer.serviceDetach();
	}
	
	/**
	 * Starts teh location client
	 */
	public void startLocationClient(){
		mLocationClient = new LocationClient(this, this, this);
		mLocationClient.connect();
	}
	
	/**
	 * called when the location client is connected. Set all the geofences for the route.
	 */
	public void onConnected(Bundle connectionHint) {
		if(timeOnlyServiceStarted == true){
			stopTimeOnlyService();
		}
		System.out.println("DBG: Location manager connected");
		setGeofencesForRoute();
		sendNextWaypointIntent(null);
		// TODO: check
		mContainer.getPebbleUiController().update();
	}
	
	/**
	 * Sets the geofences for all the waypoints on the route.
	 * Some geofences will be set at latlong(0,0) because walking segments dont have coords, 
	 * but this doesnt matter. Also some geofences will go on top of each other but this also doesnt
	 * matter. 
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
			for(int waypointIndex=0;;waypointIndex++){
				
				Waypoint nextWaypoint = currentSegment.getWaypoint(waypointIndex);
				if(nextWaypoint==null){
					break;
				}
				System.out.println("DBG making geofence for " + segmentIndex +"," + waypointIndex);
				String id = Integer.toString(segmentIndex)+","+Integer.toString(waypointIndex);
				Geofence nextBusStopGeofence = createGeofence(nextWaypoint, id, GEOFENCE_RADIUS,
						Geofence.NEVER_EXPIRE, Geofence.GEOFENCE_TRANSITION_ENTER);
				listOfFences.add(nextBusStopGeofence);	
			}
		}
		addGeofence(listOfFences);		
	}
	/**
	 * Create a geofence from the given parameters
	 * @param busStop
	 * @param id
	 * @param radius
	 * @param expiryDuration
	 * @param transition
	 * @return
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
	 * adds a geofence to the location client. The action taken when 
	 * geofence transition is made is currently hardcoded.
	 * @param newFence
	 */
	private void addGeofence(ArrayList<Geofence> list){
		Intent i = new Intent();
		i.setAction(ACTION_NEXT_GEOFENCE_REACHED);
		PendingIntent pi = PendingIntent.getBroadcast(this, 1, i, PendingIntent.FLAG_CANCEL_CURRENT);
		//if the location client is connected send the request
		if(mLocationClient.isConnected()){
			System.out.println("DBG adding geofence list");
			mLocationClient.addGeofences(list, pi, this);
		}
	}
	
	/**
	 * sends and intent indicating that the waypoint has changed. 
	 * @param message
	 */
	protected void sendNextWaypointIntent(String message){
		Intent vi = new Intent();
		if(message == null){
			message ="";
		}
		
		Intent coordsReadyIntent = new Intent().setAction(Constants.ACTION_COORDS_READY);
		this.sendBroadcast(coordsReadyIntent);
		
		vi.putExtra(Constants.ACTION_ROUTE_EVENT_EXTRA_MESSAGE, message);
		vi.setAction(Constants.ACTION_ROUTE_EVENT);
		sendBroadcast(vi);
	}
	

	

	@Override
	/**
	 * called when geofences are added
	 */
	public void onAddGeofencesResult(int statusCode, String[] geofenceRequestIds) {
		//if status code != 0 and error has occured and the geofences cant be used.
		//TODO: add granularity to the errors. Some could possibly be handled. 
		if(statusCode != 0){
			System.out.println("DBG locOnlyServ: status code for addGeofences is "+ Integer.toString(statusCode)
								+". Starting time only service");
			startTimeOnlyService();
		}
		else{
			System.out.println("DBG locOnlyServ: Geofences successfully added.");
		}
		
	}

	@Override
	/**
	 * Called when locationClient failed to connect
	 */
	public void onConnectionFailed(ConnectionResult result) {
		System.out.println("DBG locOnlyServ: LocationClient failed to connect. Starting time only service");
		startTimeOnlyService();
	}

	@Override
	/**
	 * Called when locationClient disconnected
	 */
	public void onDisconnected() {
		System.out.println("DBG locOnlyServ: LocationClient disconnected. Starting time only service");
		startTimeOnlyService();
	}
	
	public void startTimeOnlyService(){
		System.out.println("DBG locOnlyServ: TimeService started");
		if(timeOnlyServiceStarted == true){
			return;
		}
		timeOnlyServiceStarted = true;
		if (mContainer.getRoute().isJourneyEnded() == false) {
			// TODO: check
			mContainer.getPebbleUiController().update();
			long timeToNextWaypoint = timeStringToDate(mContainer.getRoute().getDate() + " "+ mContainer.getRoute()
									.getCurrentSegment().getCurrentWaypoint().getWaypointTime()).getTime();
			
			scheduleNextAlarm(timeToNextWaypoint+mOffset);
		}
	}
	
	public void stopTimeOnlyService(){
		System.out.println("DBG locOnlyServ: TimeService stoped");
		timeOnlyServiceStarted = false;
	}
	
	
	public Date timeStringToDate(String timeStr) {
		Date date = null;
		try {
			// TODO: Locale problem
			date = new SimpleDateFormat("EEEE dd.M.yyyy kk:mm", Locale.ENGLISH)
			.parse(timeStr);
		} catch (ParseException e) {
			System.out.println("DBG TimeOnlyService timeStringToDate error: " + e.getMessage());
		}
		return date;
	}

	/**
	 * Schedule a next stop alarm.
	 * 
	 * @param atMillis time in milliseconds that the alarm should go off.
	 */
	private void scheduleNextAlarm(long atMillis) {
		Intent intentWaypoint = new Intent();
		intentWaypoint.setAction(Constants.ACTION_NEXT_WAYPOINT);
		PendingIntent mNextWaypointIntent = PendingIntent.getBroadcast(this, 0,intentWaypoint, PendingIntent.FLAG_CANCEL_CURRENT);
		if (Constants.useWallClock == true) {
			mAlarmManager.set(AlarmManager.RTC_WAKEUP, atMillis, mNextWaypointIntent);
		} else {
			mAlarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()
					+ Constants.TEST_TIME, mNextWaypointIntent);
		}
	}
	
	
	public void updateLocation(int segmentIndex, int waypointIndex){

		if(mContainer.getRoute().isJourneyEnded()==true){
			return;
		}
		mWaypointIndex= waypointIndex++;
		mSegmentIndex = segmentIndex;
		Segment currentSegment = mContainer.getRoute().setNextSegment(mSegmentIndex);
		Waypoint nextWaypoint = currentSegment.setNextWaypoint(mWaypointIndex);

		//segment had ended
		if(nextWaypoint == null){
			mOffset=0;
			mSegmentIndex++;
			currentSegment = mContainer.getRoute().setNextSegment(mSegmentIndex);
			
			//journey has ended
			if(currentSegment==null){
				mSegmentIndex=-1;
				mWaypointIndex=-1;
				mContainer.getRoute().setJourneyEnded(true);
				return; //the route has ended
			}
			
			mWaypointIndex=0;
			nextWaypoint = currentSegment.setNextWaypoint(0);
		}
		else{
			long currentTime = System.currentTimeMillis();
			Waypoint cur = currentSegment.getWaypoint(mWaypointIndex-1);
			long expectedTime = timeStringToDate(mContainer.getRoute().getDate() + " "+ cur.getWaypointTime()).getTime();
			mOffset = currentTime-expectedTime;
			System.out.println("DBG LocServ: mOffset is " + Long.toString(mOffset));
		}
	}
	
	class NextWaypointLocationAlertReceiver extends BroadcastReceiver{

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
			int segmentIndex=Integer.parseInt(parts[0]);
			int waypointIndex=Integer.parseInt(parts[1]);
			
			updateLocation(segmentIndex, waypointIndex);
			makeNotificationAndAlert();
			sendNextWaypointIntent("");
			
			if(mContainer.getRoute().isJourneyEnded()==true){
				System.out.println("DBG LocServ: Journey has ended");
				((LocationService)mContext).stopSelf();
				return;
			}
			System.out.println("DBG LocServ: Next position is " + mContainer.getRoute().getCurrentSegment().getCurrentWaypoint().getWaypointName());
			
		}	
	}
	
	class NextWaypointTimerReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context arg0, Intent arg1) {
			
			//if the time service has been stored off 
			if(timeOnlyServiceStarted==false){
				return;
			}
			System.out.println("DBG LocServ: Trying to restart location client");
			startLocationClient();
			updateLocation(mSegmentIndex,mWaypointIndex);

			/**
			 * Set the alarm for the next waypoint.
			 */
			if (mContainer.getRoute().isJourneyEnded() == false) {
				// TODO: check
				mContainer.getPebbleUiController().update();
				long timeToNextWaypoint = timeStringToDate(mContainer.getRoute().getDate() + " "+ mContainer.getRoute()
										.getCurrentSegment().getCurrentWaypoint().getWaypointTime()).getTime();
				
				scheduleNextAlarm(timeToNextWaypoint+mOffset);
				System.out.println("DBG LocServ: Next position is " + mContainer.getRoute().getCurrentSegment().getCurrentWaypoint().getWaypointName());
			}

			/**
			 * Notify the UI.
			 */
			sendNextWaypointIntent(null);		
			makeNotificationAndAlert();
			/**
			 * Stop the service when the journey ends.
			 */
			if (mContainer.getRoute().isJourneyEnded() == true) {
				((LocationService)mContext).stopSelf();
			}
		}

	}
	
	protected void makeNotificationAndAlert(){
		
		//display last notification
		String msg = OutputLogic.getOutput();             
		int resID = getResources().getIdentifier("bus" , "drawable", getPackageName());
		
		Notification noti = new Notification.Builder(mContext)
				.setContentTitle("Trafficsense Route Tracking")
				.setContentText(msg)
				.setSmallIcon(resID)
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
	
}
