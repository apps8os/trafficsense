package com.example.alarmtest;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

public class Segment {
		private String startTime;
		private String startPoint;
		private String mode;
		
		
		private ArrayList<Waypoint> waypointList = new ArrayList<Waypoint>();
		
		public String getSegmentStartTime (){
			return startTime;
		}
		
		public String setSegmentStartTime (String nstartTime){
			startTime = nstartTime;
			return startTime;
		}
	
		public String getSegmentStartPoint(){
			return startPoint;
		}
		
		public String setSegmentStartPoint(String nstartPoint){
			startPoint = nstartPoint;
			return startPoint;	
		}
		
		public String getSegmentMode (){
			return mode;
		}
		
		public String setSegmentMode(String nMode){
			mode = nMode;
			return mode;
		}
		
		public ArrayList <Waypoint> getWaypointList (){
			return waypointList;
		}		
		
		public void FillSegment(JSONObject json){
			setSegmentStartTime(json.optString("startTime"));
			setSegmentStartPoint(json.optString("startPoint"));
			setSegmentMode(json.optString("mode"));
			
			JSONArray jsonArr = json.optJSONArray("waypoints");
			for (int i = 0; i < jsonArr.length(); i++) {
				Waypoint waypoint = new Waypoint();
				waypoint.FillWaypoint(json.optJSONArray("waypoints").optJSONObject(i));
				waypointList.add(i, waypoint);
			}
			waypointList.trimToSize();
			
		}
		
		
}
