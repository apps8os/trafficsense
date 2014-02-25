package org.apps8os.trafficsense.pebble;


import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apps8os.trafficsense.core.Waypoint;
import org.json.JSONArray;
import org.json.JSONObject;

import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
Handles all communication between Android and Pebble

TODO there is no .Stop() which stops the Pebble thread and unregister receivers!!
 */
public class PebbleCommunication {
	private static final UUID APP_UUID = UUID.fromString("83eef382-21a4-473a-a189-ceffa42f86b1");
	private static final int KEY_COMMAND = 0;
	private static final int COMMAND_GET_STOP = 0;
	private static final int COMMAND_UPDATELIST = 1;
	private static final int KEY_STOP_NUM = 1;
	private static final int KEY_STOP_NAME = 2;
	private static final int KEY_STOP_CODE = 3;
	private static final int KEY_STOP_TIME = 4;
	private static final int MAX_DICT_SIZE = 124;
	public static final int NUM_STOPS = 3;
	
	private Context mContext;
	private Thread mMessageManagerThread;
	private final MessageManager messageManager;
	private PebbleKit.PebbleAckReceiver ackReceiver;
    private PebbleKit.PebbleNackReceiver nackReceiver;
    
	public PebbleCommunication(Context applicationContext) {
		mContext = applicationContext;
		messageManager = new MessageManager(mContext, APP_UUID);
		// Start a new thread for MessageManager
		mMessageManagerThread = new Thread(messageManager);
		mMessageManagerThread.start();
		registerReceivers();
	}


	/** Starts the Pebble app */
	public void startAppOnPebble() {
		PebbleKit.startAppOnPebble(mContext, APP_UUID);
	}
	
	public void sendMessage(String title, String message) {
		// TODO: test whether this method is working with the First prototype app.
		// Requires the implementation of the alarm service
    	final Intent i = new Intent("com.getpebble.action.SEND_NOTIFICATION");
    	
    	final Map data = new HashMap();
    	data.put("title", title);
    	data.put("body", message);
    	final JSONObject jsonData = new JSONObject(data);
    	final String notificationData = new JSONArray().put(jsonData).toString();
    	
    	i.putExtra("messageType", "PEBBLE_ALERT");
        i.putExtra("sender", "TrafficSense");
        i.putExtra("notificationData", notificationData);
        
        Log.d("MainActivity", "About to send a modal alert to Pebble: " + notificationData);
        mContext.sendBroadcast(i);
	}
	
    public void sendWaypoint(Waypoint waypoint, int listIndex) {
    	String name = ""; String time = ""; String code = "";
    	if (waypoint != null) {
			name = waypoint.getWaypointName();
			System.out.println("DBG sendWaypoint: " + name);
			time = waypoint.getWaypointTime();
			code = waypoint.getWaypointStopCode();
    	}
    	// Sends a single stop to Pebble to a place of the list defined by stopNum
    	int charLimit = Math.min(name.length(), 20);
    	name = name.substring(0, charLimit); //limit to charLimit characters
		PebbleDictionary dictionary = new PebbleDictionary();
		dictionary.addUint8(KEY_COMMAND, (byte)COMMAND_GET_STOP);
		dictionary.addUint8(KEY_STOP_NUM, (byte)listIndex);
		dictionary.addString(KEY_STOP_NAME, name);
		dictionary.addString(KEY_STOP_CODE, code);
		dictionary.addString(KEY_STOP_TIME, time);
		// Offer the message to messageManager, which will handle queuing and finally sending the message
		messageManager.offer(dictionary);
		Log.i("Pebble", "Stop passed to messageManager with name" + name);
    }
    
    public void updateList(Waypoint waypoint) {
    	String name = ""; String time = ""; String code = "";
    	if (waypoint != null) {
			name = waypoint.getWaypointName();
			time = waypoint.getWaypointTime();
			code = waypoint.getWaypointStopCode();
    	}
    	// Sends a single stop to Pebble to a place of the list defined by stopNum
    	int charLimit = Math.min(name.length(), 20);
    	name = name.substring(0, charLimit); //limit to charLimit characters
		PebbleDictionary dictionary = new PebbleDictionary();
		dictionary.addUint8(KEY_COMMAND, (byte)COMMAND_UPDATELIST);
		dictionary.addString(KEY_STOP_NAME, name);
		dictionary.addString(KEY_STOP_CODE, code);
		dictionary.addString(KEY_STOP_TIME, time);
		// Offer the message to messageManager, which will handle queuing and finally sending the message
		messageManager.offer(dictionary);
		Log.i("Pebble", "Stop passed to messageManager with name" + name);
    }
	
	/** 
	 * Initialize and register ackReceiver and nackReceiver.
	 * They are used by MessageHandler to know whether a message is properly handled
	 * by Pebble or not.
	 */
	private void registerReceivers() {
		ackReceiver = new PebbleKit.PebbleAckReceiver(APP_UUID) {
            @Override
            public void receiveAck(final Context context, final int transactionId) {
            	// Notify messageManager that a message with transaction ID transactionId has been received
                messageManager.notifyAckReceivedAsync();
            }
        };
        PebbleKit.registerReceivedAckHandler(mContext, ackReceiver);

        nackReceiver = new PebbleKit.PebbleNackReceiver(APP_UUID) {
            @Override
            public void receiveNack(final Context context, final int transactionId) {
            	// Notify messageManager that a message with transaction ID transactionId was dropped
                messageManager.notifyNackReceivedAsync();
            }
        };
        PebbleKit.registerReceivedNackHandler(mContext, nackReceiver);
	}
	
	
	/**
	 * Should be called when there  is no need to communicate with Pebble anymore
	 */
	public void stop() {
		unRegisterReceivers();
		messageManager.stop();
	}
	
	private void unRegisterReceivers() {
        if (ackReceiver != null) {
            mContext.unregisterReceiver(ackReceiver);
            ackReceiver = null;
        }

        if (nackReceiver != null) {
            mContext.unregisterReceiver(nackReceiver);
            nackReceiver = null;
        }
	}
	
	
}
