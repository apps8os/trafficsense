package org.apps8os.trafficsense.pebble;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

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
	 * Initializes segment-related values on pebble, including
	 * the stops that need to be shown initially
	 */
	public void initializeSegment() {
		Segment currentSegment = mRoute.getCurrentSegment();
		// Don't do anything if the segment is walking
		if (currentSegment.isWalking()) return;
		
		String segmentMode = currentSegment.getSegmentMode();
		
		// do things to get the seconds of minute, minute of hour and hour of day
		System.out.println("DBG segment time: " + currentSegment.getSegmentStartTime());
		Date segmentStart = timeStringToDate(currentSegment.getSegmentStartTime());
		if (segmentStart == null) System.out.println("DBG Date segmentStart was null");
		
		Calendar cal = Calendar.getInstance();
		if (cal == null) System.out.println("DBG Calendar cal was null");
		cal.setTime(segmentStart);
		int seconds = cal.get(Calendar.SECOND);
		int minutes = cal.get(Calendar.MINUTE);
		int hours = cal.get(Calendar.HOUR);
		mPblCom.initializeSegment(segmentMode, hours, minutes, seconds);
		
		// Then, send the stops
		int currentWpIndex = currentSegment.getCurrentIndex();
		int lastWpIndex = currentSegment.getWaypointList().size() - 1;
		
		ArrayList<Waypoint> waypoints = new ArrayList<Waypoint>();
		for (int i = currentWpIndex; i < lastWpIndex; i++) {
			waypoints.add(currentSegment.getWaypointList().get(i));
		}
		// if there are less than NUM_STOPS-1 waypoints to be sent, send additional null
		// waypoints to clear the list (not including the last waypoint yet)
		for (int i = waypoints.size(); i < PebbleCommunication.NUM_STOPS-1; i++) {
			waypoints.add(null);
		}
		// finally add the last waypoint to the list
		waypoints.add(currentSegment.getLastWaypoint());
		for (int i = 0; i < PebbleCommunication.NUM_STOPS; i++) {
			mPblCom.sendWaypoint(waypoints.get(i), i);
		}
	}
	
	
	/**
	 * Sets Pebble UI to the first two and the last stop of current segment.
	 * 
	 * Should be called whenever the segment changes. Sets Pebble UI to the
	 * first two and the last stop of current segment.
	 */
	public void initializeList() {
		Segment newSegment = mRoute.getCurrentSegment();
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
	
	// TODO: stop using this and somehow get times as date objects from the route
	private Date timeStringToDate(String timeStr) {
		Date date = null;
		try {
			// TODO: Locale problem
			date = new SimpleDateFormat("kk:mm", Locale.ENGLISH)
			.parse(timeStr);
		} catch (ParseException e) {
			System.out.println("DBG TimeOnlyService timeStringToDate error: " + e.getMessage());
		}
		return date;
	}
}
