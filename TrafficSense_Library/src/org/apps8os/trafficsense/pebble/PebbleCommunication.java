package org.apps8os.trafficsense.pebble;


import java.util.HashMap;
import java.util.UUID;

import org.apps8os.trafficsense.TrafficsenseContainer;

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
	
	private static final int COMMAND_INIT_SEGMENT = 2;
	private static final int KEY_LINE_NUMBER = 1;
	private static final int KEY_FIRST_STOP_NAME = 2; // Don't confuse with KEY_STOP_NAME and -_CODE1
	private static final int KEY_FIRST_STOP_CODE = 3;
	private static final int KEY_START_TIME_HOUR = 4;
	private static final int KEY_START_TIME_MIN = 5;
	private static final int KEY_START_TIME_SEC = 6;
	
	private static final int COMMAND_SHOW_3STOP_WINDOW = 3;
	//private static final int MAX_DICT_SIZE = 124;
	public static final int NUM_STOPS = 3;
	
	/**
	 * Commands that can be sent from pebble to android
	 */
	// Command for getting required data, stop names etc. e.g. when the user starts
	// the pebble app in the middle of the journey
	private static final int PEBBLE_COMMAND_GET = 0;
	
	
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
	
	private TrafficsenseContainer mContainer;
	
	/**
	 * Constructor.
	 * 
	 * @param applicationContext the Context in which we works.
	 */
	public PebbleCommunication(Context applicationContext) {
		mContext = applicationContext;
		messageManager = new MessageManager(mContext, APP_UUID);
		mContainer = TrafficsenseContainer.getInstance();
		// The thread is started in startAppOnPebble()
		PebbleKit.registerReceivedDataHandler(mContext, new PebbleKit.PebbleDataReceiver(APP_UUID) {
			@Override
			public void receiveData(final Context context, final int transactionId, final PebbleDictionary data) {
				if (data.getUnsignedInteger(KEY_COMMAND) == PEBBLE_COMMAND_GET) {
					// Information requested from Pebble
					if (mContainer.getPebbleUiController() != null && mContainer.isJourneyStarted()) {
						mContainer.getPebbleUiController().totalUpdate();
					}
					PebbleKit.sendAckToPebble(mContext, transactionId);
				} else {
					// Not ready to send anything, send nack
					PebbleKit.sendNackToPebble(mContext, transactionId);
				}
			}
		});
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
	 * Swithches the screen shown in pebble to the 3 stop list
	 */
	public void switchTo3stopScreen() {
		PebbleDictionary dictionary = new PebbleDictionary();
		dictionary.addUint8(KEY_COMMAND, (byte) COMMAND_SHOW_3STOP_WINDOW);
		/**
		 * En-queue this message to MessageHandler.
		 */
		if (messageManager.offer(dictionary) == false) {
			System.out.println("DBG PebbleCommunicaitonswitchTo3stopScreen error: " +
					"could not offer dictionary");
		} else {
			System.out.println("DBG PebbleCommunication switchTo3stopScreen() success");
		}
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
		String notificationPayload = "[" + gson.toJson(payload) + "]";
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
		// If waypoint = null, send empty waypoints to scroll the list
		String code;
		String name;
		String time;
		if (waypoint == null) {
			name = "";
			code = "";
			time = "";
		} else {
			name = waypoint.getWaypointName();
			code = waypoint.getWaypointStopCode();
			time = waypoint.getWaypointTime();
		}
		// Maximum 20 characters.
		int charLimit = Math.min(name.length(), 20);
		name = name.substring(0, charLimit);
		PebbleDictionary dictionary = new PebbleDictionary();
		dictionary.addUint8(KEY_COMMAND, (byte) COMMAND_GET_STOP);
		dictionary.addUint8(KEY_STOP_NUM, (byte) listIndex);
		dictionary.addString(KEY_STOP_NAME, name);
		dictionary.addString(KEY_STOP_CODE, code);
		dictionary.addString(KEY_STOP_TIME, time);
		/**
		 * En-queue this message to MessageHandler.
		 */
		messageManager.offer(dictionary);
		System.out.println("DBG pebbleCommunication sendWaypoint: " + name);
	}

	/**
	 * Scroll the Pebble list UI one waypoint forward.
	 * 
	 * @param waypoint new waypoint to be added to the list.
	 */
	public void updateList(Waypoint waypoint) {
		// If waypoint = null, send empty waypoints to scroll the list
		String code;
		String name;
		String time;
		if (waypoint == null) {
			System.out.println("DBG updateList waypoint = null");
			name = "";
			code = "";
			time = "";
		} else {
			name = waypoint.getWaypointName();
			code = waypoint.getWaypointStopCode();
			time = waypoint.getWaypointTime();
		}
		// Maximum 20 characters.
		int charLimit = Math.min(name.length(), 20);
		name = name.substring(0, charLimit);
		PebbleDictionary dictionary = new PebbleDictionary();
		dictionary.addUint8(KEY_COMMAND, (byte) COMMAND_UPDATELIST);
		dictionary.addString(KEY_STOP_NAME, name);
		dictionary.addString(KEY_STOP_CODE, code);
		dictionary.addString(KEY_STOP_TIME, time);
		/**
		 * En-queue this message to MessageHandler.
		 */
		messageManager.offer(dictionary);
		System.out.println("DBG updateList: " + name);
	}
	
	/**
	 * Sends values like transport mode (line number) and other segment related values
	 *  Doesn't send the waypoints, they have to be sent after calling this.
	 *  Parameters: line = transport line e.g. 550, hours = hours of day, 
	 *  minutes = minutes of hour...
	 */
	public void initializeSegment(String line, String stopName, String stopCode, int hours, int minutes, int seconds) {
		PebbleDictionary dictionary = new PebbleDictionary();
		dictionary.addUint8(KEY_COMMAND, (byte) COMMAND_INIT_SEGMENT);
		dictionary.addString(KEY_LINE_NUMBER, line);
		dictionary.addString(KEY_FIRST_STOP_NAME, stopName);
		dictionary.addString(KEY_FIRST_STOP_CODE, stopCode);
		dictionary.addUint8(KEY_START_TIME_HOUR, (byte) hours);
		dictionary.addUint8(KEY_START_TIME_MIN, (byte) minutes);
		dictionary.addUint8(KEY_START_TIME_SEC, (byte) seconds); 
		/**
		 * En-queue this message to MessageHandler.
		 */
		System.out.println("PEBBLE_DBG: sending time:" + hours + "." + minutes + "." + seconds);
		
		messageManager.offer(dictionary);
		System.out.println("DBG PebbleCommunication:initializeSegment: " + line);
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
