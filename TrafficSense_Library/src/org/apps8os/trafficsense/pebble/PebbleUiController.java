package org.apps8os.trafficsense.pebble;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import org.apps8os.trafficsense.core.Route;
import org.apps8os.trafficsense.core.Segment;
import org.apps8os.trafficsense.core.Waypoint;
import org.apps8os.trafficsense.util.TimeParser;

/**
 * Class for Pebble user interface controller.
 */
public class PebbleUiController {
	/* -- UNUSED
	private static final int WINDOW_BASIC = 0;
	private static final int WINDOW_3STOP = 1;
	*/
	
	/**
	 * Pebble communication handler.
	 */
	private PebbleCommunication mPblCom;
	/**
	 * The route we are tracking.
	 */
	private Route mRoute;
	/**
	 * Index to the last Segment sent to Pebble.
	 */
	private int mLastSegmentIndex;
	/**
	 * Index to the last Waypoint sent to Pebble.
	 */
	private int mLastWpIndex;
	/**
	 * Is there any non-walking Segment left?
	 */
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
		
		if (newSegmentIndex == mLastSegmentIndex) {
			// We are still on the same segment
			/**
			 * If we did not jump over any stops, add the next waypoint to list.
			 * Otherwise, update the whole list.
			 */
			if (newWaypoint == newSegment.getLastWaypoint()) {
				// If we are already on the last waypoint, initialize the next segment.
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
				// We jumped over some waypoint(s), update the whole list.
				alarmIfNeeded();
				updateList(newSegment);
				mPblCom.switchTo3stopScreen();
			}
		} else if (newSegmentIndex > mLastSegmentIndex) {
			// Segment has changed
			if (newWpIndex < 2 || newSegment.isWalking()) {
				/**
				 * We are on the first waypoint (or on a walking segment),
				 * so initialize the segment.
				 */
				initializeSegment(newSegmentIndex);
			} else {
				/**
				 * Segment changed, but we jumped over its first waypoint,
				 * so do not use initializeSegment.
				 */
				updateList(newSegment);
				mPblCom.switchTo3stopScreen();
			}
		}
		/**
		 * Update the segment indices before returning.
		 * Do not return from this method before this except at the start!
		 */
		mLastSegmentIndex = newSegmentIndex;
		mLastWpIndex = newWpIndex;
	}
	
	/**
	 * Updates everything in Pebble
	 */
	public void totalUpdate() {
		int newSegmentIndex = mRoute.getCurrentIndex();
		//int newWpIndex = mRoute.getCurrentSegment().getCurrentIndex();
		int nonWalkingSeg = getFirstNonWalkingSegmentIndex(newSegmentIndex);
		initializeSegment(nonWalkingSeg);
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
			Segment seg = mRoute.getSegment(i);
			if (seg.isWalking() == false) {
				/**
				 * When the first non-walking segment is found,
				 * initialize it on Pebble and stop the loop.
				 */
				return i;
			}
		}
		return -1;
	}
	
	/**
	 * Send an alarm if we are on the second last stop
	 */
	private void alarmIfNeeded() {
		//int newSegmentIndex = mRoute.getCurrentIndex();
		int newWpIndex = mRoute.getCurrentSegment().getCurrentIndex();
		Segment newSegment = mRoute.getCurrentSegment();
		if (newWpIndex == newSegment.getWaypointList().size() - 2) {
			/**
			 * If we are at the second last waypoint, send the alarm.
			 * TODO: start a timer and do the alarm a little later to avoid confusion for the user
			 */
			alarmGetOff();
		}
	}
	
	/**
	 * Initializes segment-related values on Pebble.
	 * This includes the stops that need to be shown initially.
	 * Also shows the screen that tells how many minutes until the vehicles comes.
	 * Should be only used when we are at the first waypoint of a non-walking segment.
	 */
	private void initializeSegment(int segmentIndex) {
		/**
		 * Find the first non-walking segment.
		 * We do not care about leading walking segments.
		 */
		Segment currentSegment = null;
		for (int i = segmentIndex; i < mRoute.getSegmentList().size(); i++) {
			Segment seg = mRoute.getSegmentList().get(i);
			if (seg.isWalking() == false) {
				/**
				 * When the first non-walking segment is found,
				 * initialize it on Pebble and stop the loop.
				 */
				currentSegment = seg;
				break;
			}
		}
		if (currentSegment == null) {
			// Nothing to do if there is no non-walking segment left.
			return;
		}
		
		String segmentMode = currentSegment.getSegmentMode();
		System.out.println("DBG initializeSegment time: " + currentSegment.getSegmentStartTime());
		Date segmentStart = TimeParser.strTimeToDate(currentSegment.getSegmentStartTime());
		if (segmentStart == null) {
			System.out.println("DBG initializeSegment unable to get segment start time");
		}

		Calendar cal = Calendar.getInstance();
		cal.setTime(segmentStart);
		Date currentDate = new Date(); // Current time
		Calendar currentDateCal = Calendar.getInstance();
		currentDateCal.setTime(currentDate);
		long diffMs = cal.getTimeInMillis() - currentDateCal.getTimeInMillis();
		long diffHours = diffMs / 1000 / 3600;
		if (diffHours > 23) {
			/**
			 * Send nothing to Pebble if the route starts after 23h because
			 * our Pebble application is unable to handle that. (TODO)
			 */
			System.out.println("DBG PebbleUiController segment starts more than 23h later");
			return;
		}
		
		// Get the seconds of minute, minute of hour and hour of day.
		int seconds = cal.get(Calendar.SECOND);
		int minutes = cal.get(Calendar.MINUTE);
		int hours = cal.get(Calendar.HOUR_OF_DAY);
		String stopName = currentSegment.getWaypoint(0).getWaypointName();
		String stopCode = currentSegment.getWaypoint(0).getWaypointStopCode();
		mPblCom.initializeSegment(segmentMode, stopName, stopCode, hours, minutes, seconds);
		// Send the stops
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
		/**
		 * If there are less than NUM_STOPS-1 waypoints to be sent,
		 * send additional null waypoints to clear the list
		 * (except the last waypoint).
		 */
		for (int i = waypoints.size(); i < PebbleCommunication.NUM_STOPS-1; i++) {
			waypoints.add(null);
		}
		// Finally add the last waypoint to the list
		waypoints.add(segment.getLastWaypoint());
		for (int i = 0; i < PebbleCommunication.NUM_STOPS; i++) {
			mPblCom.sendWaypoint(waypoints.get(i), i);
		}
	}

	/**
	 * Scrolls PebbleUI one stop forward
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
}
