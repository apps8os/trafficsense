package org.apps8os.trafficsense.second;


import java.util.ArrayList;
import java.util.List;

import org.apps8os.trafficsense.TrafficsenseContainer;
import org.apps8os.trafficsense.android.Constants;
import org.apps8os.trafficsense.core.Segment;
import org.apps8os.trafficsense.core.Waypoint;
import org.apps8os.trafficsense.util.EmailCredential;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import android.os.Bundle;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class MainActivity extends Activity {

	TrafficsenseContainer mContainer;
	GoogleMap map;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mContainer = TrafficsenseContainer.getInstance();
		 map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
		 ListView listview = (ListView) findViewById(R.id.listview);
		 
		 listview.setVisibility(View.INVISIBLE); 
		 map.setMyLocationEnabled(true);
		 
	}
	
	@Override
	public void onResume() {
		super.onResume();
		mContainer.activityAttach(getApplicationContext());
		
		//TODO: update the ui with the latest info
		
	}
	
	@Override
	public void onPause() {
		super.onPause();
		mContainer.activityDetach();
	}
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.main_activity_action, menu);
	    return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item){
	    // Handle presses on the action bar items
	    switch (item.getItemId()) {
	        case R.id.menu_start_journey:
	            startJourney();
	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	
	private void startJourney(){
        EmailCredential cred = new EmailCredential("trafficsense.aalto@gmail.com", "ag47)h(58P");
		mContainer.startJourneyTracker(Constants.SERVICE_LOCATION_ONLY, cred);
		
	}
	
	private void showList(String[] messages){

		 ListView listview = (ListView) findViewById(R.id.listview);
		 ArrayList<String> list = new ArrayList<String>();
		 for (int i = 0; i < messages.length; ++i) {
		      list.add(messages[i]);
		 }
		 final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, list);
		 listview.setAdapter(adapter);
		 listview.setVisibility(View.VISIBLE);
	}
	
	class WaypointChanged extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			Segment curSegment = mContainer.getRoute().getCurrentSegment();
			int curSegmentIndex = mContainer.getRoute().getCurrentIndex();
			int curWaypointIndex = mContainer.getRoute().getCurrentSegment().getCurrentIndex();
			List<Waypoint> waypointList = mContainer.getRoute().getCurrentSegment().getWaypointList();
			
			if(curSegmentIndex ==-1 & curSegmentIndex==-1){
				String message[] = {"Congratulations. You reached your destination"};
				showList(message);
			}
			
			//if the next stop is the last stop on a segment
			if(curWaypointIndex == waypointList.size()-1){
				String message[] = {"Get off at next stop"};
				showList(message);
			}
			
			//if the current segment is a walking one
			/**if(curSegment.isWalking() == false){
				String message[] = {"Walk to next stop"};
				showList(message);
			}
			**/
			
			//if the next stop
			if(curWaypointIndex == 1){
				//if(curWaypointIndex == 1 && curSegment.isWalking() == false){   //WHY IS ISWALKING() NOT WORKING ?????
				String transportId = curSegment.getSegmentMode();
				String destination = curSegment.getLastWaypoint().getWaypointName();
				String message[] = new String[1];
				if(transportId.equals("metro")){
					message[0] = "Take metro to "+ destination;
				}
				else if(transportId.length() == 1){
					message[0] = "Take " + transportId+ " train to "+ destination;
				}
				else{
					message[0]= "Take bus " + transportId + " to " + destination;
				}
				showList(message);
						
			}
		
		}
	}
	
}
