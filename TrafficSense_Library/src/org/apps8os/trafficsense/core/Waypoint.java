package org.apps8os.trafficsense.core; 

import org.apps8os.trafficsense.android.Constants;

import com.google.gson.JsonObject;

/**
 * Class for waypoints in a journey.
 */
public class Waypoint{
	/**
	 * The time at which we will arrive at this waypoint.
	 */
	private String time;
	/**
	 * The (stop) name of this waypoint.
	 */
	private String name;
	/**
	 * HSL stop code (if available).
	 */
	private String stopCode;
	/**
	 * GPS coordinate latitude.
	 * Coordinate system: WGS84.
	 */
	private volatile double mLatitude;
	/**
	 * GPS coordinate longitude..
	 * Coordinate system: WGS84.
	 */
	private volatile double mLongitude;
	
	/**
	 * Constructor.
	 */
	public Waypoint () {
		stopCode = Constants.NO_STOP_CODE;
		mLatitude = Constants.NO_COORD;
		mLongitude = Constants.NO_COORD;
	}
	
	/**
	 * Get the time at which we will arrive here.
	 * 
	 * @return the time at which we will arrive here.
	 */
	public String getWaypointTime(){
		return time;
	}
	
	/**
	 * Set the time at which we will arrive here.
	 * @param newTime the time at which we will arrive here.
	 * @return the time at which we will arrive here.
	 */
	public String setWaypointTime(String newTime){
		time = newTime;
		return time;
	}
	
	/**
	 * Get the name of this waypoint (stop).
	 * @return the name of this waypoint (stop).
	 */
	public String getWaypointName(){
		return name;
	}
	
	/**
	 * Set the name of this waypoint (stop).
	 * @param newName the name of this waypoint (stop).
	 * @return the name of this waypoint (stop).
	 */
	public String setWaypointName(String newName){
		name= newName;
		return name;
	}
	
	/**
	 * Get the HSL stop code of this waypoint.
	 * 
	 * NOTE: Must check if it is Constants.NO_STOP_CODE .
	 * 
	 * @return the name of this waypoint (stop).
	 */
	public String getWaypointStopCode () {
		return stopCode;
	}
	
	/**
	 * Set the HSL stop code of this waypoint.
	 * 
	 * @param newStopCode the HSL stop code of this waypoint.
	 * @return the HSL stop code of this waypoint.
	 */
	public String setWaypointStopCode (String newStopCode) {
		if (newStopCode.isEmpty()) {
			stopCode = Constants.NO_STOP_CODE;
		} else {
			stopCode = newStopCode;
		}
		return stopCode;
	}
	
	/**
	 * Check if there is an HSL stopCode for this waypoint.
	 * 
	 * @return true if there is.
	 */
	public boolean hasStopCode() {
		if (stopCode.equals(Constants.NO_STOP_CODE)) {
			return false;
		}
		return true;
	}
	
	/**
	 * Fill-in data fields of this waypoint except GPS coordinates.
	 * 
	 * @param waypoint JsonObject of this waypoint.
	 */
	public void setWaypoint (JsonObject waypoint){
		setWaypointTime(waypoint.get("time").getAsString());
		setWaypointName(waypoint.get("name").getAsString());
		if (waypoint.has("stopCode")) {
			String stopCode = waypoint.get("stopCode").getAsString().trim();
			// Handle empty stopCode case ( "08:00  name ()" in journey text).
			if (stopCode.isEmpty()) {
				stopCode = Constants.NO_STOP_CODE;
			}
			setWaypointStopCode(stopCode);
		} else {
			setWaypointStopCode(Constants.NO_STOP_CODE);
		}
	}

	/**
	 * Set the GPS coordinate.
	 * 
	 * This is likely to be called from a worker thread, so all access
	 * to GPS coordinate attributes shall be synchronized.
	 * 
	 * @param latitude latitude.
	 * @param longitude longitude.
	 */
	public synchronized void setCoordinate(double latitude, double longitude) {
		mLatitude = latitude;
		mLongitude = longitude;
	}

	/**
	 * Return the longitude of the GPS coordinate.
	 * @return longitude.
	 */
	public synchronized double getLongitude() {
		return mLongitude;
	}
	
	/**
	 * Return the latitude of the GPS coordinate.
	 * @return latitude.
	 */
	public synchronized double getLatitude() {
		return mLatitude;
	}
	
	/**
	 * Check if this Waypoint has valid GPS coordinates.
	 * 
	 * @return true if it has valid GPS coordinates.
	 */
	public boolean hasCoord() {
		if (mLatitude == Constants.NO_COORD || mLongitude == Constants.NO_COORD) {
			return false;
		}
		return true;
	}
}
