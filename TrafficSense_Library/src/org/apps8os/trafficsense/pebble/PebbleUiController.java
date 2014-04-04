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
	private static final int WINDOW_BASIC = 0;
	private static final int WINDOW_3STOP = 1;
	
	private PebbleCommunication mPblCom;
	private Route mRoute;
	// Store the last segment and wp indices sent to Pebble
	private int mLastSegmentIndex;
	private int mLastWpIndex;
	private boolean mNoNonWalkingSegmentsLeft = false;
	

	/**
	 * Constructor.
	 * 
	 * @param comm PebbleCommunication link.
	 * @param route route object to use.
	 */
	public PebbleUiController(PebbleCommunication comm, Route route) {
		mLastSegmentIndex = -1;
		mLastWpIndex = 0;
		mPblCom = comm;
		mRoute = route;
	}
	
	/**
	 * Call this whenever anything needs to be updated in Pebble
	 */
	public void update() {
		if (mNoNonWalkingSegmentsLeft) {
			// There are no non-walking segments left, so do nothing
			System.out.println("DBG PebbleUiController update() called, no non-walking segments left");
			return;
		}
		
		int newSegmentIndex = mRoute.getCurrentIndex();
		int newWpIndex = mRoute.getCurrentSegment().getCurrentIndex();
		System.out.println("DBG PebbleUiController update() segment: " + newSegmentIndex + " waypoint: " + newWpIndex);
		Segment newSegment = mRoute.getSegmentList().get(newSegmentIndex);
		Waypoint newWaypoint = newSegment.getCurrentWaypoint();
		
		if (newSegmentIndex == mLastSegmentIndex) { //Still on the same segment
			// If we didn't jump over any stops, add the next wp to list. Else, update the whole list.
			if (newWaypoint == newSegment.getLastWaypoint()) {
				// If we are already on the last waypoint, initialize the new segment
				int newerSegmentIndex = getFirstNonWalkingSegmentIndex(newSegmentIndex + 1);
				if (newerSegmentIndex != -1) {
					initializeSegment(newerSegmentIndex);
				} else {
					mNoNonWalkingSegmentsLeft = true;
				}
			} else if (newWpIndex == mLastWpIndex + 1) {
				alarmIfNeeded();
				addWpToList();
			} else if (newWpIndex != mLastWpIndex) {
				// We've jumped over a wp, so update the whole list
				alarmIfNeeded();
				updateList(newSegment);
				mPblCom.switchTo3stopScreen();
			}
		} else if (newSegmentIndex > mLastSegmentIndex) {
			// Segment has changed
			if (newWpIndex < 2 || newSegment.isWalking()) {
				// We are on the first waypoint (or on a walking segment), so initialize the segment
				initializeSegment(newSegmentIndex);
			} else {
				/* Segment changed, but we've jumped over it's first waypoint,
				 * so don't use initializeSegment
				 */
				updateList(newSegment);
				mPblCom.switchTo3stopScreen();
			}
		}
		// Update the segment indices before returning. Don't return from this method before this except at the start!
		mLastSegmentIndex = newSegmentIndex;
		mLastWpIndex = newWpIndex;
	}
	
	/**
	 * Gets the index of the first non-walking segment in the route
	 * starting from index currentIndex. Returns -1 if not found or with -1
	 * as input.
	 */
	private int getFirstNonWalkingSegmentIndex(int currentIndex) {
		if (currentIndex == -1) {
			// Indicates that we have already reached the end
			return -1;
		}
		for (int i = currentIndex; i < mRoute.getSegmentList().size(); i++) {
			Segment seg = mRoute.getSegmentList().get(i);
			if (seg.isWalking() == false) {
				// When the first non-walking segment is found, initialize it on Pebble and stop the loop
				return i;
			}
		}
		return -1;
	}
	
	/**
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
	private void initializeSegment(int segmentIndex) {
		Segment currentSegment = null;
		// Find the first non-walking segment (don't care about walking segments)
		for (int i = segmentIndex; i < mRoute.getSegmentList().size(); i++) {
			Segment seg = mRoute.getSegmentList().get(i);
			if (seg.isWalking() == false) {
				// When the first non-walking segment is found, initialize it on Pebble and stop the loop
				currentSegment = seg;
				break;
			}
		}
		
		// If there are no non-walking segments left, don't do anything
		if (currentSegment == null) return;
		
		String segmentMode = currentSegment.getSegmentMode();
		
		// do things to get the seconds of minute, minute of hour and hour of day
		System.out.println("DBG segment time: " + currentSegment.getSegmentStartTime());
		Date segmentStart = timeStringToDate(currentSegment.getSegmentStartTime());
		
		Date currentDate = new Date(); // Current time
		Calendar currentDateCal = Calendar.getInstance();
		currentDateCal.setTime(currentDate);
		
		// Check if the route has already started
		if (segmentStart == null) System.out.println("DBG Date segmentStart was null");
		
		Calendar cal = Calendar.getInstance();
		if (cal == null) {
			System.out.println("DBG Calendar cal was null");
		}
		cal.setTime(segmentStart);
		
		long diffMs = cal.getTimeInMillis() - currentDateCal.getTimeInMillis();
		long diffHours = diffMs / 1000 / 3600;
		if (diffHours > 23) {
			// Don't send anything to pebble if the route starts after 24h because pebble app can't handle that
			System.out.println("DBG PebbleUiController segment start time was over 23h after current time");
			return;
		}
		
		
		int seconds = cal.get(Calendar.SECOND);
		int minutes = cal.get(Calendar.MINUTE);
		int hours = cal.get(Calendar.HOUR_OF_DAY);
		String stopName = currentSegment.getWaypoint(0).getWaypointName();
		String stopCode = currentSegment.getWaypoint(0).getWaypointStopCode();
		mPblCom.initializeSegment(segmentMode, stopName, stopCode, hours, minutes, seconds);
		
		// Then, send the stops
		updateList(currentSegment);
		// If the segment start time has already passed, switch to the 3stop screen
		if (segmentStart.before(currentDate)) {
			mPblCom.switchTo3stopScreen();
		}
	}
	
	/**
	 * Updates all stops shown in the 3stop list
	 */
	private void updateList(Segment segment) {
		int currentWpIndex = segment.getCurrentIndex();
		int lastWpIndex = segment.getWaypointList().size() - 1;
		
		ArrayList<Waypoint> waypoints = new ArrayList<Waypoint>();
		for (int i = currentWpIndex; i < lastWpIndex &&
				i < currentWpIndex + PebbleCommunication.NUM_STOPS-1; i++) {
			waypoints.add(segment.getWaypointList().get(i));
		}
		// if there are less than NUM_STOPS-1 waypoints to be sent, send additional null
		// waypoints to clear the list (not including the last waypoint yet)
		for (int i = waypoints.size(); i < PebbleCommunication.NUM_STOPS-1; i++) {
			waypoints.add(null);
		}
		// finally add the last waypoint to the list
		waypoints.add(segment.getLastWaypoint());
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
		mPblCom.switchTo3stopScreen();
	}

	/**
	 * Triggers a 'Get off' alarm on Pebble.
	 */
	private void alarmGetOff() {
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
