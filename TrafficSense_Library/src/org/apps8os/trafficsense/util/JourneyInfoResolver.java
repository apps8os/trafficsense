package org.apps8os.trafficsense.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
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
	 * HSL Geocoding response limiter.
	 * Retrieve only field number 6: coords.
	 * TODO: HSL bug? It is actually 7
	 */
	private final static String geocodingResponseLimit = "0000001";
	/**
	 * A structure for Gson to parse JSON response from HSL. 
	 */
	private static class Geocoding {
		public String coords;
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
	public synchronized boolean setLanguage(final int language) {
		boolean success = true;
		switch (language) {
		case Constants.LANG_EN:
			mLang = language;
			break;
		case Constants.LANG_FI:
			mLang = language;
			break;
		case Constants.LANG_SV:
			mLang = language;
			break;
		default:
			mLang = Constants.LANG_EN;
			success = false;
			break;
		}
		HSL_API_BASE_URL = HSL_API_BASE_URL_FIXED + "&lang="
				+ HSL_API_LANG[mLang];
		return success;
	}
	
	/**
	 * Retrieve GPS coordinates via HSL API for all Waypoints whose stopCode is
	 * available. Others are ignored.
	 * 
	 * Invokes thread-unsafe private methods which make HTTP requests.
	 * Thus, this method is also thread-unsafe and is synchronized.
	 *  
	 * @param journey the journey for which GPS coordinates shall be retrieved.
	 * @throws JourneyInfoResolverException
	 */
	public synchronized void retrieveCoordinatesFromHsl(Route journey) {
		ArrayList<Segment> segments = journey.getSegmentList();
		for (Segment segment : segments) {
			lookupSegmentTransportType(segment);
			ArrayList<Waypoint> waypoints = segment.getWaypointList();
			for (Waypoint waypoint : waypoints) {
				lookupWaypointCoordinate(waypoint);
			}
		}
		journey.setCoordsReady(true);
	}
	
	/**
	 * Query HSL API for the actual 'transport_type' of this segment.
	 * Walking(0), Metro(6), and Ferry(7) are determined by Segment.setSegmentType().
	 * 
	 * Makes HTTP request, not thread-safe.
	 * 
	 * @param segment the segment.
	 * @throws JourneyInfoResolverException
	 */
	private void lookupSegmentTransportType(Segment segment) throws JourneyInfoResolverException {
		if (segment == null) {
			throw new JourneyInfoResolverException("null segment");
		}
		if (segment.getSegmentType() != RouteConstants.UNKNOWN) {
			// Already determined
			return;
		}
		String url = buildGetLineInfoUrl(lineResponseLimit, segment.getSegmentMode());
		String result = doHttpGetRequest(url);
		if (result.isEmpty()) {
			throw new JourneyInfoResolverException("HTTP result is empty");
		}
		LineInfo[] info = mGson.fromJson(result, LineInfo[].class);
		if (info.length == 0) {
			throw new JourneyInfoResolverException("info[] size = 0");
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
	 * @throws JourneyInfoResolverException
	 */
	private void lookupWaypointCoordinate(Waypoint waypoint)
			throws JourneyInfoResolverException {
		if (waypoint == null) {
			throw new JourneyInfoResolverException("null waypoint");
		}

		String url;
		if (waypoint.hasStopCode() == false) {
			url = buildGetGeocodingUrl(geocodingResponseLimit, "address",
					waypoint.getWaypointName());
		} else {
			// Sanity check
			String stopCode = waypoint.getWaypointStopCode();
			if (stopCode == null || stopCode.isEmpty()) {
				throw new JourneyInfoResolverException("unfilled stopCode");
			}
			url = buildGetStopInfoUrl(stopResponseLimit, stopCode);
		}

		String result = doHttpGetRequest(url);
		if (result.isEmpty()) {
			throw new JourneyInfoResolverException("HTTP response is empty");
		}

		String long_lat[] = null;
		if (waypoint.hasStopCode() == false) {
			//System.out.println("DBG Geocoding case");
			Geocoding[] geocods = mGson.fromJson(result, Geocoding[].class);
			if (geocods.length == 0) {
				throw new JourneyInfoResolverException("coords[].length = 0");
			}
			long_lat = geocods[0].coords.split(",", 2);
		} else {
			//System.out.println("DBG StopInfo case");
			StopInfo[] stops = mGson.fromJson(result, StopInfo[].class);
			if (stops.length == 0) {
				throw new JourneyInfoResolverException("stop[].length = 0");
			}
			long_lat = stops[0].wgs_coords.split(",", 2);
		}

		double latCord = Double.parseDouble(long_lat[0]);
		double longCord = Double.parseDouble(long_lat[1]);
		waypoint.setCoordinate(longCord, latCord);
	}
	
	/**
	 * Make an HTTP GET request.
	 * 
	 * Uses shared HttpClient instance and thus not thread-safe.
	 * 
	 * @param url url to GET.
	 * @return response body. empty on error.
	 * @throws JourneyInfoResolverException
	 */
	private String doHttpGetRequest(String url) throws JourneyInfoResolverException {
		String responseString = "";
		System.out.println("DBG doHttpGetRequest url=" + url);
		try {
			HttpResponse response = mHttpClient.execute(new HttpGet(url));
			StatusLine statusLine = response.getStatusLine();
			if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
				response.getEntity().writeTo(mByteOutStream);
				// TODO: Encoding
				responseString = mByteOutStream.toString();
			} else {
				throw new JourneyInfoResolverException("HTTP status:"
						+ statusLine.getStatusCode() + " : "
						+ statusLine.getReasonPhrase());
			}
			HttpEntity responseBody = response.getEntity();
			if (responseBody == null) {
				throw new JourneyInfoResolverException("responseBody is null");
			}
			// Clear the buffer
			mByteOutStream.reset();
			responseBody.consumeContent();
		} catch (ClientProtocolException ex) {
			throw new JourneyInfoResolverException("ClientProtocolEx: "
					+ ex.getMessage());
		} catch (IOException ex) {
			throw new JourneyInfoResolverException("IOEx: " + ex.getMessage());
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
	
	private String buildGetGeocodingUrl(String responseLimit, String locType,
			String key) {
		if (locType.isEmpty()) {
			locType = "stop|address";
		}
		String url;
		try {
			url = HSL_API_BASE_URL + "&request=geocode" + "&loc_types="
					+ URLEncoder.encode(locType, "UTF-8") + "&p="
					+ responseLimit + "&key=" + URLEncoder.encode(key, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new JourneyInfoResolverException(e.getMessage());
		}
		return url;
	}

/*
	private String buildGetReverseGeocodingUrl(String responseLimit,
			String x, String y) {
		String url;
		url = HSL_API_BASE_URL + "&request=reverse_geocode" + "&coordinate="
				+ x + "," + y + "&p=" + responseLimit;
		return url;
	}
*/

	/**
	 *A generic exception for possible errors that could happen in this class. 
	 */
	static public class JourneyInfoResolverException extends RuntimeException {
		private static final long serialVersionUID = 1L;
		public JourneyInfoResolverException() { super(); }
		public JourneyInfoResolverException(String message) { super(message); }
		public JourneyInfoResolverException(String message, Throwable cause) { super(message, cause); }
		public JourneyInfoResolverException(Throwable cause) { super(cause); }
	}
}
