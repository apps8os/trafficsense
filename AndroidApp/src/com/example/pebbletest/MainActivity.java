package com.example.pebbletest;

import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.View;

import android.content.Context;
import android.content.Intent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.view.View.OnClickListener;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;

import org.json.JSONObject;
import org.json.JSONArray;

import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;

public class MainActivity extends Activity {

	private static final UUID APP_UUID = UUID.fromString("83eef382-21a4-473a-a189-ceffa42f86b1");
	private static final int BUFFER_LENGTH = 64;
	private PebbleKit.PebbleDataReceiver dataHandler;
	
	@Override
	public void onResume() {
		super.onResume();
		
		PebbleKit.startAppOnPebble(getApplicationContext(), APP_UUID);
		
		dataHandler = new PebbleKit.PebbleDataReceiver(APP_UUID) {
			
			@Override
			public void receiveData(Context context, int transactionId,
					PebbleDictionary data) {
				PebbleKit.sendAckToPebble(context, transactionId);
				
				int dataValue = data.getUnsignedInteger(0).intValue();
				
				if (dataValue == 0) {
					TextView text = (TextView)findViewById(R.id.textView1);
					text.setText("0 received");
				} else {
					TextView text = (TextView)findViewById(R.id.textView1);
					text.setText("0 not received");
				}
			}
		};
		
		PebbleKit.registerReceivedDataHandler(getApplicationContext(), dataHandler);
	}
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //sendMessageToPebble("Hello Pebble");
        
        Button send = (Button)findViewById(R.id.button1);
        
        final EditText message = (EditText)findViewById(R.id.editText1);
        
        send.setOnClickListener(new OnClickListener() {
        	@Override
        	public void onClick(View v) {
        		//sendMessageToPebble(message.getText().toString());
        		sendStop("Alvar Aallon puisto", "12:59", 1);
        	}
        });
    }
    
    private void sendMessageToPebble(String message) {
    	// send a message straight to the pebble, not the app
    	final Intent i = new Intent("com.getpebble.action.SEND_NOTIFICATION");
    	
    	final Map data = new HashMap();
    	data.put("title", "Test Message");
    	data.put("body", message);
    	final JSONObject jsonData = new JSONObject(data);
    	final String notificationData = new JSONArray().put(jsonData).toString();
    	
    	i.putExtra("messageType", "PEBBLE_ALERT");
        i.putExtra("sender", "PebbleTest");
        i.putExtra("notificationData", notificationData);
        
        Log.d("MainActivity", "About to send a modal alert to Pebble: " + notificationData);
        sendBroadcast(i);
    }
    

    private void sendStop(String stopName, String time, int stop) {
    	int charLimit = Math.min(stopName.length(), 20);
    	stopName = stopName.substring(0, charLimit); //limit to charLimit characters
    	String s = stopName + " - " + time;
    	if (s.length() < BUFFER_LENGTH) {
    		PebbleDictionary dictionary = new PebbleDictionary();
    		//key = stop, value = s
    		dictionary.addString(stop, s);
    		PebbleKit.sendDataToPebble(getApplicationContext(), APP_UUID, dictionary);
    	} else {
    		Log.i("sendStringToPebble", "string too long");
    	}
    
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
}
