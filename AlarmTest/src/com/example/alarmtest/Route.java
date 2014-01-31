package com.example.alarmtest;

import java.util.ArrayList;

public class Route {
	private String date;
	private String start;
	private String destination;
	private String arrivaltime;
	
	private ArrayList <Segment> segmentList = new ArrayList<Segment>();
	
	


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
	
}