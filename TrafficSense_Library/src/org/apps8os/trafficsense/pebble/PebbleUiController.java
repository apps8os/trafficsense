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
	// Store the last segment and wp indices sent to Pebble
	private int mLastSegmentIndex;
	private int mLastWpIndex;

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
	
	/*
	 * Call this whenever anything needs to be updated in Pebble
	 */
	public void update() {
		int newSegmentIndex = mRoute.getCurrentIndex();
		int newWpIndex = mRoute.getCurrentSegment().getCurrentIndex();
		Segment newSegment = mRoute.getCurrentSegment();
		Waypoint newWaypoint = newSegment.getCurrentWaypoint();
		
		if (newSegmentIndex == mLastSegmentIndex) { //Still on the same segment
			// If we didn't jump over any stops, add the next wp to list. Else, update the whole list.
			if (newWaypoint == newSegment.getLastWaypoint()) {
				// If we are already on the last waypoint, initialize the new Segment
				initializeSegment();
			} else if (newWpIndex == mLastWpIndex + 1) {
				alarmIfNeeded();
				addWpToList();
			} else if (newWpIndex != mLastWpIndex) {
				// We've jumped over a wp, so update the whole list
				alarmIfNeeded();
				updateList();
			}
		} else {
			// Segment has changed
			if (newWpIndex == 0) {
				// Find the first non-walking segment (don't care about walking segments)
				for (int i = newSegmentIndex; i < mRoute.getSegmentList().size(); i++) {
					Segment seg = mRoute.getSegmentList().get(i);
					if (seg.isWalking() == false) {
						// When the first non-walking segment is found, initialize it on Pebble and stop the loop
						initializeSegment();
						break;
					}
				}
			} else {
				/* Segment changed, but we've jumped over it's first waypoint,
				 * so don't use initializeSegment
				 */
				updateList();
			}
		}
		// Update the segment indices before returning. Don't return from this method before this!
		mLastSegmentIndex = newSegmentIndex;
		mLastWpIndex = newWpIndex;
	}
	
	/*
	 * Send an alarm if we are on the second last stop
	 */
	private void alarmIfNeeded() {
		int newSegmentIndex = mRoute.getCurrentIndex();
		int newWpIndex = mRoute.getCurrentSegment().getCurrentIndex();
		Segment newSegment = mRoute.getCurrentSegment();
		if (newWpIndex == newSegment.getWaypointList().size() - 2) {
			// If we are at the second last wp, send the alarm
			// TODO: start a timer and do the alarm a little later to avoid confusion for the user
			alarmGetOff();
		}
	}
	
	/**
	 * Initializes segment-related values on pebble, including
	 * the stops that need to be shown initially. Also shows the screen
	 * that tells how many minutes until the vehicles comes.
	 * Should be only used when we are at the first wp of a non-walking segment.
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
		int hours = cal.get(Calendar.HOUR_OF_DAY);
		String stopName = currentSegment.getWaypoint(0).getWaypointName();
		String stopCode = currentSegment.getWaypoint(0).getWaypointStopCode();
		mPblCom.initializeSegment(segmentMode, stopName, stopCode, hours, minutes, seconds);
		
		// Then, send the stops
		updateList();
	}
	
	private void updateList() {
		Segment currentSegment = mRoute.getCurrentSegment();
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
	 * Scrolls PebbleUI 1 stop forward
	 */
	private void addWpToList() {
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
