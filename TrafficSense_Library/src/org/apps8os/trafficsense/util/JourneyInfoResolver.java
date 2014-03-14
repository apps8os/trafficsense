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
 * TODO 1. test Finnish and Swedish support
 * TODO 2. documentation.
 * 
 * @see http://developer.reittiopas.fi/pages/en/http-get-interface-version-2.php
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
	/**
	 * Sequence here must match the values of Constants.LANG_*
	 */
	private final static String[] HSL_API_LANG = { "fi", "sv", "en" };
	/**
	 * Fixed part of API access URL.
	 */
	private final static String HSL_API_BASE_URL_FIXED = HSL_API_ACCESS_POINT
			+ "user=" + HSL_API_USER + "&pass=" + HSL_API_PASS + "&format="
			+ HSL_API_FORMAT + "&epsg_in=" + HSL_API_EPSG_IN + "&epsg_out="
			+ HSL_API_EPSG_OUT;
	/**
	 * HSL Line Information response limiter.
	 * Retrieve only field number 3: transport_type_id.
	 */
	private final static String lineResponseLimit = "001";
	/**
	 * A structure for Gson to parse JSON response from HSL. 
	 */
	private static class LineInfo {
		public int transport_type_id;
	};
	/**
	 * HSL Stop Information response limiter.
	 * Retrieve only field number 9: wgs_coords.
	 */
	private final static String stopResponseLimit = "000000001";
	/**
	 * A structure for Gson to parse JSON response from HSL. 
	 */
	private static class StopInfo {
		public String wgs_coords;
	};
	
	/**
	 * ByteArrayOutputStream for flattening HTTP responses into Strings.
	 */
	private ByteArrayOutputStream mByteOutStream;
	/**
	 * Shared HttpClient instance.
	 */
	private HttpClient mHttpClient;
	/**
	 * Google Json library instance.
	 */
	private Gson mGson;
	/**
	 * Language.
	 * @see Constants.LANG_*
	 */
	private int mLang;
	/**
	 * Flag indicating an error status in a long process.
	 */
	private boolean errorOccurred;
	/**
	 * API access base URL.
	 * Initialized by the constructor.
	 * Language parameter updated by {@link #setLanguage(int)}
	 */
	private String HSL_API_BASE_URL;
	
	/**
	 * Constructor.
	 */
	public JourneyInfoResolver () {
		mHttpClient = new DefaultHttpClient();
		mByteOutStream = new ByteArrayOutputStream();
		mByteOutStream.reset();
		mGson = new Gson();
		setLanguage(Constants.LANG_EN);
	}
	
	/**
	 * Set the desired language.
	 * Defaults to Constants.LANG_EN on error.
	 * 
	 * This method is synchronized because changing the language
	 * in the middle of a series of API requests may result in
	 * mixed response sets.
	 * 
	 * @param language language code (Constants.LANG_*)
	 * @return true on success.
	 */
	public boolean setLanguage(final int language) {
		boolean success = true;
		synchronized (this) {
			switch (language) {
			case Constants.LANG_EN:
				break;
			case Constants.LANG_FI:
				break;
			case Constants.LANG_SV:
				break;
			default:
				mLang = Constants.LANG_EN;
				success = false;
				break;
			}
			mLang = language;
			HSL_API_BASE_URL = HSL_API_BASE_URL_FIXED + "&lang="
					+ HSL_API_LANG[mLang];
			return success;
		}
	}
	
	/**
	 * Retrieve GPS coordinates via HSL API for all Waypoints whose stopCode is
	 * available. Others are ignored.
	 * 
	 * Invokes thread-unsafe private methods which make HTTP requests.
	 * Thus, this method is also thread-unsafe and is synchronized.
	 *  
	 * @param journey the journey for which GPS coordinates shall be retrieved.
	 * @return true on success, false otherwise.
	 */
	public boolean retrieveCoordinatesFromHsl(Route journey) {
		if (journey == null) {
			return false;
		}
		synchronized (this) {
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
	}
	
	/**
	 * Query HSL API for the actual 'transport_type' of this segment.
	 * Walking(0), Metro(6), and Ferry(7) are determined by Segment.setSegmentType().
	 * 
	 * Makes HTTP request, not thread-safe.
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
	 * Makes HTTP request, not thread-safe.
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
	 * Make an HTTP GET request.
	 * 
	 * Uses shared HttpClient instance and thus not thread-safe.
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
	 * Builds the URL to query HSL API for Line information.
	 * 
	 * @param responseLimit response field filter.
	 * @param query search key.
	 * @return the URL.
	 */
	private String buildGetLineInfoUrl(String responseLimit, String query) {
		String url;
		url = HSL_API_BASE_URL + "&request=lines" + "&p=" + responseLimit
				+ "&query=" + query;
		return url;
	}

	
	/**
	 * Builds the URL to query HSL API for Stop information.
	 * 
	 * @param responseLimit response field filter.
	 * @param stopCode stopCode.
	 * @return the URL.
	 */
	private String buildGetStopInfoUrl(String responseLimit,
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
	private String buildGetGeocodingUrl(String responseLimit,
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
	private String buildGetReverseGeocodingUrl(String responseLimit,
			String x, String y) {
		String url;
		url = HSL_API_BASE_URL + "&request=reverse_geocode" + "&coordinate="
				+ x + "," + y + "&p=" + responseLimit;
		return url;
	}

}
