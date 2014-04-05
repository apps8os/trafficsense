package org.apps8os.trafficsense.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Utility class holding various time string parsers.
 * 
 * TODO: Locale.
 */
public class TimeParser {
	/**
	 * No instantiation of this class.
	 */
	private TimeParser() {}

	/**
	 * Parse a given date-time string.
	 * 
	 * @param timeStr date-time string.
	 * @return a Date object.
	 * @throws TimeParserException
	 */
	public static Date strDateTimeToDate(String timeStr) throws TimeParserException {
		/**
		 *  TODO: Locale problem.
		 *  HSL API gives Finnish day of week as, for example, "Maanatai" but
		 *  SimpleDateFormat recognizes only "Maanataina".
		 */
		try {
			return new SimpleDateFormat("EEEE dd.M.yyyy kk:mm", Locale.ENGLISH)
					.parse(timeStr);
		} catch (ParseException e) {
			/**
			 * This is not recoverable and later stages will not be able to continue without
			 * this result, so we re-throw a RuntimeException.
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
		/**
		 * TODO: Locale support.
		 */
		try {
			return new SimpleDateFormat("kk:mm", Locale.ENGLISH)
					.parse(timeStr);
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
