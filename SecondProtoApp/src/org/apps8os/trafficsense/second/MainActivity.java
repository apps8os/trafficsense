package org.apps8os.trafficsense.second;


import java.util.ArrayList;
import java.util.List;

import org.apps8os.trafficsense.TrafficsenseContainer;
import org.apps8os.trafficsense.android.Constants;
import org.apps8os.trafficsense.core.OutputLogic;
import org.apps8os.trafficsense.core.Route;
import org.apps8os.trafficsense.core.Segment;
import org.apps8os.trafficsense.core.Waypoint;
import org.apps8os.trafficsense.util.EmailCredential;

import com.google.android.gms.location.LocationClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import android.location.LocationManager;
import android.os.Bundle;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class MainActivity extends Activity {
	
	CoordsReadyReceiver mCoordsReadyReceiver;
	WaypointChanged mWaypointChangedReceiver;
	
	TrafficsenseContainer mContainer;
	GoogleMap map;
	Menu mMenu;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mContainer = TrafficsenseContainer.getInstance();
		 map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
		 ListView listview = (ListView) findViewById(R.id.listview);
		  
		 listview.setVisibility(View.INVISIBLE); 
		 map.setMyLocationEnabled(true);
		 
		mCoordsReadyReceiver = new CoordsReadyReceiver();
		mWaypointChangedReceiver = new WaypointChanged();  
		String welcome[] = {"Welcome"};
		showList(welcome);
		 
	}
	
	@Override
	public void onResume() {
		super.onResume();
        invalidateOptionsMenu();
		mContainer.activityAttach(getApplicationContext());
		registerReceiver(mCoordsReadyReceiver, new IntentFilter(Constants.ACTION_COORDS_READY));
		registerReceiver(mWaypointChangedReceiver, new IntentFilter(Constants.ACTION_ROUTE_EVENT));
		
		if (mContainer.getRoute().getCoordsReady()) {
			Intent i = new Intent().setAction(Constants.ACTION_ROUTE_EVENT);
			sendBroadcast(i);
			drawRoute();
		 }

	}
	
	@Override
	public void onPause() {
		unregisterReceiver(mCoordsReadyReceiver);
		unregisterReceiver(mWaypointChangedReceiver);
		mContainer.activityDetach();
		super.onPause();
	}
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.main_activity_action, menu);
	    return super.onCreateOptionsMenu(menu);
	}
	
	/**
	 * Makes the options menu. 
	 */
	public boolean onPrepareOptionsMenu(Menu menu){

		super.onPrepareOptionsMenu(menu);
		MenuItem start = menu.findItem(R.id.menu_start_journey);
		MenuItem stop = menu.findItem(R.id.menu_stop_journey);
		MenuItem schematic = menu.findItem(R.id.menu_schematic_view);
		if(mContainer.isJourneyStarted() == true){
			start.setVisible(false);
			stop.setVisible(true);
			schematic.setVisible(true);
		}
		else{
			start.setVisible(true);
			stop.setVisible(false);
			schematic.setVisible(false);
		}
		return(true);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item){
	    // Handle presses on the action bar items
	    switch (item.getItemId()) {
	        case R.id.menu_start_journey:
	            item.setActionView(R.layout.progressbar); 
	            item.expandActionView();
	        	startJourney();
	            return true;
	        case R.id.menu_stop_journey:
	        	stopJourney();
	            invalidateOptionsMenu();
	        	return true;
	        case R.id.menu_schematic_view:
	        	Intent myIntent = new Intent(MainActivity.this, SchematicView.class);
	        	this.startActivity(myIntent);
	        	return true;
	        default:
	            return super.onOptionsItemSelected(item); 
	    }
	}
	
	/**
	 * Starts the journey.
	 */
	private void startJourney(){
		//TODO: check for network connectivity
        EmailCredential cred = new EmailCredential("trafficsense.aalto@gmail.com", "ag47)h(58P", "imap.gmail.com");
		mContainer.startJourneyTracker(Constants.SERVICE_LOCATION_ONLY, cred);  
		
	}
	
	
	
	/**
	 * Stops the journey.
	 */
	private void stopJourney(){
		mContainer.stopJourney();
		map.clear();
		String msg[] = {"Welcome"};
		showList(msg);
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
	
	/**
	 * draws the route on the map using lines that dont follow roads. Also zooms to first waypoint with location. 
	 */
	public void drawRoute() {  
		map.clear();
		Route r = mContainer.getRoute();
		boolean zoomed = false;
		PolylineOptions o = new PolylineOptions().geodesic(true); 
		for (Segment s : r.getSegmentList()) {
			if (s.isWalking()) { 
				// Don't draw walking segments because they don't have coordinates
				continue;
			}

			for (Waypoint w : s.getWaypointList()) {
				if (w.getLatitude() == 0 && w.getLongitude() == 0) {
					continue;
				}
				LatLng coord = new LatLng(w.getLatitude(), w.getLongitude());

				o.add(coord);

				if(zoomed == false){
					centerLocationOnMap(coord);
					zoomed = true;
				}
			}
		}
		map.addPolyline(o);
	}
	
	
	
	
	/**
	 * resize the icon used in the map for busstops. 
	 * @param resID
	 * @return
	 */
	public Bitmap resizeIcon(int resID){
		Bitmap origIcon = BitmapFactory.decodeResource(getResources(),resID);
		Bitmap newIcon = Bitmap.createScaledBitmap(origIcon, 25, 25, false);
		return newIcon;
	}
	
	/**
	 * centers the map on a location
	 */
	public void centerLocationOnMap(LatLng location){
		map.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 14));
	}
	
	/**
	 * Class that receives an intent when all the coordinates have been loaded
	 * @author traffisense
	 *
	 */
	class CoordsReadyReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context arg0, Intent intent) {
            invalidateOptionsMenu();
			drawRoute();
			
		}
	}
	
	/**
	 * Class that receives an intent when current waypoint has changed. 
	 * @author traffisense
	 *
	 */
	class WaypointChanged extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			System.out.println("DBG: Main activity: Waypoint changed");
			String msg[] = {OutputLogic.getOutput()};
			showList(msg);
		}
		
		
		
	
	}
}