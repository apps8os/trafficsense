/**
 * 
 */
package org.apps8os.trafficsense.first;

import java.util.ArrayList;

import android.content.Context;


/**
 * Controls the Pebble UI
 *
 */
public class PebbleUiController {
	PebbleCommunication mPblCom;
	Route mRoute;
	public PebbleUiController(Context appContext, Route route) {
		mPblCom = new PebbleCommunication(appContext);
		mRoute = route;
	}
	
	/*
	 * Should be called whenever the segment changes. Initializes
	 * the list of stops shown in Pebble screen to show the 1st, 2nd
	 * and the last stop of the segment.
	 */
	public void initializeList() {
		Segment newSegment = mRoute.getCurrentSegment();
		ArrayList<Waypoint> waypoints = new ArrayList<Waypoint>();
		// Add the waypoints from the segment to the list
		int maxIndex = Math.min(newSegment.getWaypointList().size() - 1, PebbleCommunication.NUM_STOPS - 1 );
		for (int i = 0; i < maxIndex; i++) {
			if (newSegment.getWaypoint(i) == null)
				System.out.println("DBG waypoint at index " + i + " was null");
			waypoints.add(newSegment.getWaypoint(i));
		}
		waypoints.add(newSegment.getLastWaypoint());
		if (newSegment.getLastWaypoint() == null)
			System.out.println("DBG last waypoint was null");
		// Send the waypoints to Pebble
		for (int i = 0; i < maxIndex + 1; i++) {
			mPblCom.sendWaypoint(waypoints.get(i), i);
		}
	}
	
	public void updateList() {
		System.out.println("updatelist s " + mRoute.getCurrentIndex());
		System.out.println("updatelist w " + mRoute.getCurrentSegment().getCurrentIndex());
		if (mRoute.getCurrentSegment().getCurrentIndex() == 0) {
			return;
		}
		int newWaypoint = mRoute.getCurrentSegment().getCurrentIndex() + PebbleCommunication.NUM_STOPS - 2;
		Waypoint waypoint;
		if (newWaypoint > mRoute.getCurrentSegment().getWaypointList().size() - 1) {
			waypoint = null;
		} else {
			waypoint = mRoute.getCurrentSegment().getWaypoint(newWaypoint);
		}
		mPblCom.updateList(waypoint);
	}
	
	public void alarmGetOff() {
		mPblCom.sendMessage("Alarm", "Get off on the next stop!");
	}
}
