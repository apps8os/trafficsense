package org.apps8os.trafficsense.first;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

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
		//returns the last waypoint
		public Waypoint getLastWaypoint(){
			return waypointList.get(waypointList.size()-1);
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
