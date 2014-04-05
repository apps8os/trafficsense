package org.apps8os.trafficsense.android;

/**
 * Shared constants.
 */
public class Constants {

	/**
	 * Prevent instantiation of this class.
	 */
	private Constants() {}
	
	/**
	 * Debug/Development mode indicator.
	 */
	final public static boolean isDevelopmentMode = true;
	/**
	 * Whether time-based service(s) should use wall clock or toy time.
	 * Affects also Route.isJourneyInThePast().
	 */
	final public static boolean useWallClock = false;
	/**
	 * Radius of GeoFence regions (meters?).
	 */
	final public static int GEOFENCE_RADIUS = 100;
	/**
	 * Action strings of Intents.
	 */
	final public static String ACTION_NEXT_WAYPOINT = "trafficsense.NextWaypointAlarm";
	final public static String ACTION_GET_OFF = "trafficsense.GetOffAlarm";
	final public static String ACTION_ROUTE_EVENT = "trafficsense.RouteEventUpdateUi";
	final public static String ACTION_ROUTE_EVENT_EXTRA_MESSAGE = "trafficsense.RouteEventUpdateUi.Extras.Message";
	final public static String ACTION_COORDS_READY = "trafficsense.CoordsReady";
	/**
	 * Action/Intent when GeoFence transition is detected by LocationClient.
	 */
	final public static String ACTION_NEXT_GEOFENCE_REACHED = "trafficesense.nextGeofenceAlarm";
	final public static String ROUTE_STOPPED = "route_stopped";
	final public static String ERROR = "route_error";
	
	/**
	 * Interval of time-based events in development mode. (ms)
	 */
	final public static long TEST_TIME = 5000;
	/**
	 * Duration to vibrate on certain events.
	 */
	final public static int VIBRATOR_DURATION = 250;
	/**
	 * Tracker service types.
	 */
	final public static int SERVICE_TIME_ONLY = 1;
	final public static int SERVICE_LOCATION_ONLY = 2;
	final public static int SERVICE_LOCATION_AND_TIME = 3;
	/**
	 * Magic stopCode for those stops without one.
	 */
	final public static String NO_STOP_CODE = "XXXX";
	/**
	 * The UUID of our Pebble smart watch application.
	 * @see appinfo.json
	 */
	final public static String PEBBLE_APP_UUID = "83eef382-21a4-473a-a189-ceffa42f86b1";
	/**
	 * Language codes.
	 */
	final public static int LANG_FI = 0;
	final public static int LANG_SV = 1;
	final public static int LANG_EN = 2;
	
	/**
	 * id of the notification 
	 */
	final public static int NOTIFICATION_ID = 1;
	
	/**
	 * how long the phone vibrates on alerts
	 */
	final public static int VIBRATION_TIME = 4000;
	
	/**
	 * the time offset that can be passed timeOnlyService
	 */
	final public static String OFFSET = "offset";

	/**
	 * JourneyParser codes
	 */
	final public static int PARSER_FAILURE = -1;
	final public static int PARSER_SUCCESS = 0 ;
	final public static int MALFORMED_JOURNEY_TEXT = 1;
	final public static int PARSER_INVALIDCASE = 2;
	final public static int PARSER_STOPCODEINVALID =3;

}

