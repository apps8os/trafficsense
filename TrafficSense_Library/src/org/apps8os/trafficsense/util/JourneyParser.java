package org.apps8os.trafficsense.util;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Scanner;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Class for parsing plain text journey into a Gson JSON object.
 * 
 * The parser accepts English, Finnish, and Swedish
 * Entry point: {@link #parseString(String)}
 * Use {@link #getJsonObj()} or {@link #getJsonText()} to retrieve the result.
 * 
 * JSON object is organize as follow :
 * 
 * "_mainObj"(JsonObject) contains :
 *     Property "date"
 *     Property "start"
 *     Property "dest"
 *     Property "arrivalTime"
 * 
 * Property "segments" that is JsonArray called "mSegmentsArray" containing:
 * "SegmentsObj"(JsonObject) contains:
 *   Property "startTime"
 *   Property "startPoint"
 *   Property "mode"
 * 
 * Property "waypoints" that is JsonArray called "waypointsArray" containing:
 * "waypointObj"(JsonObject ) contains:
 *   Property "time"
 *   Property "name"
 *   Property "stopCode" (except for walking)
 * @see #addJsonObject()
 */
public class JourneyParser {
	/**
	 * The Gson JSON object for the current journey.
	 * */
	private JsonObject mJourneyObj;
	/**
	 * Parsed segments.
	 */
	private JsonArray mSegmentsArray;
	/**
	 * Temporary Array for the values of each segment.
	 *  (startTime,startPoint,mode,waypoints)
	 */
	private ArrayList<String> mTextArray;
	/**
	 * Attribute to keep track in which line we are in the journey text.
	 */
	private int mCurrentLineLoc = 0;
	/**
	 * Line stored from the text journey
	 */
	private String mCurrentLine = null;

	/**
	 * Class constructor
	 */
	public JourneyParser() {
		mJourneyObj = new JsonObject();
		mSegmentsArray = new JsonArray();
		mTextArray = new ArrayList<String>();
	}

	/**
	 * Increment the number of lines of journey processed.
	 */
	private void incrementLine() {
		mCurrentLineLoc ++;
	}

	/**
	 * Set the text line of the moment.
	 * 
	 * @param txtLine
	 *            the journey text line.
	 * @see #parseOneLine(String)
	 */
	private void setTxtLine(String txtLine) {
		mCurrentLine = txtLine;
	}

	/**
	 * Temporary array that stores one information segment at time one segment
	 * has startTime,startPoint,mode,waypoints
	 * 
	 * @return array of strings carrying the information of one segment
	 */
	private ArrayList<String> getTextArray() {
		return mTextArray;
	}

	/**
	 * Clear all the elements of the text array
	 * 
	 * @see #getTextArray()
	 */
	private void flushTextArray() {
		mTextArray.clear();
	}

	/**
	 * Add a line of the segment to the temporary TextArray
	 * 
	 * @param line the line to add
	 * 
	 * @see #getTextArray()
	 */
	private void addTextArray(String line) {
		// trim removes the white space at the beginning and the end
		mTextArray.add(line.trim());
	}

	/**
	 * Return the Gson JSON object of the journey.
	 * 
	 * @return Gson JSON object of the journey.
	 */
	public JsonObject getJsonObj() {
		return mJourneyObj;
	}

	/**
	 * Add a String property field to journey object.
	 * 
	 * @param key
	 *            key as a String.
	 * @param value
	 *            value as a String.
	 */
	private void addProperty(String key, String value) {
		mJourneyObj.addProperty(key, value);
	}

	/**
	 * Add a JsonElement property field to journey object.
	 * 
	 * @param key
	 *            key as a String.
	 * @param element
	 *            value as a JsonElement.
	 */
	private void addProperty(String key, JsonElement element) {
		mJourneyObj.add(key, element);
	}

	/**
	 * Adds the given Segment into the journey.
	 * 
	 * One segment is arranged as follow:
	 * "startTime","startPoint","mode", and a list of "waypoints".
	 */
	private void setSegmentsArray(JsonElement value) {
		mSegmentsArray.add(value);
	}

	/**
	 * Add the elements of the journey to the JSON object.
	 * 
	 * General Structure:
	 * Line 1 add the time
	 * Line 2 is always the string Departure/Lähtö/Avgång - do nothing.
	 * 
	 * Line 3 until the second last are the segments and have the
	 * following structure:
	 * Time and location
	 *  Transportation mode (Walking, Bus... etc)
	 *    Time Location (several lines)
	 *    
	 * Stop condition is through a blank line
	 * 
	 * Exception conditions from the above segments structure:
	 * 1) Two first lines can be equal and only third line has the mode.
	 *   This happens when you have to wait for a while a some point
	 *   before the segment starts.
	 * 
	 * Last line is always "Arrival"(English) or "Perillä"(Suomi/Finnish) or
	 * "Ankomst"(Svenska/Swedish).
	 * 
	 * @throws java.text.ParseException
	 * 
	 * @see #organizeJson(String)
	 */
	private void addJsonObject() throws ParseException {

		/*
		System.out.println("DBG addJsonObject loc:" + mCurrentLineLoc + " arraySz:"
				+ _textArray.size() + " line:" + mCurrentLine);
				*/

		/**
		 * Temporary array string, store the the line of a segment from the text
		 * array in two parts: time and location
		 */
		String[] str_split = null;
		/**
		 * Number of the exception case. 0 = Normal case
		 * Also used as the offset into the plain text.
		 */
		int exception = 0;
		String[] temp1, temp2; // Temporaries variables

		// The first line is the date
		if (mCurrentLineLoc == 1) {
			addProperty("date", mCurrentLine);
			return;
		}

		/**
		 * Extract the starting point of the first segment and use it
		 * as the starting point of the journey.
		 */
		if (mCurrentLineLoc == 3) {
			String[] parts = mCurrentLine.split(" ", 2);
			addProperty("start", parts[1]);
			return;
		}

		/**
		 * Now we have a segment. 
		 */

		if (mTextArray.get(1).contains(":")) {
			// We are in an exception case: a longer wait here
			temp1 = mTextArray.get(0).split(" ", 2);
			temp2 = mTextArray.get(1).split(" ", 2);
			if ((temp1[1].compareTo(temp2[1])) == 0) {
				// Exception case number 1

				// Split the string in two parts at a space (" ")
				str_split = mTextArray.get(1).split(" ", 2);
				exception = 1;
				//System.out.println("DBG EXCEPTION: " + exception);
			}
		} else {
			// Normal case
			str_split = mTextArray.get(0).split(" ", 2);
			//System.out.println("DBG NORMAL CASE: " + exception + "->" + str_split[1]);
		}

		if (str_split == null) {
			throw new ParseException(
					"JourneyParser: Malformed journey text:", mCurrentLineLoc);
		}

		if (!mTextArray.get(1).equals("Arrival")
				&& !mTextArray.get(1).equals("Perillä")
				&& !mTextArray.get(1).equals("Ankomst")) {

			JsonObject segmentsObj = new JsonObject();
			segmentsObj.addProperty("startTime", str_split[0]);
			segmentsObj.addProperty("startPoint", str_split[1]);

			//System.out.println("DBG CASE:" + exception);
			switch (exception) {
			case 0:
				segmentsObj.addProperty("mode", mTextArray.get(1));
				break;
			case 1:
				segmentsObj.addProperty("mode", mTextArray.get(2));
				break;

			default:
				// TODO: Can we ever reach here?
				throw new ParseException(
						"JourneyParser: Invalid segment structure:", mCurrentLineLoc);
			}

			setSegmentsArray(segmentsObj);

			/**
			 * Now we parse the waypoints.
			 */
			
			JsonArray waypointsArray = new JsonArray();

			//System.out.println("DBG ARRAY SIZE: " + mTextArray.size() + "exception: " + exception);

			boolean isWalkingSegment = false;
			if (mTextArray.get(exception + 1).contains("Walking")
					|| mTextArray.get(exception + 1).contains("Kävelyä")
					|| mTextArray.get(exception + 1).contains("Gång")) {
				isWalkingSegment = true;
			}
			
			for (int i = exception + 2; i < mTextArray.size(); i++) {

				JsonObject waypointObj = new JsonObject();

				str_split = getTextArray().get(i).split(" ", 2);

				//System.out.println("DBG textArray " + mTextArray.get(i));

				waypointObj.addProperty("time", str_split[0]);
				waypointObj.addProperty("name", str_split[1]);

				/**
				 *  A stopCode field for each waypoint(stop) is expected from
				 *  a non-walking Segment.
				 */
				if (!isWalkingSegment) {

					//System.out.println("DBG STRING: " + str_split[0] + " "+ str_split[1]);
					String stopCode;
					int start, end;
					/**
					 * Get the last pair of parentheses from the string because
					 * usually that is where the stop code lies.
					 */
					start = str_split[1].lastIndexOf("(") + 1;
					end = str_split[1].lastIndexOf(")");

					if (start == -1 || end == -1) {
						throw new ParseException(
								"JourneyParser: No stopCode:"+mCurrentLine, mCurrentLineLoc);
					}

					stopCode = str_split[1].substring(start, end);

					waypointObj.remove("name");
					waypointObj.addProperty("name",
							str_split[1].substring(0, start - 1));
					waypointObj.addProperty("stopCode", stopCode);
				}

				waypointsArray.add(waypointObj);
			}

			segmentsObj.add("waypoints", waypointsArray);

		} else {
			// Last line of the file, "Arrival" line

			//System.out.println("DBG ARRAY " + mSegmentsArray);
			addProperty("dest", str_split[1]);
			addProperty("arrivalTime", str_split[0]);
			addProperty("segments", mSegmentsArray);
		}
	}

	/**
	 * Augment journey object with a newly encountered journey text line.
	 * The newly encountered line should had been passed to
	 * {@link #setTxtLine(String)} and {@link #incrementLine()} should be called
	 * before.
	 * 
	 * @throws java.text.ParseException from {@link #addJsonObject()}
	 * 
	 * @see #addJsonObject()
	 */
	private void organizeJson() throws ParseException {

		/*
		System.out.println("DBG organizeJson loc:" + mCurrentLineLoc + " arraySz:"
				+ mTextArray.size() + " line:" + mCurrentLine);
				*/

		switch (mCurrentLineLoc) {
		case 1:
			// Add to json object the key value "date"
			addJsonObject();
			return;
		case 2:
			// "Departure" line
			return;
		case 3:
			// Add to json object the key value "start", starting point
			addJsonObject();
			break;
		default:
			// Normal segment text.
			break;
		}

		/**
		 * Checks if a String is not empty (""), not null and not whitespace only.
		 */
		if (mCurrentLine.trim().equals("")) {
			// The line is blank - the end of a segment.
			addJsonObject();
			flushTextArray();
			return;
		} else {
			addTextArray(mCurrentLine);
		}

		// End of the last segment
		if (mCurrentLine.equals("Arrival") || mCurrentLine.equals("Perillä")
				|| mCurrentLine.equals("Ankomst")) {
			addJsonObject();
			flushTextArray();
		}
	}

	/**
	 * Return the parsed journey in JSON text.
	 * 
	 * @return JSON object
	 */
	public String getJsonText() {
		return mJourneyObj.toString();
	}

	/**
	 * Parse the current line of the journey.
	 * 
	 * @param line  line to be parsed
	 * 
	 * @throws java.text.ParseException from {@link #organizeJson()}
	 */
	private void parseOneLine(String line) throws ParseException {
		//System.out.println("DBG parseOneLine: " + line);
		setTxtLine(line);
		incrementLine();
		organizeJson();
	}

	/**
	 * Parse a journey supplied in a String with line breaks.
	 * 
	 * @param jsonText
	 *            plain text journey with line breaks.
	 *
	 * @throws java.text.ParseException on error.
	 */
	public void parseString(String jsonText) throws ParseException {
		Scanner scanner = new Scanner(jsonText);
		try {
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				parseOneLine(line);
				if (line.equals("Arrival") || line.equals("Perillä")
						|| line.equals("Ankomst")) {
					// Reached the end of the plain text journey
					break;
				}
			}
		} catch (ParseException e) {
			System.out.println("DBG ParseException: "+e.getMessage());
			throw e;
		} finally {
			scanner.close();
		}
	}

}
