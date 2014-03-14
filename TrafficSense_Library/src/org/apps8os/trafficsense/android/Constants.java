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
	 */
	final public static boolean useWallClock = false;
	/**
	 * Action strings of Intents.
	 */
	final public static String ACTION_NEXT_WAYPOINT = "trafficsense.NextWaypointAlarm";
	final public static String ACTION_GET_OFF = "trafficsense.GetOffAlarm";
	final public static String ACTION_ROUTE_EVENT = "trafficsense.RouteEventUpdateUi";
	final public static String ACTION_ROUTE_EVENT_EXTRA_MESSAGE = "trafficsense.RouteEventUpdateUi.Extras.Message";
	final public static String ACTION_COORDS_READY = "trafficsense.CoordsReady";
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
}
