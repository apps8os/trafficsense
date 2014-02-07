package org.apps8os.trafficsense.first;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Route {
	private String date;
	private String start;
	private String destination;
	private String arrivaltime;
	private String departure;
	private int currentSegment=0;
	
	private ArrayList <Segment> segmentList = new ArrayList<Segment>();
	
	/**
	 * gets the next segment in the route
	 * returns next segment or a null if on last segment
	 */
	public Segment getNextSegment(){
		if(currentSegment+1 < segmentList.size()){
			return(segmentList.get(currentSegment+1));
		}
		else{
			return null;
		}
		
	}

	//sets the current segement to the next segment
	//and returns the next segment.
	//returns null if on last segment.
	public Segment setNextSegment(){
		Segment segment = getNextSegment();
		if(segment ==null){
			return segment;
		}
		else{
			currentSegment++;
			return segment;
		}

	}
	
	//gets the last segment on the route
	public Segment getLastSegment(){
		return segmentList.get(segmentList.size()-1);
	}
	
	public Segment getCurrentSegment(){
		return(segmentList.get(currentSegment));
	}
	
	public String getDate() {	
		return date;
	}

	public String setDate (String nDate){
		date=nDate;
		return date;
	}
	
	public String getStart(){
		return start;
	}
	
	public String setStart(String nStart){
		start = nStart;
		return start;
	}
	
	public String getDestination(){
		return destination;
	}
	
	public String setDestination(String nDestination){
		destination = nDestination;
		return destination;
	}
	
	public String getArrivalTime (){
		return arrivaltime;
	}
	
	public String setArrivalTime (String nArrival) {
		arrivaltime = nArrival;
		return arrivaltime;
	}
	
	public ArrayList <Segment> getSegmentList (){
		return segmentList;
	}
	
	public Segment getSegment(int index) {
		return segmentList.get(index);
	}
	
	
	public void FillRoute (JSONObject json){
		try {
			setDate(json.getString("date"));
			setStart(json.getString("start"));
			setDestination(json.getString("dest"));
	    	setArrivalTime(json.getString("arrivalTime"));
	    	
	    	JSONArray jsonArr = json.getJSONArray("segments");
	    	for (int i = 0; i < jsonArr.length(); i++) {
				Segment segment = new Segment();
				segment.FillSegment(json.getJSONArray("segments").getJSONObject(i));
				segmentList.add(i, segment);
			}
	    	segmentList.trimToSize();
	    	
	    	setDepartureTime(json);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	
	}

	
	public String setDepartureTime (JSONObject json){
		try {
			departure = json.getJSONArray("segments").getJSONObject(0).getString("startTime");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		departure = date + " " + departure;
		return departure;
	}
	
	public String getDepartureTime (){
		return departure;
	}
}