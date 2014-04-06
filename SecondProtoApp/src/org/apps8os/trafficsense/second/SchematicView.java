package org.apps8os.trafficsense.second;


import org.apps8os.trafficsense.TrafficsenseContainer;
import org.apps8os.trafficsense.android.Constants;
import org.apps8os.trafficsense.core.Route;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import android.widget.ExpandableListView;
import android.widget.TextView;

public class SchematicView extends Activity{
	
	//private EditText mEditText;
	//private Button mButton;
	private TextView mTextView;
	//private String mKey=null;
	//private String mRequest;
	private TrafficsenseContainer mContainer;
	private Route mRoute;
	//private CoordsReadyReceiver mCoordsReadyReceiver;*/
	WaypointChanged mWaypointChangedReceiver;
	//private SparseArray<Group> mGroups = new SparseArray<Group>();
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.schematic_main);
		mContainer = TrafficsenseContainer.getInstance();
		mRoute = mContainer.getRoute();

		ExpandableListView listView = (ExpandableListView) findViewById(R.id.listView);
		ExpandableListAdapter adapter = new ExpandableListAdapter(this, mRoute);
		listView.setAdapter(adapter);
		mTextView = (TextView) findViewById(R.id.checkedTextView);
		mTextView.setText("\t From: " + mRoute.getStart() + "\n\t To: "
				+ mRoute.getDestination() + "\n");
		mWaypointChangedReceiver = new WaypointChanged(adapter);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.schematic_activity_action, menu);
		return super.onCreateOptionsMenu(menu);
	}

	/**
	 * Makes the options menu. 
	 */
	public boolean onPrepareOptionsMenu(Menu menu){
		super.onPrepareOptionsMenu(menu);
		MenuItem map_view = menu.findItem(R.id.menu_map_view);
		map_view.setVisible(true);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item){
	    // Handle taps on the action bar items
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

	/**
	 * Handle Intents generated when current waypoint has changed. 
	 */
	private class WaypointChanged extends BroadcastReceiver {
		
		//ExpandableListAdapter mAdapter;

		public WaypointChanged (ExpandableListAdapter adapter) {
			super();
			//mAdapter = adapter;
		}

		@Override
		public void onReceive(Context context, Intent intent) {
			System.out.println("DBG Schematic View. Waypoint changed");
			if (intent.hasExtra(Constants.ROUTE_STOPPED)) {
				//TODO: handle route stoped
				return;
			}
			if (intent.hasExtra(Constants.ERROR)) {
				//TODO: handle error
				return;
			}

			//changeColor();
			//TODO: call method that highlights current waypoint
		} 

		/*
		private void changeColor() {
			TrafficsenseContainer container = TrafficsenseContainer.getInstance();
			int seg = container.getRoute().getCurrentIndex();
			int way = container.getRoute().getCurrentSegment().getCurrentIndex();
			ArrayList <View> segView = mAdapter.getSegmentViewList();
			ArrayList <View> wayView = mAdapter.getWaypointViewList();
			segView.get(seg-1).setBackgroundColor(Color.CYAN);
			wayView.get(way-1).setBackgroundColor(Color.CYAN);
		}
		*/
	}

}

