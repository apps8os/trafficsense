package com.org.hslapitest;

import java.io.IOException;
import java.io.InputStream;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import android.os.Bundle;
import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.util.SparseArray;
import android.view.Menu;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.TextView;

public class MainActivity extends Activity {

	EditText myEditText;
	Button myButton;
	TextView myTextView;
	String key=null;
	String request;
	
	
	
	 //SparseArray<Group> groups = new SparseArray<Group>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.schematic_main);
		
		 String myString = null;
                try {

                    InputStream is = getAssets().open("sample.js");

                    int size = is.available();

                    byte[] buffer = new byte[size];

                    is.read(buffer);

                    is.close();

                    myString = new String(buffer, "UTF-8");


                } catch (IOException ex) {
                    ex.printStackTrace();
                }
				
                Route route = new Route();
    			Gson gson = new Gson();
    			//System.out.println(myString);
    			JsonParser parser = new JsonParser();
    			JsonObject json = (JsonObject) parser.parse(myString);
    			//System.out.println(json.toString());
    			route.setRoute(json);
    			System.out.println(gson.toJson(route));
            
    		    //createData();
    		    ExpandableListView listView = (ExpandableListView) findViewById(R.id.listView);
    		    ExpandableListAdapter adapter = new ExpandableListAdapter(this, route);
    		    listView.setAdapter(adapter);
    		   
    		    ((ExpandableListView) listView).setGroupIndicator(getResources().getDrawable(R.drawable.bus_icon));
    		    myTextView = (TextView) findViewById(R.id.textView1); 
    		    //myTextView.setText("From: ");
    		   
    		    myTextView.setText("From: " + route.getStart() + ", to " + route.getDestination());
    		  }
			/*
    		  public void createData() {
    		    for (int j = 0; j < 5; j++) {
    		      Group group = new Group("Test " + j);
    		      for (int i = 0; i < 5; i++) {
    		        group.children.add("Sub Item" + i);
    		      }
    		      groups.append(j, group);
    		    }
    		  }*/
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	

	
}


