package org.apps8os.trafficsense.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Utility class holding various time string parsers.
 */
public class TimeParser {
	/**
	 * No instantiation of this class.
	 */
	private TimeParser() {}

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
			System.out.println("DBG strDateTimeToDate: " +timeStr +"->"+ result.toString());
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
