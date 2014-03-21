package org.apps8os.trafficsense.core;

import java.util.List;

import org.apps8os.trafficsense.TrafficsenseContainer;


/**
 * Class for implementing any output logic. For example what strings the mainactivity should present
 * to the user at a particular stop. 
 * @author traffisense
 *
 */
public class OutputLogic {
	
	
	public static String getOutput(){
		
		TrafficsenseContainer mContainer= TrafficsenseContainer.getInstance();
		Segment curSegment = mContainer.getRoute().getCurrentSegment();
		int curSegmentIndex = mContainer.getRoute().getCurrentIndex();
		int curWaypointIndex = mContainer.getRoute().getCurrentSegment().getCurrentIndex();
		List<Waypoint> waypointList = mContainer.getRoute().getCurrentSegment().getWaypointList();
		
		if(mContainer.getRoute().isJourneyEnded()==true){
			String message = "Congratulations. You reached your destination";
			return(message);
		}
		
		//if the next stop is the last stop on a segment
		if(curWaypointIndex == waypointList.size()-1){
			String message = String.format("Next stop is %s. Get off here", waypointList.get(curWaypointIndex).getWaypointName());
			return message;
		}
		
		//if the current segment is a walking one
		if(curSegment.isWalking() == true){
			String message = "Walk to next stop"; 
			return message;
		}
		
		
		if(curSegment.isWalking() == false){
			String transportId = curSegment.getSegmentMode();
			String destination = curSegment.getLastWaypoint().getWaypointName();
			String message = new String();
			if(transportId.equals("metro")){
				message = String.format("Take metro to %s",destination);
			}
			else if(transportId.length() == 1){
				message = String.format("Take %s train to %s.", transportId, destination);
			}
			else{
				message= String.format("Take bus %s to %s.",transportId, destination);
			}
			return message;
		}		
		
		return("");
	}
	
	
}
