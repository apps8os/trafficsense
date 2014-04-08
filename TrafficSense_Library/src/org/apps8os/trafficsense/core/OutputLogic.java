package org.apps8os.trafficsense.core;


import org.apps8os.trafficsense.TrafficsenseContainer;


/**
 * Class implementing output logic.
 */
public class OutputLogic {
	/**
	 * Builds the message that UI banner should present to the user at a particular stop.
	 * 
	 * TODO: locale support.
	 * TODO: extract these strings into resource.
	 * 
	 * @return the message.
	 */
	public static String getJourneyProgressMessage() {
		TrafficsenseContainer mContainer = TrafficsenseContainer.getInstance();
		Route route = mContainer.getRoute();
		Segment curSegment = route.getCurrentSegment();

		String message = "Error: no message set in getJourneyProgressMessage";
		if (route.isJourneyEnded() == true) {
			// End of journey
			message = "Arrived at the final destination.";
		} else if (curSegment.isWalking() == true) {
			// Current segment is a walking one
			message = curSegment.getSegmentMode() + " until "
				+ curSegment.getLastWaypoint().getWaypointName() + ".";

			Segment nextSegment = null;
			try {
				nextSegment = route.getNextSegment();
			} catch (IndexOutOfBoundsException e) {
				// This is the last walking segment.
				message = "Walk to final destination.";
			}
			
			if (nextSegment != null && nextSegment.isWalking() == false) {
				message = message + " There catch "
					+ nextSegment.getSegmentMode()
					+ " ("
					+ nextSegment.getCurrentWaypoint().getWaypointStopCode()
					+ ") at "
					+ nextSegment.getSegmentStartTime();
			}
		} else if (curSegment.getCurrentIndex() == curSegment.getWaypointList().size() - 1) {
			/**
			 * Current segment is not a walking one and the next stop
			 * is the last stop of current segment
			 */
			message = curSegment.getCurrentWaypoint().getWaypointName()
					+ " is the last stop on "
					+ curSegment.getSegmentMode() + ".";
		} else {
			// General case
			switch (curSegment.getSegmentType()) {
			case RouteConstants.METRO:
				message = "Take metro";
				break;
			case RouteConstants.CONMUTER_TRAINS:
				message = "Take" + curSegment.getSegmentMode() + " train";
				break;
			default:
				message = "Take bus " + curSegment.getSegmentMode();
				break;
			}
			message += " to " + curSegment.getLastWaypoint().getWaypointName();
		}

		return message;
	}

	/**
	 * No instantiation.
	 */
	private OutputLogic() {}
}
