package org.apps8os.trafficsense.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.apps8os.trafficsense.TrafficsenseContainer;
import org.apps8os.trafficsense.core.Route;

/**
 * Utility class holding various time string parsers.
 */
public class TimeParser {
	/**
	 * No instantiation of this class.
	 */
	private TimeParser() {}

	/**
	 * Parse a given time string and adds one day if it is in the past.
	 * Used to parse Time/Date from an Reittiopas journey which may cross midnight.
	 * Supports English, Swedish, and Finnish journey text from Reittiopas.
	 * 
	 * MUST only be called when tracking a journey.
	 * 
	 * Detect midnight crossing by comparing the given time against the starting
	 * time of the first waypoint in route.
	 * 
	 * @param route the journey we are tracking.
	 * @param timeStr date-time string.
	 * @return number of milliseconds since epoch.
	 * @throws TimeParserException
	 */
	public static long strWaypointTimeToMillisCrossDay(Route route, String timeStr)
			throws TimeParserException {
		System.out.println("DBG ToMillisCrossDay str: "+timeStr);
		Date parsed = strDateTimeToDate(route.getDate() + " " + timeStr);
		long result = parsed.getTime();
		System.out.println("DBG ToMillisCrossDay parsed: "+result);
		if (parsed.before(route.getFirstWaypointTime())) {
			System.out.println("DBG ToMillisCrossDay crossed midnight, adjusting");
			result += 86400000;
		}
		System.out.println("DBG ToMillisCrossDay: result "+result+" : "+new Date(result).toString());
		return result;
	}

	/**
	 * Parse a given date-time string.
	 * 
	 * Supports English, Swedish, and Finnish journey text from Reittiopas.
	 * 
	 * @param timeStr date-time string.
	 * @return a Date object.
	 * @throws TimeParserException
	 */
	public static Date strDateTimeToDate(String timeStr) throws TimeParserException {
		// Determine the language
		Locale locale;
		if (timeStr.contains("tai") || timeStr.contains("Keskiviikko")) {
			/**
			 * HSL API gives Finnish day of week as, for example, "Maanatai" but
			 * SimpleDateFormat recognizes only "Maanataina".
			 */
			// Watch out the extra space, it is there to exclude other suffixes.
			if (timeStr.contains("Keskiviikko ")) {
				timeStr = timeStr.replace("Keskiviikko", "Keskiviikkona");
			} else if (timeStr.contains("tai ")) {
				// Append -na to Manaatai,Tiistai,Torstai,Perjantai,Lauantai,Sunnuntai
				timeStr = timeStr.replace("tai", "taina");
			}
			locale = new Locale("fi");
		} else if (timeStr.contains("dag")) {
			// Swedish - Måndag,Tisdag,Onsdag,Torsdag,Fridag,Lördag,Söndag
			locale = new Locale("sv");
		} else if (timeStr.contains("day")) {
			// English
			locale = Locale.ENGLISH;
		} else {
			throw new TimeParserException("Unsupported language: "+timeStr);
		}
		try {
			Date result = new SimpleDateFormat("EEEE dd.M.yyyy kk:mm", locale)
					.parse(timeStr);
			//System.out.println("DBG strDateTimeToDate: " +timeStr +"->"+ result.toString());
			return result;
		} catch (ParseException e) {
			/**
			 * This is not recoverable and later stages will not be able to
			 * continue without this result, so we re-throw a RuntimeException.
			 */
			throw new TimeParserException(e.getMessage());
		}
	}
	
	/**
	 * Parse a given time string.
	 * 
	 * @param timeStr time string.
	 * @return a Date object.
	 * @throws TimeParserException
	 */
	public static Date strTimeToDate(String timeStr) throws TimeParserException {
		try {
			// Hour:Minute part is locale-neutral
			return new SimpleDateFormat("kk:mm").parse(timeStr);
		} catch (ParseException e) {
			/**
			 * This is not recoverable and later stages will not be able to continue without
			 * this result, so we re-throw a RuntimeException.
			 */
			throw new TimeParserException(e.getMessage());
		}
	}
	
	/**
	 *A generic exception for unrecoverable that could happen in this class. 
	 */
	static public class TimeParserException extends RuntimeException {
		private static final long serialVersionUID = 1L;
		public TimeParserException() { super(); }
		public TimeParserException(String message) { super(message); }
		public TimeParserException(String message, Throwable cause) { super(message, cause); }
		public TimeParserException(Throwable cause) { super(cause); }
	}
}
