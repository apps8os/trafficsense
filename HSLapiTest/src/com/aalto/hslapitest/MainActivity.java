package com.aalto.hslapitest;

import java.util.Calendar;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	EditText myEditText;
	Button myButton;
	TextView myTextView;
	String key=null;
	String request;
	private PendingIntent pendingIntent;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		myButton = (Button) findViewById(R.id.button1);
		myEditText = (EditText) findViewById(R.id.editText1);
		myTextView = (TextView) findViewById(R.id.textView2);
		
		
		myButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {            	
            	
            	key=myEditText.getText().toString();
            	if(key.isEmpty()){
            		Toast toast = Toast.makeText(getApplicationContext(), "Searching...", Toast.LENGTH_SHORT);
    				toast.show();
            	}
            	
            	key = key.replace(" ", "_");
            	System.out.println(key);
            	String har = null;
            	Request Geo = new Request();
            	Geo.getGeocoding("","",key);
            	//String uri = null;
            	//Geo.sendRequest(uri);
            	//RequestTask task = new RequestTask(getBaseContext()); 
            	//task.execute();
				//task.execute("http://api.reittiopas.fi/hsl/prod/?request=geocode&user=trafficsenseuser&pass=trafficsense&format=txt&key="+key+"&epsg_out=4326"+"&p=");
				//Toast toast = Toast.makeText(getApplicationContext(), "Searching...", Toast.LENGTH_SHORT);
				//toast.show();*/
				
            }
       });
		
		
	
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	

	
}


