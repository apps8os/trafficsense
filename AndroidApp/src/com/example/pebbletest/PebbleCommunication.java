/**
 * 
 */
package com.example.pebbletest;

import android.content.Context;
import android.content.Intent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.view.View.OnClickListener;
import android.app.Activity;

import java.util.Map;
import java.util.HashMap;
import java.util.UUID;

import org.json.JSONObject;
import org.json.JSONArray;

import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;
/**
 * @author traffisense
 *
 */
public class PebbleCommunication {
	private static final UUID APP_UUID = UUID.fromString("83eef382-21a4-473a-a189-ceffa42f86b1");
	private static final int BUFFER_LENGTH = 64;
	private static final int KEY_COMMAND = 0;
	private static final int COMMAND_GET_STOP = 0;
	private static final int KEY_STOP_NUM = 1;
	private static final int KEY_STOP_NAME = 2;
	private static final int KEY_STOP_CODE = 3;
	private static final int KEY_STOP_TIME = 4;
	
	private Context mApplicationContext;
	
	private PebbleKit.PebbleDataReceiver dataHandler;
	
	public void PebbleCommunication(Context applicationContext) {
		// Set the android application context
		mApplicationContext = applicationContext;
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
		//PebbleKit.sendDataToPebbleWithTransactionId(getApplicationContext(), APP_UUID, dictionary, stopNum);
    }
	
	public void startAppOnPebble() {
		PebbleKit.startAppOnPebble(mApplicationContext, APP_UUID);
	};
}
