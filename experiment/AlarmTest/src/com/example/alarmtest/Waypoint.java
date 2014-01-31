package com.example.alarmtest;

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
	
	public String setWaypointAtopCode (String nStopCode){
		stopCode = nStopCode;
		return stopCode;
	}
}
