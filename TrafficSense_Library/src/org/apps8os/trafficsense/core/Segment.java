package org.apps8os.trafficsense.core;


import java.util.ArrayList;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;


/**
 * Class for segments in journey.
 */
public class Segment {
	/**
	 * Start time (without date) of this segment.
	 */
	private String startTime;
	/**
	 * Starting point of this segment.
	 */
	private String startPoint;
	/**
	 * The mode of ths segment.
	 */
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
	/**
	 * List of waypoints in this segment.
	 */
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

	/**
	 * Get the index of the waypoint on which we currently are.
	 * 
	 * @return the index of the waypoint on which we currently are.
	 */
	public int getCurrentIndex() {
		return currentWaypoint;
	}

	/**
	 * Get the start time (without date) of this segment.
	 * 
	 * @return the start time (without date) of this segment.
	 */
	public String getSegmentStartTime() {
		return startTime;
	}

	/**
	 * Set the start time (without date) of this segment.
	 * 
	 * @param newStartTime the start time (without date) of this segment.
	 * @return the start time (without date) of this segment.
	 */
	public String setSegmentStartTime(String newStartTime) {
		startTime = newStartTime;
		return startTime;
	}

	/**
	 * Get (the name of) the starting point of this segment.
	 * 
	 * @return (the name of) the starting point of this segment.
	 */
	public String getSegmentStartPoint() {
		return startPoint;
	}

	/**
	 * Set the starting point of this segment.
	 * 
	 * @param newStartPoint (the name of) the starting point of this segment.
	 * @return (the name of) the starting point of this segment.
	 */
	public String setSegmentStartPoint(String newStartPoint) {
		startPoint = newStartPoint;
		return startPoint;
	}

	/**
	 * Get the travel mode of this segment. 
	 * 
	 * @return the travel mode of this segment.
	 */
	public String getSegmentMode() {
		synchronized (this) {
			return mode;
		}
	}

	/**
	 * Set the travel mode of this segment.
	 * 
	 * @param newMode the travel mode of this segment.
	 * @return the travel mode of this segment.
	 */
	public String setSegmentMode(String newMode) {
		synchronized (this) {
			mode = newMode;
			return mode;
		}
	}

	/**
	 * Get the list of waypoints in this segment.
	 * 
	 * @return the list of waypoints.
	 */
	public ArrayList<Waypoint> getWaypointList() {
		return waypointList;
	}

	/**
	 * Check if this is a walking segment.
	 * 
	 * @return true if it is.
	 */
	public boolean isWalking() {
		return isWalking;
	}

	/**
	 * Get the waypoint at the given index.
	 * 
	 * NOTE: throws an exception if index is out of bound.
	 * 
	 * @param index index of the waypoint.
	 * @return the waypoint.
	 */
	public Waypoint getWaypoint(int index) {
		if (index > waypointList.size() - 1) {
			return null;
		} else {
			return waypointList.get(index);
		}
	}

	/**
	 * Populate this segment with the given Gson JSON object.
	 * 
	 * @param segment JSON object of a segment.
	 */
	public void setSegment(JsonObject segment) {
		setSegmentStartTime(segment.get("startTime").getAsString());
		setSegmentStartPoint(segment.get("startPoint").getAsString());
		setSegmentMode(segment.get("mode").getAsString());
		synchronized (this) {
			// metro/ferry is the same for English, Swedisn, and Finnish
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

	/**
	 * Set the type of transportation of this segment.
	 * It is an internal integral version of mode.
	 * 
	 * @param type the type of transportation of this segment.
	 */
	public void setSegmentType(int type) {
		synchronized (this) {
			transportType = type;
		}
	}

	/**
	 * Get the type of transportation of this segment.
	 * 
	 * @return the type of transportation of this segment.
	 */
	public int getSegmentType() {
		synchronized (this) {
			return transportType;
		}
	}

}
