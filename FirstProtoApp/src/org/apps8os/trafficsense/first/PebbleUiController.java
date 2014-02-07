/**
 * 
 */
package org.apps8os.trafficsense.first;

import java.util.ArrayList;


/**
 * Controls the Pebble UI
 *
 */
public class PebbleUiController {
	PebbleCommunication mPblCom;
	Route mRoute;
	public PebbleUiController(PebbleCommunication pebbleCommunication, Route route) {
		mPblCom = pebbleCommunication;
		mRoute = route;
	}
	
	/*
	 * Should be called whenever the segment changes. Initializes
	 * the list of stops shown in Pebble screen to show the 1st, 2nd
	 * and the last stop of the segment.
	 */
	public void initializeList(Segment newSegment) {
		ArrayList<Waypoint> waypoints = new ArrayList<Waypoint>();
		// Add the waypoints from the segment to the list
		for (int i = 0; i < PebbleCommunication.NUM_STOPS - 1; i++) {
			waypoints.add(newSegment.getWaypoint(i));
		}
		waypoints.add(newSegment.getLastWaypoint());
		// Send the waypoints to Pebble
		for (int i = 0; i < PebbleCommunication.NUM_STOPS; i++) {
			mPblCom.sendWaypoint(waypoints.get(i), i);
		}
	}
	
	public void updateList(int currentSegment, int currentWaypoint) {
		// TODO: Update the list of stops in pebble to show the new list of stops
		// starting from currentStop. Depends on the implementation of the
		// journey's data structure.
		Waypoint s = mRoute.getSegmentList().get(currentSegment).getWaypointList().get(currentWaypoint);
		
	}
	
	public void alarmGetOff() {
		mPblCom.sendMessage("Alarm", "Get off on the next stop!");
	}
}
