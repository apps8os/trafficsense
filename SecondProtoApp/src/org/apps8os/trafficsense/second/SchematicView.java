package org.apps8os.trafficsense.second;

import java.util.ArrayList;

import org.apps8os.trafficsense.TrafficsenseContainer;
import org.apps8os.trafficsense.android.Constants;
import org.apps8os.trafficsense.core.OutputLogic;
import org.apps8os.trafficsense.core.Route;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import android.widget.ExpandableListView;
import android.widget.TextView;

public class SchematicView extends Activity{
	
	EditText myEditText;
	Button myButton;
	TextView myTextView;
	String key=null;
	String request;
	public TrafficsenseContainer mContainer;
	Route route = new Route();

	
	/*CoordsReadyReceiver mCoordsReadyReceiver;*/
	WaypointChanged mWaypointChangedReceiver;
	// SparseArray<Group> groups = new SparseArray<Group>();
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.schematic_main);
		mContainer = TrafficsenseContainer.getInstance();
		route = mContainer.getRoute();
		
		ExpandableListView listView = (ExpandableListView) findViewById(R.id.listView);
		ExpandableListAdapter adapter = new ExpandableListAdapter(this, route);
		listView.setAdapter(adapter);
		myTextView = (TextView) findViewById(R.id.checkedTextView);
		myTextView.setText("From: " + route.getStart() + ", to " + route.getDestination());
		mWaypointChangedReceiver = new WaypointChanged(adapter);
   }

    		 
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		MenuItem map = menu.findItem(R.id.menu_map_view);
		return true;
	}
	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item){
	    // Handle presses on the action bar items
	    switch (item.getItemId()) {
	        case R.id.menu_map_view:
	        	Intent myIntent = new Intent(SchematicView.this, MainActivity.class);
	        	this.startActivity(myIntent);
	        	return true;
	        default:
	            return super.onOptionsItemSelected(item); 
	    }
	}
	
	
	
	@Override
	public void onResume() {
		super.onResume();
		mContainer.activityAttach(getApplicationContext());
		registerReceiver(mWaypointChangedReceiver, new IntentFilter(Constants.ACTION_ROUTE_EVENT));
		Intent i = new Intent().setAction(Constants.ACTION_ROUTE_EVENT);
		sendBroadcast(i);
		}

	
	
	@Override
	public void onPause() {
		unregisterReceiver(mWaypointChangedReceiver);
		mContainer.activityDetach();
		super.onPause();
	}

}

/**
 * Class that receives an intent when current waypoint has changed. 
 * @author traffisense
 */
 
class WaypointChanged extends BroadcastReceiver {

	ExpandableListAdapter adapter;
	
	public WaypointChanged (ExpandableListAdapter adapter){
		super();
		this.adapter = adapter;
	}
	
	@Override
	public void onReceive(Context context, Intent intent) {
		if(intent.hasExtra(Constants.ROUTE_STOPPED)){
			//TODO: handle route stoped
			return;
		}
		if(intent.hasExtra(Constants.ERROR)){
			//TODO: handle error
			return;
		}
		
		System.out.println("DBG Schematic View. Waypoint changed");
		//changeColor();
		//TODO: call method that highlights current waypoint
	} 

	private void changeColor() {
		/*TrafficsenseContainer mContainer = TrafficsenseContainer.getInstance();
		int seg = mContainer.getRoute().getCurrentIndex();
		int way = mContainer.getRoute().getCurrentSegment().getCurrentIndex();
		ArrayList <View> segView = adapter.getSegmentViewList();
		ArrayList <View> wayView = adapter.getWaypointViewList();
		segView.get(seg-1).setBackgroundColor(Color.CYAN);
		wayView.get(way-1).setBackgroundColor(Color.CYAN);*/
	}
	
	

}