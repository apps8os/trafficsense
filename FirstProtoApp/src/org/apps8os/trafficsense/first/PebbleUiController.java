/**
 * 
 */
package org.apps8os.trafficsense.first;


/**
 * Controls the Pebble UI
 *
 */
public class PebbleUiController {
	PebbleCommunication mPebbleCommunication;
	Route mRoute;
	public PebbleUiController(PebbleCommunication pebbleCommunication, Route route) {
		mPebbleCommunication = pebbleCommunication;
		mRoute = route;
	}
	
	
	public void updateList(int currentSegment, int currentWaypoint) {
		// TODO: Update the list of stops in pebble to show the new list of stops
		// starting from currentStop. Depends on the implementation of the
		// journey's data structure.
		Waypoint s = mRoute.getSegmentList().get(currentSegment).getWaypointList().get(currentWaypoint);
		
	}
	
	public void alarmGetOff() {
		mPebbleCommunication.sendMessage("Alarm", "Get off on the next stop!");
	}
}
