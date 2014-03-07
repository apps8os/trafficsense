package org.apps8os.trafficsense.pebble;


import java.util.HashMap;
import java.util.UUID;

import org.apps8os.trafficsense.android.Constants;
import org.apps8os.trafficsense.core.Waypoint;

import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;
import com.google.gson.Gson;

import android.content.Context;
import android.content.Intent;

/**
 * Handles all communication between Android side and Pebble.
 * 
 * The application should first instantiate this class, invoke {@link #startAppOnPebble()}
 * before any Pebble interaction, and {@link #stop()} after finished.
 * 
 * Instantiated/started by TrafficsenseContainer.
 * Mostly invoked from PebbleUiController
 */
public class PebbleCommunication {
	/**
	 * See appinfo.json of Pebble application.
	 */
	private static final UUID APP_UUID = UUID.fromString(Constants.PEBBLE_APP_UUID);
	private static final int KEY_COMMAND = 0;
	private static final int COMMAND_GET_STOP = 0;
	private static final int COMMAND_UPDATELIST = 1;
	private static final int KEY_STOP_NUM = 1;
	private static final int KEY_STOP_NAME = 2;
	private static final int KEY_STOP_CODE = 3;
	private static final int KEY_STOP_TIME = 4;
	//private static final int MAX_DICT_SIZE = 124;
	public static final int NUM_STOPS = 3;

	/**
	 * The Context in which we works.
	 */
	private Context mContext;
	/**
	 * Handle Pebble (Bluetooth) communication in another Thread. 
	 */
	private Thread mMessageManagerThread;
	/**
	 * Pebble MessageHandler.
	 */
	private final MessageManager messageManager;
	/**
	 * Handle Pebble messaging ACKs.
	 */
	private PebbleKit.PebbleAckReceiver ackReceiver;
	/**
	 * Handle Pebble messaging NACKs.
	 */
	private PebbleKit.PebbleNackReceiver nackReceiver;

	/**
	 * Constructor.
	 * 
	 * @param applicationContext the Context in which we works.
	 */
	public PebbleCommunication(Context applicationContext) {
		mContext = applicationContext;
		messageManager = new MessageManager(mContext, APP_UUID);
		// The thread is started in startAppOnPebble()
		mMessageManagerThread = new Thread(messageManager);
	}


	/**
	 * Start the MessageManager thread and the smart watch app on Pebble.
	 * Must be called before any other operations.
	 */
	public void startAppOnPebble() {
		mMessageManagerThread.start();
		registerReceivers();
		PebbleKit.startAppOnPebble(mContext, APP_UUID);
	}

	/**
	 * Send a text notification to Pebble.
	 * 
	 * @param title the title of the notification.
	 * @param message the content of the notification.
	 */
	public void sendMessage(String title, String message) {
		Intent i = new Intent("com.getpebble.action.SEND_NOTIFICATION");
		Gson gson = new Gson();
		HashMap<String, String> payload = new HashMap<String, String>();
		payload.put("title", title);
		payload.put("body", message);
		String notificationPayload = gson.toJson(payload);
		i.putExtra("messageType", "PEBBLE_ALERT");
		i.putExtra("sender", "TrafficSense");
		i.putExtra("notificationData", notificationPayload);
		System.out.println("DBG sendMessage " + notificationPayload);
		mContext.sendBroadcast(i);
	}

	/**
	 * Send a Waypoint to Pebble.
	 * This is usually invoked in a loop to initialize the Pebble UI at start.
	 * 
	 * @param waypoint the Waypoint.
	 * @param listIndex the sequence number of this Waypoint in the journey. 
	 */
	public void sendWaypoint(Waypoint waypoint, int listIndex) {
		if (waypoint == null) {
			System.out.println("DBG sendWaypoint waypoint = null");
			return;
		}
		String name = waypoint.getWaypointName();
		if (name == null) {
			name = "null";
		}
		// Maximum 20 characters.
		int charLimit = Math.min(name.length(), 20);
		name = name.substring(0, charLimit);
		PebbleDictionary dictionary = new PebbleDictionary();
		dictionary.addUint8(KEY_COMMAND, (byte) COMMAND_GET_STOP);
		dictionary.addUint8(KEY_STOP_NUM, (byte) listIndex);
		dictionary.addString(KEY_STOP_NAME, name);
		dictionary.addString(KEY_STOP_CODE, waypoint.getWaypointStopCode());
		dictionary.addString(KEY_STOP_TIME, waypoint.getWaypointTime());
		/**
		 * En-queue this message to MessageHandler.
		 */
		messageManager.offer(dictionary);
		System.out.println("DBG sendWaypoint: " + name);
	}

	/**
	 * Scroll the Pebble list UI one waypoint forward.
	 * 
	 * @param waypoint new waypoint to be added to the list.
	 */
	public void updateList(Waypoint waypoint) {
		if (waypoint == null) {
			System.out.println("DBG updateList waypoint = null");
			return;
		}
		String name = waypoint.getWaypointName();
		if (name == null) {
			name = "null";
		}
		// Maximum 20 characters.
		int charLimit = Math.min(name.length(), 20);
		name = name.substring(0, charLimit);
		PebbleDictionary dictionary = new PebbleDictionary();
		dictionary.addUint8(KEY_COMMAND, (byte) COMMAND_UPDATELIST);
		dictionary.addString(KEY_STOP_NAME, name);
		dictionary.addString(KEY_STOP_CODE, waypoint.getWaypointStopCode());
		dictionary.addString(KEY_STOP_TIME, waypoint.getWaypointTime());
		/**
		 * En-queue this message to MessageHandler.
		 */
		messageManager.offer(dictionary);
		System.out.println("DBG updateList: " + name);
	}

	/** 
	 * Set up Pebble ACK and NACK handlers.
	 * They are used by MessageHandler to know whether a message is
	 * properly handled by Pebble or not.
	 */
	private void registerReceivers() {
		ackReceiver = new PebbleKit.PebbleAckReceiver(APP_UUID) {
			@Override
			public void receiveAck(final Context context,
					final int transactionId) {
				/**
				 *  Notify messageManager that a message with transactionId
				 *  has been handled by Pebble.
				 */
				messageManager.notifyAckReceivedAsync(transactionId);
			}
		};
		PebbleKit.registerReceivedAckHandler(mContext, ackReceiver);

		nackReceiver = new PebbleKit.PebbleNackReceiver(APP_UUID) {
			@Override
			public void receiveNack(final Context context,
					final int transactionId) {
				/**
				 *  Notify messageManager that a message with transactionId
				 *  was dropped.
				 */
				messageManager.notifyNackReceivedAsync(transactionId);
			}
		};
		PebbleKit.registerReceivedNackHandler(mContext, nackReceiver);
	}


	/**
	 * Release Pebble messaging resources.
	 * Must be called when there is no need to communicate with Pebble anymore.
	 */
	public void stop() {
		unRegisterReceivers();
		messageManager.stop();
	}

	/**
	 * Unregister Pebble ACK and NACK handlers.
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
