package com.aalto.hslapitest; 

import com.google.gson.JsonObject;

public class Waypoint {
	
	private String time;
	private String name;
	private String stopCode;
	private String xCord;
	private String yCord;
	
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
		Request req = new Request();
		if (waypoint.has("stopCode")) {
			setWaypointStopCode(waypoint.get("stopCode").getAsString());		
			req.getStopInfo("", stopCode);		
		} 
			else { 
				// TODO: what should this be if there is no stopCode ???
				setWaypointStopCode("0000");
				req.getGeocoding("00000010", "", name, "street");
		}
		
	}
}