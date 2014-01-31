/**
 * 
 */
package org.apps8os.trafficsense.first;

import java.util.UUID;

import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;

import android.content.Context;
import android.util.Log;

/**
Handles all communication between Android and Pebble
 */
public class PebbleCommunication {
	private static final UUID APP_UUID = UUID.fromString("83eef382-21a4-473a-a189-ceffa42f86b1");
	private static final int KEY_COMMAND = 0;
	private static final int COMMAND_GET_STOP = 0;
	private static final int KEY_STOP_NUM = 1;
	private static final int KEY_STOP_NAME = 2;
	private static final int KEY_STOP_CODE = 3;
	private static final int KEY_STOP_TIME = 4;
	private static final int MAX_DICT_SIZE = 124;
	
	private Context mContext;
	private final MessageManager messageManager;
	private PebbleKit.PebbleAckReceiver ackReceiver;
    private PebbleKit.PebbleNackReceiver nackReceiver;
    
	public PebbleCommunication(Context applicationContext) {
		mContext = applicationContext;
		messageManager = new MessageManager(mContext, APP_UUID);
		// Start a new thread for MessageManager
		new Thread(messageManager).start();
		registerReceivers();
	}


	/** Starts the Pebble app */
	public void startAppOnPebble() {
		PebbleKit.startAppOnPebble(mContext, APP_UUID);
	}
	
	public void sendAlarm(String text) {
		
		
	}
	
    public void sendStop(String stopName, String stopCode, String time, int stopNum) {
    	// Sends a single stop to Pebble to a place of the list defined by stopNum
    	int charLimit = Math.min(stopName.length(), 20);
    	stopName = stopName.substring(0, charLimit); //limit to charLimit characters
		PebbleDictionary dictionary = new PebbleDictionary();
		dictionary.addUint8(KEY_COMMAND, (byte)COMMAND_GET_STOP);
		dictionary.addUint8(KEY_STOP_NUM, (byte)stopNum);
		dictionary.addString(KEY_STOP_NAME, stopName);
		dictionary.addString(KEY_STOP_CODE, stopCode);
		dictionary.addString(KEY_STOP_TIME, time);
		// Offer the message to messageManager, which will handle queuing and finally sending the message
		messageManager.offer(dictionary);
		Log.i("Pebble", "Stop passed to messageManager with name" + stopName);
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
	 * Should be called whenever there is no need to receive acks/nacks or messages
	 * from Pebble anymore.
	 */
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
