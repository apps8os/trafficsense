/**
 * 
 */
package org.apps8os.trafficsense.first;

import android.content.Context;

/**
 * Controls the Pebble UI
 *
 */
public class PebbleUiController {
	PebbleCommunication mPebbleCommunication;
	public PebbleUiController(PebbleCommunication pebbleCommunication) {
		mPebbleCommunication = pebbleCommunication;
	}
	
	public void updateList(int currentStop) {
		// TODO: Update the list of stops in pebble to show the new list of stops
		// starting from currentStop. Depends on the implementation of the
		// journey's data structure.
	}
	
	public void alarmGetOff() {
		mPebbleCommunication.sendMessage("Alarm", "Get off on the next stop!");
	}
}
