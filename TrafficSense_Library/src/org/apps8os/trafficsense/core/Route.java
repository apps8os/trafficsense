package org.apps8os.trafficsense.core;

import java.util.ArrayList;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;


/**
 * TODO: Documentation.
 */
public class Route {

	private String date;
	private String start;
	private String destination;
	private String arrivaltime;
	private String departure;
	private int currentSegment=0;
	private boolean coordsReady = false;
	
	
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

	public Segment setNextSegment(int nextSegment){
        if(currentSegment+1 < segmentList.size()){
                currentSegment=nextSegment;
                return(getCurrentSegment());
        }
        else return(null);
}
	
	//sets the current segment to the next segment
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
	
	public int getCurrentIndex() {
		return currentSegment;
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
		if(index > segmentList.size()-1){
	        return null;
	}
		return segmentList.get(index);
	}
	
	public void setRoute (JsonObject journey){
		setDate(journey.get("date").getAsString());
		setStart(journey.get("start").getAsString());
		setDestination(journey.get("dest").getAsString());
	    setArrivalTime(journey.get("arrivalTime").getAsString());
	    	
	    JsonArray segmentJs = journey.getAsJsonArray("segments");
	    for (int i = 0; i < segmentJs.size(); i++) {
			Segment segment = new Segment();
			segment.setSegment(segmentJs.get(i).getAsJsonObject());
			segmentList.add(i, segment);
		}
	    segmentList.trimToSize();
	    setDepartureTime(segmentJs.get(0).getAsJsonObject());
	}

	public String setDepartureTime (JsonObject firstSegment){
		departure = firstSegment.get("startTime").getAsString();
		departure = date + " " + departure;
		return departure;
	}
	
	public String getDepartureTime (){
		return departure;
	}
	
	public void setCoordsReady(boolean isReady) {
		coordsReady = isReady;
	}
	
	public boolean getCoordsReady() {
		return coordsReady;
	}

}
