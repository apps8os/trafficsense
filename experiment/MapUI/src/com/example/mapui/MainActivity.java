package com.example.mapui;

import java.util.ArrayList;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.app.ActionBar;

public class MainActivity extends Activity {

	GoogleMap map;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		 map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
		 ListView listview = (ListView) findViewById(R.id.listview);
		 
		 listview.setVisibility(View.INVISIBLE);
		 
	}
	


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.main_activity_action, menu);
	    return super.onCreateOptionsMenu(menu);
	}

	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle presses on the action bar items
	    switch (item.getItemId()) {
	        case R.id.menu_select_route:
	            selectRoute();
	            showList();
	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	
	private void selectRoute(){
		Marker test = map.addMarker(new MarkerOptions().position(new LatLng(60.1708, 24.9375)).title("Destination: Testing how long text works"));
		
	}
	
	private void showList(){

		 String[] values = new String[] {"Walk to waypoint", "Take bus to waypoint" };
		 ListView listview = (ListView) findViewById(R.id.listview);
		 ArrayList<String> list = new ArrayList<String>();
		 for (int i = 0; i < values.length; ++i) {
		      list.add(values[i]);
		 }
		 final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, list);
		 listview.setAdapter(adapter);
		 listview.setVisibility(View.VISIBLE);
	}
	
	
}
