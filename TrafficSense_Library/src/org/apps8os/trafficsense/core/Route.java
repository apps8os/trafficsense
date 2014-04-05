package org.apps8os.trafficsense.core;

import java.util.ArrayList;

import org.apps8os.trafficsense.android.Constants;
import org.apps8os.trafficsense.util.TimeParser;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;


/**
 * Top level object holding a journey in internal structure.
 * Includes also progress tracking facility.
 */
public class Route {

	/**
	 * Date of the journey.
	 */
	private String mDate;
	/**
	 * Starting point.
	 */
	private String mStart;
	/**
	 * Destination.
	 */
	private String mDestination;
	/**
	 * Arrival time.
	 */
	private String mArrivalTime;
	/**
	 * Departure time including date.
	 */
	private String mDeparture;
	/**
	 * Index of the Segment in which we currently are.
	 */
	private int mCurrentSegment=0;
	/**
	 * The list of Segments of the journey.
	 */
	private ArrayList <Segment> mSegmentList;
	/**
	 * Have all the GPS coordinates been retrieved?
	 */
	private boolean coordsReady = false;
	/**
	 * Have we finished the journey?
	 */
	private boolean journeyEnded = false;
	
	/**
	 * Constructor
	 */
	public Route () {
		mSegmentList = new ArrayList<Segment>();
	}

	/**
	 * Get the next Segment in the journey.
	 * 
	 * @return the next Segment, null if already on the last Segment.
	 */
	public Segment getNextSegment() {
		if (mCurrentSegment+1 >= mSegmentList.size()) {
			return null;
		}
		return mSegmentList.get(mCurrentSegment+1);
	}

	/**
	 * Set current segment to the given index.
	 * 
	 * @param nextSegment index to the new current segment.
	 * @return new current Segment.
	 * @throws IndexOutOfBoundsException
	 */
	public Segment setNextSegment(int nextSegment) throws IndexOutOfBoundsException {
        if(nextSegment < mSegmentList.size()) {
                mCurrentSegment = nextSegment;
                return getCurrentSegment();
        }
        throw new IndexOutOfBoundsException("size:"+mSegmentList.size()+" next:"+nextSegment);
	}

	/**
	 * Sets the current segment to the next.
	 * 
	 * @return the next Segment, null if already on the last segment.
	 */
	public Segment setNextSegment(){
		Segment segment = getNextSegment();
		if(segment ==null) {
			return null;
		}
		else{
			mCurrentSegment++;
			return segment;
		}
	}
	
	/**
	 * Mark or clear the ended state of this journey.
	 * @param state state
	 */
	public void setJourneyEnded(boolean state){
		journeyEnded = state;
	}
	
	/**
	 * Check if the journey is ended.
	 * 
	 * @return is the journey ended.
	 */
	public boolean isJourneyEnded(){
		return journeyEnded;
	}
	
	/**
	 * Get the last Segment of this journey.
	 * 
	 * @return the last Segment.
	 */
	public Segment getLastSegment() {
		return mSegmentList.get(mSegmentList.size()-1);
	}
	
	/**
	 * Get the Segment in which we currently are.
	 * 
	 * @return the Segment.
	 */
	public Segment getCurrentSegment(){
		return mSegmentList.get(mCurrentSegment);
	}
	
	/**
	 * Get the index of the segment in which we currently are.
	 * 
	 * @return the index.
	 */
	public int getCurrentIndex() {
		return mCurrentSegment;
	}
	
	/**
	 * Get the date of this journey.
	 * 
	 * @return date string.
	 */
	public String getDate() {	
		return mDate;
	}

	/**
	 * Set the date of this journey.
	 * 
	 * @param newDate date string.
	 * @return date string.
	 */
	public String setDate (String newDate){
		mDate=newDate;
		return mDate;
	}
	
	/**
	 * Get starting point string.
	 * 
	 * @return starting point.
	 */
	public String getStart(){
		return mStart;
	}
	
	/**
	 * Set starting point string.
	 * 
	 * @param newStart starting point
	 * @return starting point
	 */
	public String setStart(String newStart){
		mStart = newStart;
		return mStart;
	}
	
	/**
	 * Get destination string.
	 * 
	 * @return destination string.
	 */
	public String getDestination() {
		return mDestination;
	}
	
	/**
	 * Set destination string.
	 * 
	 * @param newDestination destination.
	 * @return destination.
	 */
	public String setDestination(String newDestination){
		mDestination = newDestination;
		return mDestination;
	}
	
	/**
	 * Get arrival time.
	 * @return arrival time.
	 */
	public String getArrivalTime (){
		return mArrivalTime;
	}
	
	/**
	 * Set arrival time.
	 * 
	 * @param newArrival new arrival time.
	 * @return arrival time.
	 */
	public String setArrivalTime (String newArrival) {
		mArrivalTime = newArrival;
		return mArrivalTime;
	}
	
	/**
	 * Get the list of Segments in th journey.
	 * 
	 * @return list of Segments.
	 */
	public ArrayList <Segment> getSegmentList () {
		return mSegmentList;
	}
	
	/**
	 * Get the Segment of given index.
	 * 
	 * @param index index of the requested Segment.
	 * @return the Segment.
	 */
	public Segment getSegment(int index) {
		return mSegmentList.get(index);
	}
	
	/**
	 * Populate the route with given JSON object.
	 * 
	 * @param journey journey in Gson JSON.
	 */
	public void setRoute (JsonObject journey) {
		setDate(journey.get("date").getAsString());
		setStart(journey.get("start").getAsString());
		setDestination(journey.get("dest").getAsString());
	    setArrivalTime(journey.get("arrivalTime").getAsString());
	    	
	    JsonArray segmentJs = journey.getAsJsonArray("segments");
	    for (int i = 0; i < segmentJs.size(); i++) {
			Segment segment = new Segment();
			segment.setSegment(segmentJs.get(i).getAsJsonObject());
			mSegmentList.add(i, segment);
		}
	    mSegmentList.trimToSize();
	    setDepartureTime(segmentJs.get(0).getAsJsonObject());
	}

	/**
	 * Set the departure time to that of the given Segment.
	 * 
	 * Should call {@link #setDate(String)} first.
	 * 
	 * @param segment the intended first segment.
	 * @return departure time.
	 */
	public String setDepartureTime (JsonObject segment) {
		mDeparture = mDate + " " + segment.get("startTime").getAsString();
		return mDeparture;
	}
	
	/**
	 * Get the departure time.
	 * 
	 * @return departure time.
	 */
	public String getDepartureTime (){
		return mDeparture;
	}
	
	/**
	 * Mark the status of GPS coordinates.
	 * 
	 * @param isReady are they ready?
	 */
	public void setCoordsReady(boolean isReady) {
		coordsReady = isReady;
	}
	
	/**
	 * Get the status of GPS coordinates.
	 * 
	 * @return status of GPS coordinates.
	 */
	public boolean getCoordsReady() {
		return coordsReady;
	}
	
	/**
	 * Check if this journey is in the past if Constants.useWallClock is true.
	 * 
	 * @return true if it is it in the past. Always false if  Constants.useWallClock is false.
	 */
	public boolean isJourneyInThePast() {
		if (Constants.useWallClock == false) {
			return false;
		}
		long journeyStartTime = TimeParser.strDateTimeToDate(mDeparture).getTime();
		long now = System.currentTimeMillis();
		//System.out.println("DBG isInPast: now:"+now+" start:"+journeyStartTime);
		if (journeyStartTime >= now) {
			return false;
		}
		return true;
	}
	
	/**
	 * Get the starting time of the first waypoint in the journey.
	 * 
	 * @return the time in miliseconds since epoch, or TEST_TIME after current time if Constants.useWallClock is false.
	 */
	public long getFirstWaypointTime() {
		if (Constants.useWallClock == false) {
			return Constants.TEST_TIME + System.currentTimeMillis();
		}
		// Get the time of the first waypoint
		return TimeParser.strDateTimeToDate(
				mDate + " " +
				mSegmentList.get(0).getWaypointList().get(0).getWaypointTime())
				.getTime();
	}

}
