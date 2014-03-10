package org.apps8os.trafficsense.pebble;

import java.util.ArrayList;

import org.apps8os.trafficsense.core.Route;
import org.apps8os.trafficsense.core.Segment;
import org.apps8os.trafficsense.core.Waypoint;

/**
 * Class for Pebble user interface controller.
 */
public class PebbleUiController {
	private PebbleCommunication mPblCom;
	private Route mRoute;

	/**
	 * Constructor.
	 * 
	 * @param comm PebbleCommunication link.
	 * @param route route object to use.
	 */
	public PebbleUiController(PebbleCommunication comm, Route route) {
		mPblCom = comm;
		mRoute = route;
	}

	/**
	 * Sets Pebble UI to the first two and the last stop of current segment.
	 * 
	 * Should be called whenever the segment changes. Sets Pebble UI to the
	 * first two and the last stop of current segment.
	 */
	public void initializeList() {
		Segment newSegment = mRoute.getCurrentSegment();
		if (newSegment.isWalking()) {
			return;
		}
		ArrayList<Waypoint> waypoints = new ArrayList<Waypoint>();
		
		// Add the waypoints from the segment to the list
		//int maxIndex = Math.min(newSegment.getWaypointList().size() - 1, PebbleCommunication.NUM_STOPS - 1 );
		for (int i = 0; i < PebbleCommunication.NUM_STOPS - 1; i++) {
			if (newSegment.getWaypoint(i) == null) {
				waypoints.add(null);
			} else {
				waypoints.add(newSegment.getWaypoint(i));
			}
		}
		waypoints.add(newSegment.getLastWaypoint());
		if (newSegment.getLastWaypoint() == null) {
			System.out.println("DBG initializeList last waypoint was null");
		}
		// Send the waypoints to Pebble
		for (int i = 0; i < PebbleCommunication.NUM_STOPS; i++) {
			mPblCom.sendWaypoint(waypoints.get(i), i);
		}
	}

	/**
	 * Scrolls Pebble UI NUM_STOPS stops forward.
	 */
	public void updateList() {
		System.out.println("DBG updateList s " + mRoute.getCurrentIndex());
		System.out.println("DBG updateList w "
				+ mRoute.getCurrentSegment().getCurrentIndex());
		if (mRoute.getCurrentSegment().getCurrentIndex() == 0) {
			return;
		}
		int newWaypoint = mRoute.getCurrentSegment().getCurrentIndex()
				+ PebbleCommunication.NUM_STOPS - 2;
		Waypoint waypoint;
		if (newWaypoint > mRoute.getCurrentSegment().getWaypointList().size() - 1) {
			waypoint = null;
		} else {
			waypoint = mRoute.getCurrentSegment().getWaypoint(newWaypoint);
		}
		mPblCom.updateList(waypoint);
	}

	/**
	 * Triggers a 'Get off' alarm on Pebble.
	 */
	public void alarmGetOff() {
		mPblCom.sendMessage("Alarm", "Get off on the next stop!");
	}
}
