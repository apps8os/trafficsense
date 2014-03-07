package org.apps8os.trafficsense.android;

public class Constants {

	private Constants() {}
	
	final public static String ACTION_NEXT_WAYPOINT = "trafficsense.NextWaypointAlarm";
	final public static String ACTION_GET_OFF = "trafficsense.GetOffAlarm";
	final public static String ACTION_ROUTE_EVENT = "trafficsense.RouteEventUpdateUi";
	final public static String ACTION_ROUTE_EVENT_EXTRA_MESSAGE = "trafficsense.RouteEventUpdateUi.Extras.Message";
	final public static long TEST_TIME = 5000;
	final public static int SERVICE_TIME_ONLY = 1;
	final public static int SERVICE_LOCATION_ONLY = 2;
	/**
	 * The UUID of our Pebble smart watch application.
	 * @see appinfo.json
	 */
	final public static String PEBBLE_APP_UUID = "83eef382-21a4-473a-a189-ceffa42f86b1";
}
