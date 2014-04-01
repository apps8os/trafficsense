package org.apps8os.trafficsense.second;

import org.apps8os.trafficsense.TrafficsenseContainer;
import org.apps8os.trafficsense.android.Constants;
import org.apps8os.trafficsense.core.OutputLogic;
import org.apps8os.trafficsense.core.Route;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
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
	TrafficsenseContainer mContainer;
	Route route = new Route();
	
	/*CoordsReadyReceiver mCoordsReadyReceiver;
	WaypointChanged mWaypointChangedReceiver;*/
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
       myTextView = (TextView) findViewById(R.layout.schematic_main);
	   
	    myTextView.setText("From: " + route.getStart() + ", to " + route.getDestination());
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
	
	
	/*
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
		 }

	}
	
	@Override
	public void onPause() {
		unregisterReceiver(mCoordsReadyReceiver);
		unregisterReceiver(mWaypointChangedReceiver);
		mContainer.activityDetach();
		super.onPause();
	}
	
	
	

}


class CoordsReadyReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context arg0, Intent intent) {
      
		
	}
}

/**
 * Class that receives an intent when current waypoint has changed. 
 * @author traffisense
 *
 
class WaypointChanged extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		
	}*/
	

}