package org.apps8os.trafficsense.core;


import com.google.gson.JsonObject;

public class Waypoint {
	
	private String time;
	private String name;
	private String stopCode;
	
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
	
	public void setWaypoint (JsonObject waypoint){
		setWaypointTime (waypoint.get("time").getAsString());
		setWaypointName (waypoint.get("name").getAsString());
		if (waypoint.has("stopCode")) {
			setWaypointStopCode(waypoint.get("stopCode").getAsString());		
		} 
		else { 
				// TODO: what should this be if there is no stopCode ???
				setWaypointStopCode("0000");
		}
		
	}
}
