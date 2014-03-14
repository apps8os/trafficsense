package com.org.hslapitest;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.Calendar;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.util.JsonReader;
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
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		myButton = (Button) findViewById(R.id.button1);
		myEditText = (EditText) findViewById(R.id.editText1);
		myTextView = (TextView) findViewById(R.id.textView2);
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
    			//System.out.println(gson.toJson(route));
            }
           
       
		
		
	
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	

	
}


