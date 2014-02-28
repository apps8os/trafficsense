package org.apps8os.trafficsense.core;


import java.util.ArrayList;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class Segment {
		private String startTime;
		private String startPoint;
		private String mode;
		private int currentWaypoint;
		
		private ArrayList<Waypoint> waypointList = new ArrayList<Waypoint>();
		
		//returns the next waypoint
		public Waypoint  getNextWaypoint(){
			if(currentWaypoint+1 < waypointList.size()){
				return(waypointList.get(currentWaypoint+1));
			}
			else{
				return null;
			}
			
		}

		//sets the current waypoint to the next segment
		//and returns the next waypoint.
		//returns null if on last waypoint.
		public Waypoint setNextWaypoint(){
			Waypoint waypoint = getNextWaypoint();
			if(waypoint ==null){
				return waypoint;
			}
			else{
				currentWaypoint++;
				return waypoint;
			}

		}
		
		public Waypoint setNextWaypoint(int nextWaypoint){
			if(currentWaypoint+1 < waypointList.size()){
				currentWaypoint=nextWaypoint;
				return(getCurrentWaypoint());
			}
			else return(null);
		}
		
		//returns the last waypoint
		public Waypoint getLastWaypoint(){
			return waypointList.get(waypointList.size()-1);
		}
		
		public Waypoint getCurrentWaypoint(){
			return waypointList.get(currentWaypoint);
		}
		
		public int getCurrentIndex() {
			return currentWaypoint;
		}
		
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
		
		public Waypoint getWaypoint(int index) {
			if (index > waypointList.size() - 1) {
				return null;
			} else {
				return waypointList.get(index);
			}
		}
		
		public void setSegment(JsonObject segment){
			setSegmentStartTime(segment.get("startTime").getAsString());
			setSegmentStartPoint(segment.get("startPoint").getAsString());
			setSegmentMode(segment.get("mode").getAsString());
			
			JsonArray waypoints = segment.get("waypoints").getAsJsonArray();
			for (int i = 0; i < waypoints.size(); i++) {
				Waypoint waypoint = new Waypoint();
				waypoint.setWaypoint(waypoints.get(i).getAsJsonObject());
				waypointList.add(i, waypoint);
			}
			waypointList.trimToSize();
			
		}
		
		
}
