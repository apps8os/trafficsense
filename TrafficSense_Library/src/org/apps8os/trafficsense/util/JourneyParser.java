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
 * The parser accepts the languages English, Finnish and Swedish
 * 
 * JSON object is organize as follow :
 * 
 * "_mainObj"(JsonObject) contains : Property "date" Property "start" Property
 * "dest" Property "arrivalTime"
 * 
 * Property "segments" that is JsonArray called "_segmentsArray" containing:
 * "SegmentsObj"(JsonObject) contains: Property "startTime" Property
 * "startPoint" Property "mode"
 * 
 * Property "waypoints" that is JsonArray called "waypointsArray" containing:
 * "waypointObj"(JsonObject ) contains: Property "time" Property "name" Property
 * "stopCode" (!walking)
 * 
 * @see #parseString(String)
 */

public class JourneyParser {

	/**
	 * The Gson JSON object for the current journey.
	 * */
	private JsonObject _mainObj;
	/**
	 * Parsed segments.
	 */
	private JsonArray _segmentsArray;
	/**
	 * Temporary Array for the values of each segment.
	 *  (startTime,startPoint,mode,waypoints)
	 */
	private ArrayList<String> _textArray = new ArrayList<String>();

	/**
	 * Attribute to keep track in which line we are in the journey text.
	 */
	private int mCurrentLineLoc = 0;
	/**
	 * line stored from the text journey
	 */
	private String mCurrentLine;

	/**
	 * Class constructor
	 */
	public JourneyParser() {
		_mainObj = new JsonObject();
		_segmentsArray = new JsonArray();
	}

	/**
	 * Increment the number of lines of journey processed.
	 */
	private void incrementLine() {
		mCurrentLineLoc ++;
	}

	/**
	 * Set the text line of the moment
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

		return _textArray;
	}

	/**
	 * Clear all the elements of the text array
	 * 
	 * @see #getTextArray()
	 */
	private void flushTextArray() {

		getTextArray().clear();
	}

	/**
	 * Add a line of the segment to the temporary TextArray
	 * 
	 * @param line
	 * 
	 * @see #getTextArray()
	 */
	private void addTextArray(String line) {
		// trim removes the white space at the beginning and end
		this.getTextArray().add(line.trim());
	}

	/**
	 * Return the Gson JSON object of the journey.
	 * 
	 * @return Gson JSON object of the journey.
	 */
	public JsonObject getJsonObj() {
		return _mainObj;
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
		_mainObj.addProperty(key, value);
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
		_mainObj.add(key, element);
	}

	/**
	 * Contains all the segments of the parser
	 * 
	 * @return segments of the parser
	 */
	private JsonArray getSegmentsArray() {
		return _segmentsArray;
	}

	/**
	 * Set the each segment of the parser One segment is arranged:
	 * "startTime","startPoint","mode","waypoints"
	 * 
	 */
	private void setSegmentsArray(JsonElement value) {
		_segmentsArray.add(value);
	}

	/**
	 * Will add the elements to the JSON object
	 * 
	 * General Structure : line 1 add the time line 2 is always the string
	 * "Departure" do nothing
	 * 
	 * line 3 until second from the last is always segments and have the
	 * following struct: Time and location Transportation mode (e.g Walking Bus
	 * etc) Time location (several lines) Stop condition is through a blank line
	 * 
	 * Exception Conditions from the above segments structure: 1) Two first
	 * lines can be equal and only third line has the mode
	 * 
	 * Last line always string "Arrival" || "Perillä"(FIN lang) || "Ankomst"(SWE
	 * lang)
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
		int exception = 0; // Number of the exception case. 0 Normal case
		String[] temp1, temp2; // Temporaries variables

		if (mCurrentLineLoc == 1) {
			addProperty("date", mCurrentLine);
			return;
		}

		if (mCurrentLineLoc == 3) {
			String[] parts = mCurrentLine.split(" ", 2);
			addProperty("start", parts[1]);
			return;
		}

		if (getTextArray().get(1).contains(":")) {
			// We are in an exception case

			temp1 = getTextArray().get(0).split(" ", 2);
			temp2 = getTextArray().get(1).split(" ", 2);
			if ((temp1[1].compareTo(temp2[1])) == 0) {
				// Exception case number 1

				// split the string in two parts through a " "
				str_split = getTextArray().get(1).split(" ", 2);
				exception = 1;
				// System.out.println("EXCEPTION: " + exception);
			}
		} else {
			// Normal case
			str_split = getTextArray().get(0).split(" ", 2);
			// System.out.println("NORMAL CASE: " + exception + "->" +
			// str_split[1]);
		}

		if (str_split == null) {

			throw new ParseException(
					"JourneyParser: Malformed journey text:", mCurrentLineLoc);
		}

		if (!getTextArray().get(1).equals("Arrival")
				&& !getTextArray().get(1).equals("Perillä")
				&& !getTextArray().get(1).equals("Ankomst")) {

			JsonObject SegmentsObj = new JsonObject();
			SegmentsObj.addProperty("startTime", str_split[0]);
			SegmentsObj.addProperty("startPoint", str_split[1]);

			// System.out.println("CASE:" + exception);
			switch (exception) {
			case 0:
				SegmentsObj.addProperty("mode", getTextArray().get(1));
				break;
			case 1:
				SegmentsObj.addProperty("mode", getTextArray().get(2));
				break;

			default:
				throw new ParseException(
						"JourneyParser: Invalid segment structure:", mCurrentLineLoc);

			}

			setSegmentsArray(SegmentsObj);

			JsonArray waypointsArray = new JsonArray();

			// System.out.println("ARRAY SIZE: " + getTextArray().size()
			// + "exception: " + exception);

			for (int i = exception + 2; i < getTextArray().size(); i++) {

				JsonObject waypointObj = new JsonObject();

				str_split = getTextArray().get(i).split(" ", 2);

				System.out.println("DBG: textArray " + getTextArray().get(i));

				waypointObj.addProperty("time", str_split[0]);
				waypointObj.addProperty("name", str_split[1]);

				if (!getTextArray().get(exception + 1).contains("Walking")
						&& !getTextArray().get(exception + 1).contains(
								"Kävelyä")
						&& !getTextArray().get(exception + 1).contains("Gång")) {

					// System.out.println("STRING: " + str_split[0] + " "+
					// str_split[1]);
					String stopCode;
					int start, end;
					// start = str_split[1].indexOf("(") + 1;
					// end = str_split[1].indexOf(")");

					/*
					 * Getting the last parentheses from the string because
					 * usually is where is the stop code
					 */

					start = str_split[1].lastIndexOf("(") + 1;
					end = str_split[1].lastIndexOf(")");

					if (start == -1 || end == -1) {
						throw new ParseException(
								"JsonParser: No stopCode:"+mCurrentLine, mCurrentLineLoc);
					}

					stopCode = str_split[1].substring(start, end);

					waypointObj.remove("name");
					waypointObj.addProperty("name",
							str_split[1].substring(0, start - 1));
					waypointObj.addProperty("stopCode", stopCode);
				}

				waypointsArray.add(waypointObj);
			}

			SegmentsObj.add("waypoints", waypointsArray);

		} else {
			// Last line of the file, "Arrival" line

			// System.out.println("ARRAY " + this.getSegmentsArray());
			addProperty("dest", str_split[1]);
			addProperty("arrivalTime", str_split[0]);
			addProperty("segments", this.getSegmentsArray());
		}
	}

	/**
	 * Augment journey object with a newly encountered journey text line. The
	 * newly read encountered line should had been passed to
	 * {@link #setTxtLine(String)} and {@link #incrementLine()} should be called
	 * before.
	 * 
	 * @throws java.text.ParseException from {@link #addJsonObject()}
	 * 
	 * @see #addJsonObject()
	 */
	private void organizeJson() throws ParseException {

		System.out.println("DBG organizeJson loc:" + mCurrentLineLoc + " arraySz:"
				+ _textArray.size() + " line:" + mCurrentLine);

		switch (mCurrentLineLoc) {
		case 1:
			addJsonObject();
			return; // Add to json object the key value "date"
		case 2:
			return; // "Departure" line
		case 3:
			addJsonObject(); // Add to json object the key value "start"
			break;
		default:
			break;
		}

		/**
		 * isNotBlank - Checks if a String is not empty (""), not null and not
		 * whitespace only
		 */
		if (mCurrentLine.trim().equals("")) {
			// The line is blank
			addJsonObject();
			flushTextArray();
			return;
		} else {
			addTextArray(mCurrentLine);
		}

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
		return _mainObj.toString();
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
