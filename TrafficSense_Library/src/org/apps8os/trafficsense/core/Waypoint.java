package org.apps8os.trafficsense.core; 

import org.json.JSONArray;
import org.json.JSONException;

import com.google.gson.JsonObject;

public class Waypoint{
	
	private String time;
	private String name;
	private String stopCode;
	private String longCord;
	private String latCord;
	
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
			setWaypointCords(stopCode);
		} 
			else { 
				// TODO: what should this be if there is no stopCode ???
				setWaypointStopCode("0000");
		}
	}

	public void setWaypointCords(String stopCode){
		RequestThread r= new RequestThread ();
		String returned = r.getStopInfo("000000001", stopCode);
		//System.out.println(returned);
		String coords ="";
		try {
			JSONArray json = new JSONArray(returned);
			coords = json.getJSONObject(0).get("wgs_coords").toString();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		/*
		System.out.println(coords);
		System.out.println(coords.substring(0, 8));
		System.out.println(coords.substring(9, 17));*/
		longCord = coords.substring(9, 17);
		latCord = coords.substring(0, 8);
	}
	
	public String getLongitude(){
		return longCord;
	}
	
	public String getLatitude(){
		return latCord;
	}
}
