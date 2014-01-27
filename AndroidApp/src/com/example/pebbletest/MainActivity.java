package com.example.pebbletest;

import android.os.Bundle;
import android.os.Handler;
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
import com.getpebble.android.kit.PebbleKit.PebbleAckReceiver;
import com.getpebble.android.kit.util.PebbleDictionary;

public class MainActivity extends Activity {

	private static final UUID APP_UUID = UUID.fromString("83eef382-21a4-473a-a189-ceffa42f86b1");
	private static final int BUFFER_LENGTH = 64;
	private static final int KEY_COMMAND = 0;
	private static final int COMMAND_GET_STOP = 0;
	private static final int KEY_STOP_NUM = 1;
	private static final int KEY_STOP_NAME = 2;
	private static final int KEY_STOP_CODE = 3;
	private static final int KEY_STOP_TIME = 4;
	
	public boolean mPebbleAckReceived = false;
	
	private PebbleKit.PebbleDataReceiver dataHandler;
	
	private int s = 0;
	
	private final MessageManager messageManager = new MessageManager(MainActivity.this, APP_UUID);
	private PebbleKit.PebbleAckReceiver ackReceiver;
    private PebbleKit.PebbleNackReceiver nackReceiver;
	
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
		
        ackReceiver = new PebbleKit.PebbleAckReceiver(APP_UUID) {
            @Override
            public void receiveAck(final Context context, final int transactionId) {
                messageManager.notifyAckReceivedAsync();
            }
        };

        PebbleKit.registerReceivedAckHandler(this, ackReceiver);


        nackReceiver = new PebbleKit.PebbleNackReceiver(APP_UUID) {
            @Override
            public void receiveNack(final Context context, final int transactionId) {
                messageManager.notifyNackReceivedAsync();
            }
        };

        PebbleKit.registerReceivedNackHandler(this, nackReceiver);
        
	}
	
	@Override
    protected void onPause() {
        super.onPause();

        // Always deregister any Activity-scoped BroadcastReceivers when the Activity is paused
        if (dataHandler != null) {
            unregisterReceiver(dataHandler);
            dataHandler = null;
        }

        if (ackReceiver != null) {
            unregisterReceiver(ackReceiver);
            ackReceiver = null;
        }

        if (nackReceiver != null) {
            unregisterReceiver(nackReceiver);
            nackReceiver = null;
        }
    }
	
	@Override
    public void onStart() {
        // FIXME do I need to do any cleanup in onStop()?
        super.onStart();
        new Thread(messageManager).start();

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
        		/*
        		PebbleAckReceiver r = new PebbleAckReceiver(APP_UUID) {
  				  
  		  		  @Override
  		  		  public void receiveAck(Context context, int transactionId) {
  		  			Log.i(getLocalClassName(), "checkpoint c");
  		  		    Log.i(getLocalClassName(), "Received ack for transaction " + transactionId);
  		  		    MainActivity.this.notify(); // This stops the calling thread from waiting
  		  		    //MainActivity.this.mPebbleAckReceived = true;
  		  		  }};
  		  		
  		  		PebbleKit.registerReceivedAckHandler(MainActivity.this, r);
  		  		
        		//sendMessageToPebble(message.getText().toString());
  		  		mPebbleAckReceived = false;
  		  	    Log.i(getLocalClassName(), "checkpoint a");
        		sendStop("Ääkkösiä", "1234", "15:29", 0);
        		r.getResultCode();
        		Log.i(getLocalClassName(), "checkpoint b");
        		try {
        			synchronized (this) {
        			  this.wait(30000);
        			}
        		} catch (InterruptedException e) {
        			// When current thread is activated before r is ready
        			e.printStackTrace();
        		}
        		Log.i(getLocalClassName(), "checkpoint d");
        		sendStop("Kemisti", "E1234", "15:59", 1);
        		try {
        			synchronized (this) {
        			  this.wait(30000);
        			}
        		} catch (InterruptedException e) {
        			// When current thread is activated before r is ready
        			e.printStackTrace();
        		}
        		sendStop("Some stop", "Ki1234", "16:59", 2);
        		try {
        			synchronized (this) {
        			  this.wait(30000);
        			}
        		} catch (InterruptedException e) {
        			// When current thread is activated before r is ready
        			e.printStackTrace();
        		}
        		*/
        		sendStop("Ääkkösiä", "1234", "15:29", 0);
        		sendStop("Kemisti", "E1234", "15:59", 1);
        		sendStop("Some stop", "Ki1234", "16:59", 2);
        		Log.i(getLocalClassName(), "Button clicked");
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
    

    private void sendStop(String stopName, String stopCode, String time, int stopNum) {
    	// Sends a single stop to Pebble to a place of the list defined by stopNum
    	int charLimit = Math.min(stopName.length(), 20);
    	stopName = stopName.substring(0, charLimit); //limit to charLimit characters
		PebbleDictionary dictionary = new PebbleDictionary();
		dictionary.addUint8(KEY_COMMAND, (byte)COMMAND_GET_STOP);
		dictionary.addUint8(KEY_STOP_NUM, (byte)stopNum);
		dictionary.addString(KEY_STOP_NAME, stopName);
		dictionary.addString(KEY_STOP_CODE, stopCode);
		dictionary.addString(KEY_STOP_TIME, time);
		messageManager.offer(dictionary);
		
		//PebbleKit.sendDataToPebbleWithTransactionId(getApplicationContext(), APP_UUID, dictionary, stopNum);
		/**try {
			synchronized (this) {
			  wait(10000);
			}
		} catch (InterruptedException e) {
			// When current thread is activated before r is ready
			e.printStackTrace();
		}
		**/
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    /**private class SendStop implements Runnable {
    	private String mStopName;
    	private String mStopCode;
    	private String mTime;
    	private int mStopNum;
    	
    	public SendStop(String stopName, String stopCode, String time, int stopNum) {
    	  mStopName = stopName;
    	  mStopCode = stopCode;
    	  mTime = time;
    	  mStopNum = stopNum;
    	}
		@Override
		public void run() {
			synchronized(this){
		    	// Sends a single stop to Pebble to a place of the list defined by stopNum
		    	int charLimit = Math.min(mStopName.length(), 20);
		    	mStopName = mStopName.substring(0, charLimit); //limit to charLimit characters
				PebbleDictionary dictionary = new PebbleDictionary();
				dictionary.addUint8(KEY_COMMAND, (byte)COMMAND_GET_STOP);
				dictionary.addUint8(KEY_STOP_NUM, (byte)mStopNum);
				dictionary.addString(KEY_STOP_NAME, mStopName);
				dictionary.addString(KEY_STOP_CODE, mStopCode);
				dictionary.addString(KEY_STOP_TIME, mTime);
				PebbleKit.sendDataToPebbleWithTransactionId(getApplicationContext(), APP_UUID, dictionary, mStopNum);
				
				/**try {
				wait(3000);
				
				} catch (InterruptedException e) {
					// When current thread is activated before r is ready
					e.printStackTrace();
				}
			}
		}
    	
    }**/
    
}
