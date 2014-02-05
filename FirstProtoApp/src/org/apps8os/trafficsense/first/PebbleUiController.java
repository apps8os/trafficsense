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
	public PebbleUiController(PebbleCommunication pebbleCommunication, Route journey) {
		mPebbleCommunication = pebbleCommunication;
		
		journey.getSegmentList().get(0).getWaypointList().get(0).getWaypointStopCode();
	}
	
	public void updateList(int currentSegment, int currentStop) {
		// TODO: Update the list of stops in pebble to show the new list of stops
		// starting from currentStop. Depends on the implementation of the
		// journey's data structure.
	}
	
	public void alarmGetOff() {
		mPebbleCommunication.sendMessage("Alarm", "Get off on the next stop!");
	}
}
