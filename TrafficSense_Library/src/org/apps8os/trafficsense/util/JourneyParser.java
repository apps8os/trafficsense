package org.apps8os.trafficsense.util;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import org.apps8os.trafficsense.exceptions.InvalidCase;
import org.apps8os.trafficsense.exceptions.StopCodeInvalidParser;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Class for parsing plain text journey into a Gson JSON object.
 * 
 * The parser accepts the languages english, finnish and swedish
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

	/** The Gson JSON object for the current journey. */
	private JsonObject _mainObj;

	private JsonArray _segmentsArray;

	/*
	 * Temporary Array stores the values of each segment
	 * (startTime,startPoint,mode,waypoints)
	 */
	private ArrayList<String> _textArray = new ArrayList<String>();

	/**
	 * Attribute to keep track in which line is the file
	 */
	private int _nline = 0;

	/**
	 * Next non-empty journey text line to be processed. TODO: refactor to
	 * mCurrentLine
	 */
	private String _txtLine;

	/**
	 * Class constructor
	 */
	public JourneyParser() {
		_mainObj = new JsonObject();
		_segmentsArray = new JsonArray();
	}

	/**
	 * Get the number of the line at this moment
	 * 
	 * @return the line number
	 */
	private int getLine() {
		return _nline;
	}

	/**
	 * Increment the number of lines of journey processed.
	 */
	private void incrementLine() {
		_nline++;
	}

	/**
	 * Return text line of the moment
	 * 
	 * @return text line
	 */
	private String getTxtLine() {
		return _txtLine;
	}

	/**
	 * Set the text line of the moment
	 * 
	 * @param txtLine
	 *            the journey text line.
	 * @see #parseOneLine(String)
	 */
	private void setTxtLine(String txtLine) {
		_txtLine = txtLine;
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
	 * @see #organizeJson(String)
	 */
	private void addJsonObject() throws InvalidCase,StopCodeInvalidParser,NullPointerException {

		System.out.println("DBG addJsonObject nline:" + _nline + " arraySz:"
				+ _textArray.size() + " txtLine:" + _txtLine);


		/*
		 * temporary array string, store the the line of a segment from the text
		 * array in two parts: time and location
		 */
		String[] str_split = null;
		int exception = 0; // Number of the exception case. 0 Normal case
		String[] temp1, temp2; // Temporaries variables

		if (getLine() == 1) {
			addProperty("date", getTxtLine());
			return;
		}

		if (this.getLine() == 3) {
			String[] parts = getTxtLine().split(" ", 2);
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

		default:throw new InvalidCase("JourneyParser: Invalid case at the segement structure");

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
				start = str_split[1].indexOf("(") + 1;
				end = str_split[1].indexOf(")");

				if (start == -1 || end == -1) {
					throw new StopCodeInvalidParser("JsonParser: Does not exist a stopCode in the line:" + 
							"\n"+ getTxtLine() + " (line number:" + getLine() + ")" );
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
 * @see #addJsonObject()
 */
private void organizeJson() throws InvalidCase,StopCodeInvalidParser {

	System.out.println("DBG organizeJson nline:" + _nline + " arraySz:"
			+ _textArray.size() + " txtLine:" + _txtLine);

	switch (getLine()) {
	case 1:
		addJsonObject();
		return; // Add to json object the key value "date"
	case 2:
		return; // "Departure" line
	case 3:
		addJsonObject(); // Add to json object the key value "start"
		break;
	default: break;
	}

	/*
	 * isNotBlank - Checks if a String is not empty (""), not null and not
	 * whitespace only
	 */
	if (getTxtLine().trim().equals("")) {
		// The line is blank
		addJsonObject();
		flushTextArray();
		return;
	} else {
		addTextArray(getTxtLine());
	}

	if (getTxtLine().equals("Arrival") || getTxtLine().equals("Perillä")
			|| getTxtLine().equals("Ankomst")) {

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
 * @param line
 *            line to be parsed
 */
private void parseOneLine(String line) throws InvalidCase,StopCodeInvalidParser {
	System.out.println("DBG parseOneLine: " + line);
	setTxtLine(line);
	incrementLine();
	organizeJson();
}

/**
 * Parse a journey supplied in a String with line breaks.
 * 
 * @param jsonText
 *            plain text journey with line breaks.
 */
public void parseString(String jsonText){

	Scanner scanner = new Scanner(jsonText);

	while (scanner.hasNextLine()) {

		String line = scanner.nextLine();
		try {
			parseOneLine(line);
		}catch(InvalidCase e){
			System.out.println(e.getMessage());
		} 
		catch (StopCodeInvalidParser e) {
			System.out.println(e.getMessage());
		}
		catch(NullPointerException e){
			System.out.println("JsonParser: "+ e.getMessage());
		}
		catch (Exception e) {
			System.out.println("JsonParser: " + e.getMessage());
		}
		if (line.equals("Arrival") || line.equals("Perillä")
				|| line.equals("Ankomst")) {
			/* Reach the end of the plain text journey */
			break;
		}
	}
	scanner.close();
}

/**********************************************************************
 *********************** DEPRECATED METHODS ***********************
 **********************************************************************/

/**
 * Read and parse a plain text journey from a file.
 * 
 * @param fileName
 *            path to the file.
 * @deprecated replaced by {@link #parseString(String)}
 */
public void parsingFile(String fileName) throws InvalidCase {
	// This will reference one line at a time
	String line = null;

	try {
		// FileReader reads text files in the default encoding and wrap
		// FileReader in BufferedReader.
		BufferedReader bufferedReader = new BufferedReader(new FileReader(
				fileName));
		while ((line = bufferedReader.readLine()) != null) {
			parseOneLine(line);
			if (getTxtLine().equals("Arrival")
					|| getTxtLine().equals("Perillä")
					|| getTxtLine().equals("Ankomst")) {
				// Document it was already all parsed
				break;
			}
		}
		bufferedReader.close(); // Close the file
	} catch (FileNotFoundException ex) {
		System.out.println("Unable to open file '" + fileName + "'");
	} catch (IOException ex) {
		System.out.println("Error reading file '" + fileName + "'");
	} catch (StopCodeInvalidParser e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
}

/**
 * Write parsed journey in JSON to a file.
 * 
 * @param outFileName
 *            path to file.
 * @deprecated not need anymore
 * @see #parsingFile(String)
 */
public void writeJsonFile(String outFileName) {
	FileWriter file = null;
	try {
		file = new FileWriter(outFileName);
		file.write(getJsonObj().toString());
		file.flush();
		file.close();
	} catch (IOException e) {
		e.printStackTrace();
	}
}
}
