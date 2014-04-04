package org.apps8os.trafficsense.core;


import java.util.ArrayList;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;


/**
 * TODO: Documentation
 */
public class Segment {
	// Time format: EEEE dd.M.yyyy kk:mm (used in both TimeOnlyService and PebbleUiController
	// TODO: save times as Date objects or something instead of strings
	private String startTime;
	private String startPoint;
	private String mode;
	/**
	 * Current progress in this segment.
	 * Index to the stop where we are right now.
	 */
	private int currentWaypoint;
	/**
	 * Is this a walking segment.
	 * @see #setSegmentType()
	 */
	private boolean isWalking = false;
	/**
	 * Type of transportation.
	 * It is an internal integral version of {@link #mode}.
	 */
	private volatile int transportType;
	private ArrayList<Waypoint> waypointList;

	/**
	 * Constructor.
	 */
	public Segment () {
		currentWaypoint = 0;
		waypointList = new ArrayList<Waypoint>();
	}
	
	/**
	 * Return the next Waypoint according to our progress.
	 * @return next Waypoint. null if we are at the end already.
	 */
	public Waypoint getNextWaypoint() {
		if (currentWaypoint + 1 < waypointList.size()) {
			return (waypointList.get(currentWaypoint + 1));
		} else {
			return null;
		}

	}

	/**
	 * Advance to the next Waypoint.
	 *  
	 * @return the next Waypoint. null if we are at the end already.
	 */
	public Waypoint setNextWaypoint() {
		Waypoint waypoint = getNextWaypoint();
		if(waypoint != null) {
			currentWaypoint++;
		}
		return waypoint;
	}

	/**
	 * Advance to the specified-th Waypoint.
	 * 
	 * @param index index of the Waypoint to become current.
	 * @return current Waypoint. null if at the end or out of range.
	 */
	public Waypoint setNextWaypoint(int index) {
		if (currentWaypoint + 1 < waypointList.size()
				&& index < waypointList.size()) {
			currentWaypoint = index;
			return getCurrentWaypoint();
		} else {
			return null;
		}
	}

	/**
	 * Return the last Waypoint.
	 * 
	 * @return the last Waypoint in this Segment. null if empty segment.
	 */
	public Waypoint getLastWaypoint(){
		if (waypointList.size() == 0) {
			return null;
		}
		return waypointList.get(waypointList.size()-1);
	}


	/**
	 * Return the current Waypoint.
	 * 
	 * @return current Waypoint. null if empty segment or error.
	 */
	public Waypoint getCurrentWaypoint() {
		if (waypointList.size() == 0) {
			return null;
		}
		return waypointList.get(currentWaypoint);
	}

	public int getCurrentIndex() {
		return currentWaypoint;
	}

	public String getSegmentStartTime() {
		return startTime;
	}

	public String setSegmentStartTime(String nstartTime) {
		startTime = nstartTime;
		return startTime;
	}

	public String getSegmentStartPoint() {
		return startPoint;
	}

	public String setSegmentStartPoint(String nstartPoint) {
		startPoint = nstartPoint;
		return startPoint;
	}

	public String getSegmentMode() {
		synchronized (this) {
			return mode;
		}
	}

	public String setSegmentMode(String nMode) {
		synchronized (this) {
			mode = nMode;
			return mode;
		}
	}

	public ArrayList<Waypoint> getWaypointList() {
		return waypointList;
	}

	public boolean isWalking() {
		return isWalking;
	}

	public Waypoint getWaypoint(int index) {
		if (index > waypointList.size() - 1) {
			return null;
		} else {
			return waypointList.get(index);
		}
	}

	public void setSegment(JsonObject segment) {
		setSegmentStartTime(segment.get("startTime").getAsString());
		setSegmentStartPoint(segment.get("startPoint").getAsString());
		setSegmentMode(segment.get("mode").getAsString());
		/**
		 * TODO: Finnish/Swedish support!!
		 */
		synchronized (this) {
			if (mode.equals("metro")) {
				transportType = RouteConstants.METRO;
			} else	if (mode.equals("ferry")) {
				transportType = RouteConstants.FERRY;
			} else	if(mode.startsWith("Walking") || mode.startsWith("Kävelyä") || mode.startsWith("Gång")) {
				isWalking = true;
				transportType = RouteConstants.WALKING;
			} else {
				/**
				 * Other types are resolved later by JourneyInfoResolver.
				 */
				transportType = RouteConstants.UNKNOWN;
			}
		}

		JsonArray waypoints = segment.get("waypoints").getAsJsonArray();
		for (int i = 0; i < waypoints.size(); i++) {
			Waypoint waypoint = new Waypoint();
			waypoint.setWaypoint(waypoints.get(i).getAsJsonObject());
			waypointList.add(i, waypoint);
		}
		waypointList.trimToSize();
	}

	public void setSegmentType(int type) {
		synchronized (this) {
			transportType = type;
		}
	}

	public int getSegmentType() {
		synchronized (this) {
			return transportType;
		}
	}


}
