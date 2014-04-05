package org.apps8os.trafficsense.second;

import java.util.ArrayList;

import org.apps8os.trafficsense.TrafficsenseContainer;
import org.apps8os.trafficsense.android.Constants;
import org.apps8os.trafficsense.core.OutputLogic;
import org.apps8os.trafficsense.core.Route;
import org.apps8os.trafficsense.core.Segment;
import org.apps8os.trafficsense.core.Waypoint;
import org.apps8os.trafficsense.util.EmailCredential;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import android.os.Bundle;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;


public class MainActivity extends Activity {

	private CoordsReadyReceiver mCoordsReadyReceiver;
	private WaypointChanged mWaypointChangedReceiver;

	private TrafficsenseContainer mContainer;
	private GoogleMap map;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mContainer = TrafficsenseContainer.getInstance();
		map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map))
				.getMap();
		ListView listview = (ListView) findViewById(R.id.listview);

		listview.setVisibility(View.INVISIBLE);
		map.setMyLocationEnabled(true);

		mCoordsReadyReceiver = new CoordsReadyReceiver();
		mWaypointChangedReceiver = new WaypointChanged();
		String welcome[] = { "Welcome" };
		showList(welcome);

	}

	@Override
	public void onResume() {
		super.onResume();
		invalidateOptionsMenu();
		mContainer.activityAttach(getApplicationContext());
		registerReceiver(mCoordsReadyReceiver, new IntentFilter(
				Constants.ACTION_COORDS_READY));
		registerReceiver(mWaypointChangedReceiver, new IntentFilter(
				Constants.ACTION_ROUTE_EVENT));

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
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {

		super.onPrepareOptionsMenu(menu);
		MenuItem start = menu.findItem(R.id.menu_start_journey);
		MenuItem stop = menu.findItem(R.id.menu_stop_journey);
		MenuItem schematic = menu.findItem(R.id.menu_schematic_view);
		if (mContainer.isJourneyStarted() == true) {
			start.setVisible(false);
			stop.setVisible(true);
			schematic.setVisible(true);
		} else if (mContainer.isLoading()) {
			start.setVisible(true);
			start.setActionView(R.layout.progressbar);
			stop.setVisible(false);
			schematic.setVisible(false);
		} else {
			start.setVisible(true);
			stop.setVisible(false);
			schematic.setVisible(false);
		}
		return (true);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
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
	private void startJourney() {
		// TODO: check for network connectivity
		EmailCredential cred = new EmailCredential(
				"trafficsense.aalto@gmail.com", "ag47)h(58P", "imap.gmail.com");
		mContainer.startJourneyTracker(Constants.SERVICE_LOCATION_ONLY, cred);

	}

	/**
	 * Stops the journey.
	 */
	private void stopJourney() {
		mContainer.stopJourney();
		map.clear();
		String msg[] = { "Welcome" };
		showList(msg);
	}

	/**
	 * TODO: documentation.
	 * @param messages
	 */
	private void showList(String[] messages) {

		ListView listview = (ListView) findViewById(R.id.listview);
		ArrayList<String> list = new ArrayList<String>();
		for (int i = 0; i < messages.length; ++i) {
			list.add(messages[i]);
		}
		final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, list);
		listview.setAdapter(adapter);
		listview.setVisibility(View.VISIBLE);
	}

	/**
	 * draws the route on the map using lines that dont follow roads. Also zooms
	 * to first waypoint with location.
	 */
	public void drawRoute() {
		map.clear();
		Route r = mContainer.getRoute();

		boolean zoomed = false;
		// Get the image that will be used as the bus stop icon
		int resID = getResources().getIdentifier("bus_stop_marker", "drawable",
				getPackageName());
		Bitmap icon = resizeIcon(resID);
		PolylineOptions o = new PolylineOptions().geodesic(true);
		for (Segment s : r.getSegmentList()) {

			/**
			 * Do not draw walking segments because they have no GPS coordinates.
			 */
			if (s.isWalking()) {
				continue;
			}
			
			int indexInSegment = 1;
			
			for (Waypoint w : s.getWaypointList()) {
				
				/**
				 * Ignore waypoints without valid GPS coordinates.
				 */
				if (w.hasCoord() == false) {
					continue;
				}

				//System.out.println("DBG waypoint: (" + w.getLatitude() + "," + w.getLongitude() + ")");
				
				LatLng coord = new LatLng(w.getLatitude(), w.getLongitude());

				/**
				 * Make a green marker for starting point and a red marker for end point.
				 * 
				 * TODO: What if they have no GPS coordinates?
				 * This happens when their stopCode is empty. 
				 */
				if (s.getWaypoint(0).getWaypointName()
						.equals(w.getWaypointName())) {
					// This waypoint is the start of the segment
					map.addMarker(new MarkerOptions()
							.position(coord)
							.title("Departure on " + s.getSegmentMode() + ": "
									+ indexInSegment + "." + w.getWaypointName() + "("
									+ w.getWaypointStopCode() + ")")
							.icon(BitmapDescriptorFactory
									.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
							);
				} else if (s.getLastWaypoint().getWaypointName()
						.equals(w.getWaypointName())) {
					// This waypoint is the end of this segment.
					map.addMarker(new MarkerOptions()
							.position(coord)
							.title("Arrival on " + s.getSegmentMode() + ": "
									+ indexInSegment + "." + w.getWaypointName() + "("
									+ w.getWaypointStopCode() + ")")
							.icon(BitmapDescriptorFactory
									.defaultMarker(BitmapDescriptorFactory.HUE_RED))
							);
				} else {
					// Intermediate points.
					map.addMarker(new MarkerOptions()
							.position(coord)
							.title(indexInSegment + "." + w.getWaypointName() + " ("
									+ w.getWaypointStopCode() + ")")
							.icon(BitmapDescriptorFactory.fromBitmap(icon)));
				}
				
				o.add(coord);
				indexInSegment++;

				if (zoomed == false) {
					centerLocationOnMap(coord);
					zoomed = true;
				}
			}
		}

		// Plot the polyline
		map.addPolyline(o);

	}

	/**
	 * Zooms the map to the current waypoint.
	 */
	public void zoomToCurrentWaypoint() {
		ArrayList<Segment> segments = mContainer.getRoute().getSegmentList();
		int index = mContainer.getRoute().getCurrentIndex();
		Waypoint curWay;
		/**
		 * If the current segment is a walking one then the location of waypoints
		 * will be (0,0) so we need to get the next index which is always not walking.
		 */
		if (segments.get(index).isWalking()) {
			curWay = segments.get(index + 1).getWaypoint(0);
		} else {
			curWay = segments.get(index).getCurrentWaypoint();
		}
		LatLng loc = new LatLng(curWay.getLatitude(), curWay.getLongitude());
		map.animateCamera(CameraUpdateFactory.newLatLng(loc));
	}

	/**
	 * Resize the icon used in the map for bus stops.
	 * 
	 * @param resID
	 * @return
	 */
	public Bitmap resizeIcon(int resID) {
		Bitmap origIcon = BitmapFactory.decodeResource(getResources(), resID);
		Bitmap newIcon = Bitmap.createScaledBitmap(origIcon, 25, 25, false);
		return newIcon;
	}

	/**
	 * Centers the map at a location.
	 */
	public void centerLocationOnMap(LatLng location) {
		map.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 14));
	}

	/**
	 * Class that receives an intent when all the coordinates have been loaded.
	 */
	private class CoordsReadyReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context arg0, Intent intent) {
			invalidateOptionsMenu();
			drawRoute();

		}
	}

	/**
	 * Class that receives an intent when current waypoint has changed.
	 */
	private class WaypointChanged extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.hasExtra(Constants.ROUTE_STOPPED)) {
				invalidateOptionsMenu();
				return;
			}
			if (intent.hasExtra(Constants.ERROR)) {
				String msg[] = { intent.getStringExtra(Constants.ERROR) };
				showList(msg);
				return;
			}
			System.out.println("DBG: MainActivity: Waypoint changed");
			String msg[] = { OutputLogic.getJourneyProgressMessage() };
			showList(msg);
		}

	}
}