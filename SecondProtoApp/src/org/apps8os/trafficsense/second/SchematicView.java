package org.apps8os.trafficsense.second;


import java.util.ArrayList;

import org.apps8os.trafficsense.TrafficsenseContainer;
import org.apps8os.trafficsense.android.Constants;
import org.apps8os.trafficsense.core.Route;
import android.app.Activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import android.widget.ExpandableListView;
import android.widget.TextView;


	

/**
 * Class for Schematic View.
 */
public class SchematicView extends Activity {
	
	//private EditText mEditText;
	//private Button mButton;
	/**
	 * The top banner.
	 */

	private TextView mTextView;
	//private String mKey=null;
	//private String mRequest;
	private TrafficsenseContainer mContainer;
	/**
	 * The journey we are tracking.
	 */
	private Route mRoute;

	//private CoordsReadyReceiver mCoordsReadyReceiver;*/
	WaypointChangedReceiver mWaypointChangedReceiver;
	ExpandableListAdapter mAdapter;
	

	
	//private SparseArray<Group> mGroups = new SparseArray<Group>();


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.schematic_main);
		mContainer = TrafficsenseContainer.getInstance();
		mRoute = mContainer.getRoute();

		ExpandableListView listView = (ExpandableListView) findViewById(R.id.listView);
		mAdapter = new ExpandableListAdapter(this, mRoute);
		listView.setAdapter(mAdapter);

		mTextView = (TextView) findViewById(R.id.checkedTextView);
		mTextView.setText("\t From: " + mRoute.getStart() + "\n\t To: "
				+ mRoute.getDestination() + "\n");

		mWaypointChangedReceiver = new WaypointChangedReceiver(mAdapter);
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
	@Override
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
	 * Change UI current waypoint has changed.
	 */
	private class WaypointChangedReceiver extends BroadcastReceiver {
		
		//ExpandableListAdapter mAdapter;

		public WaypointChangedReceiver (ExpandableListAdapter adapter) {
			super();
			//mAdapter = adapter;
		}

		@Override
		public void onReceive(Context context, Intent intent) {
			System.out.println("DBG SchematicView.WaypointChangedReceiver.onReceive");
			if (intent.hasExtra(Constants.ROUTE_STOPPED)) {
				//TODO: Restore or clean UI if user stopped the journey.
				return;
			}
			if (intent.hasExtra(Constants.ERROR)) {
				//TODO: handle error
				return;
			}


			changeColor();
			//TODO: Highlight or colour current waypoint/segment.
			//changeColor();

			//TODO: call method that highlights current waypoint
		} 

		
		private void changeColor() {
			
			TrafficsenseContainer container = TrafficsenseContainer.getInstance();
			System.out.println("Trying to update!");
			ExpandableListView listView = (ExpandableListView) findViewById(R.id.listView);
			listView.setAdapter(mAdapter);		}
			/*
		if(mAdapter != null){
			System.out.println("it should update!");
			
			else{
				System.out.println("Why is this null?");
			}
			int seg = container.getRoute().getCurrentIndex();
			int way = container.getRoute().getCurrentSegment().getCurrentIndex();
			try{
			ArrayList <View> segView = mAdapter.getSegmentViewList();
			if (segView !=null){
				System.out.println("Change color of seg");
				segView.get(seg-1).setBackgroundColor(Color.CYAN);
			}}
			catch(NullPointerException npe){
				System.out.println("NPE on segView: ");
			}
			
			try{
			ArrayList<ArrayList<View>> wayView = mAdapter.getWaypointViewList();
			if(wayView != null && wayView.size() !=0){
				System.out.println("Change color of way");
				wayView.get(seg-1).get(way-1).setBackgroundColor(Color.RED);
			}}
			catch (NullPointerException npe){
				System.out.println("NPE on wayView:");
			}
			
			*/
		
		
	}
}

