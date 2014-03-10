package org.apps8os.trafficsense.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apps8os.trafficsense.android.Constants;
import org.apps8os.trafficsense.core.Route;
import org.apps8os.trafficsense.core.RouteConstants;
import org.apps8os.trafficsense.core.Segment;
import org.apps8os.trafficsense.core.Waypoint;

import com.google.gson.Gson;



/**
 * Class for retrieving more information on a journey.
 * This includes GPS coordinates for Waypoints,
 * and HSL transport type for Segments.
 * 
 * TODO 1. support for Finnish and Swedish
 * TODO 2. documentation.
 */
public class JourneyInfoResolver {

	private final static String HSL_API_ACCESS_POINT = "http://api.reittiopas.fi/hsl/prod/?";
	private final static String HSL_API_USER = "trafficsenseuser";
	private final static String HSL_API_PASS = "trafficsense";
	private final static String HSL_API_FORMAT = "json";
	/**
	 * Coordinate system of choice: WGS84.
	 */
	private final static String HSL_API_EPSG_OUT = "4326";
	private final static String HSL_API_EPSG_IN = HSL_API_EPSG_OUT;
	private final static String[] LANG = { "fi", "sv", "en" };
	private final static int defLang = 2;

	/*
	private final static String[] locTypes = { "" };
	private final static int disableErrorCorr = 0;
	private final static int disableUniqStopNames = 0;
	*/

	/**
	 * TODO: Once language support is implemented, remove the lang/LANG part.
	 */
	private final static String HSL_API_BASE_URL = HSL_API_ACCESS_POINT
			+ "user=" + HSL_API_USER + "&pass=" + HSL_API_PASS + "&format="
			+ HSL_API_FORMAT + "&epsg_in=" + HSL_API_EPSG_IN + "&epsg_out="
			+ HSL_API_EPSG_OUT + "&lang=" + LANG[defLang];

	/**
	 * 
	 */
	private final static String lineResponseLimit = "001";
	private static class LineInfo {
		public int transport_type_id;
	};
	
	/**
	 * 
	 */
	private final static String stopResponseLimit = "000000001";
	private static class StopInfo {
		public String wgs_coords;
	};
	
	private ByteArrayOutputStream mByteOutStream;
	private HttpClient mHttpClient;
	private Gson mGson;
	private boolean errorOccurred;
	
	/**
	 * Constructor.
	 */
	public JourneyInfoResolver () {
		mHttpClient = new DefaultHttpClient();
		mByteOutStream = new ByteArrayOutputStream();
		mByteOutStream.reset();
		mGson = new Gson();
	}
	
	/**
	 * Retrieve GPS coordinates via HSL API for all Waypoints whose stopCode is
	 * available. Others are ignored.
	 * 
	 * @param journey the journey for which GPS coordinates shall be retrieved.
	 * @return true on success, false otherwise.
	 */
	public boolean retrieveCoordinatesFromHsl(Route journey) {
		if (journey == null) {
			return false;
		}
		errorOccurred = false;
		ArrayList<Segment> segments = journey.getSegmentList();
		for (Segment segment : segments) {
			lookupSegmentTransportType(segment);
			ArrayList<Waypoint> waypoints = segment.getWaypointList();
			for (Waypoint waypoint : waypoints) {
				lookupWaypointCoordinate(waypoint);
			}
		}
		if (errorOccurred) {
			System.out.println("DBG retrieveCoordinatesFromHsl errorOccurred = true");
			// TODO error handling
			return false;
		}
		journey.setCoordsReady(true);
		return true;
	}
	
	/**
	 * Query HSL API for the actual 'transport_type' of this segment.
	 * Walking(0), Metro(6), and Ferry(7) are determined by Segment.setSegmentType().
	 * 
	 * @param segment the segment.
	 */
	private void lookupSegmentTransportType(Segment segment) {
		if (segment == null) {
			// TODO: error handling.
			errorOccurred = true;
			return;
		}
		if (segment.getSegmentType() != RouteConstants.UNKNOWN) {
			// Determined by Segment.setSegmentType() already.
			return;
		}
		String url = buildGetLineInfoUrl(lineResponseLimit, segment.getSegmentMode());
		String result = doHttpGetRequest(url);
		if (result.isEmpty()) {
			// TODO: error handling.
			errorOccurred = true;
			return;
		}
		LineInfo[] info = mGson.fromJson(result, LineInfo[].class);
		if (info.length == 0) {
			System.out.println("DBG lookupSegmentTransportType info[] size = 0");
			// TODO: error handling.
			errorOccurred = true;
			return;
		}
		segment.setSegmentType(info[0].transport_type_id);
	}
	
	/**
	 * Query HSL API for the GPS coordinate of a given Waypoint.
	 * Do nothing if stopCode is unavailable.
	 * 
	 * @param waypoint waypoint to query for
	 */
	private void lookupWaypointCoordinate(Waypoint waypoint) {
		if (waypoint == null) {
			// TODO: error handling.
			errorOccurred = true;
			return;
		}
		String stopCode = waypoint.getWaypointStopCode();
		if (stopCode == null || stopCode.isEmpty() || stopCode.equals(Constants.NO_STOP_CODE)) {
			// TODO: Actually, the first two cases should be error if they get to this point.
			return;
		}
		String url = buildGetStopInfoUrl(stopResponseLimit, stopCode);
		String result = doHttpGetRequest(url);
		if (result.isEmpty()) {
			// TODO error handling
			errorOccurred = true;
			return;
		}
		StopInfo[] stop = mGson.fromJson(result, StopInfo[].class);
		if (stop.length == 0) {
			System.out.println("DBG lookupWaypointCoordinate stop[] size = 0");
			// TODO error handling.
			errorOccurred = true;
			return;
		}
		// Parse longitude and latitude coordinates from the result string
		String coords = stop[0].wgs_coords;
		if (coords.isEmpty()) {
			System.out.println("DBG lookupWaypointCoordinate coords empty");
			// TODO error handling.
			errorOccurred = true;
			return;
		}
		
		double longCord = Double.parseDouble(coords.substring(9, 17));
		double latCord = Double.parseDouble(coords.substring(0, 8));
		waypoint.setCoordinate(longCord, latCord);
	}
	
	/**
	 * Perform an HTTP GET request.
	 * 
	 * Not thread-safe.
	 * 
	 * @param url url to GET.
	 * @return response body. empty on error.
	 */
	private String doHttpGetRequest(String url) {
		String responseString = "";
		System.out.println("DBG doHttpGetRequest url=" + url);
		try {
			HttpResponse response = mHttpClient.execute(new HttpGet(url));
			StatusLine statusLine = response.getStatusLine();
			HttpEntity responseBody = response.getEntity();
			if (statusLine.getStatusCode() == HttpStatus.SC_OK &&
					responseBody != null) {
				response.getEntity().writeTo(mByteOutStream);
				responseString = mByteOutStream.toString();
				// Clear the buffer
				mByteOutStream.reset();
				responseBody.consumeContent();
			} else {
				System.out.println("DBG doHttpGetRequest status="
						+ statusLine.getStatusCode() + " : "
						+ statusLine.getReasonPhrase());
				// TODO error handling
				errorOccurred = true;
			}
			// Close the connection
			if (responseBody == null) {
				if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
					System.out.println("DBG doHttpGetRequest responseBody/Entity = null");
					// TODO error handling
				}
			} else {
				responseBody.consumeContent();
			}
		} catch (ClientProtocolException ex) {
			System.out.println("DBG doHttpGetRequest ClientProtocolEx: "
					+ ex.getMessage());
			// TODO error handling.
			errorOccurred = true;
		} catch (IOException ex) {
			System.out.println("DBG doHttpGetRequest IOEx: " + ex.getMessage());
			// TODO error handling.
			errorOccurred = true;
		}
		System.out.println("DBG doHttpGetRequest response: " + responseString);
		return responseString;
	}


	/**
	 * TODO: documentation
	 * 
	 * @param responseLimit
	 * @param query
	 * @return
	 */
	private static String buildGetLineInfoUrl(String responseLimit, String query) {
		String url;
		url = HSL_API_BASE_URL + "&request=lines" + "&p=" + responseLimit
				+ "&query=" + query;
		return url;
	}

	
	/**
	 * TODO: documentation
	 * 
	 * @param responseLimit
	 * @param stopCode
	 * @return
	 */
	private static String buildGetStopInfoUrl(String responseLimit,
			String stopCode) {
		String url;
		url = HSL_API_BASE_URL + "&request=stop" + "&p=" + responseLimit
				+ "&code=" + stopCode;
		return url;
	}
	
	
	/**
	 * TODO: 1. Documentation
	 * TODO: 2. Test!!
	 * @deprecated unused and not tested yet.
	 * 
	 * @param responseLimit
	 * @param cities
	 * @param locType
	 * @param key
	 * @return
	 */
	private static String buildGetGeocodingUrl(String responseLimit,
			String cities, String locType, String key) {
		String url;
		url = HSL_API_BASE_URL + "&request=geocode" + "&loc_types=" + locType
				+ "&p=" + responseLimit + "&cities=" + cities + "&key=" + key;
		return url;
	}

	
	/**
	 * TODO: 1. Documentation
	 * TODO: 2. Test!!
	 * @deprecated unused and not tested yet.
	 * 
	 * @param responseLimit
	 * @param x
	 * @param y
	 * @return
	 */
	private static String buildGetReverseGeocodingUrl(String responseLimit,
			String x, String y) {
		String url;
		url = HSL_API_BASE_URL + "&request=reverse_geocode" + "&coordinate="
				+ x + "," + y + "&p=" + responseLimit;
		return url;
	}

}
