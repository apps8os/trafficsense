package org.apps8os.trafficsense.core; 

import org.apps8os.trafficsense.android.Constants;

import com.google.gson.JsonObject;

/**
 * TODO: documentation
 */
public class Waypoint{
	
	private String time;
	private String name;
	private String stopCode;
	/**
	 * Coordinate system: WGS84.
	 */
	private volatile double mLatitude;
	/**
	 * Coordinate system: WGS84.
	 */
	private volatile double mLongitude;
	
	public String getWaypointTime(){
		return time;
	}
	
	public String setWaypointTime(String nTime){
		time=nTime;
		return time;
	}
	
	public String getWaypointName(){
		return name;
	}
	
	public String setWaypointName(String nName){
		name= nName;
		return name;
	}
	
	public String getWaypointStopCode (){
		return stopCode;
	}
	
	public String setWaypointStopCode (String nStopCode){
		stopCode = nStopCode;
		return stopCode;
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
			setWaypointStopCode(waypoint.get("stopCode").getAsString());
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
	public void setCoordinate(double latitude, double longitude) {
		synchronized (this) {
			mLatitude = latitude;
			mLongitude = longitude;
		}
	}

	/**
	 * Return the longitude of the GPS coordinate.
	 * @return longitude.
	 */
	public double getLongitude() {
		synchronized (this) {
			return mLongitude;
		}
	}
	
	/**
	 * Return the latitude of the GPS coordinate.
	 * @return latitude.
	 */
	public double getLatitude() {
		synchronized (this) {
			return mLatitude;
		}
	}
}
