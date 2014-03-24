package org.apps8os.trafficsense.second;

import org.apps8os.trafficsense.TrafficsenseContainer;
import org.apps8os.trafficsense.core.Route;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
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
	// SparseArray<Group> groups = new SparseArray<Group>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		route = mContainer.getRoute();
		
       ExpandableListView listView = (ExpandableListView) findViewById(R.id.listview);
       ExpandableListAdapter adapter = new ExpandableListAdapter(this, route);
       listView.setAdapter(adapter);
       myTextView = (TextView) findViewById(R.id.textView1);
	    //myTextView.setText("From: ");
	   
	    myTextView.setText("From: " + route.getStart() + ", to " + route.getDestination());
    	}

    		 
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
