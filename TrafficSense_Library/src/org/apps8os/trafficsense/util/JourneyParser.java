package org.apps8os.trafficsense.util;


import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Class for parsing plain text journey into a Gson JSON object.
 */
public class JourneyParser {
	/**
	 * The Gson JSON object for the current journey.
	 */
	private JsonObject _mainObj;
	/**
	 * TODO: documentation
	 */
	private JsonArray _segmentsArray;
	/**
	 * TODO: documentation
	 */
	private ArrayList<String> _textArray = new ArrayList<String>();
	/**
	 * Number of lines of journey processed.
	 * TODO: refactor to mNumberOfLines
	 */
	private int _nline = 0;
	/**
	 * Next non-empty journey text line to be processed.
	 * TODO: refactor to mCurrentLine
	 */
	private String _txtLine;

	/**
	 * Constructor
	 */
	public JourneyParser() {
		_mainObj = new JsonObject();
		_segmentsArray = new JsonArray();
	}

	/**
	 * Return the number of lines of journey processed.
	 * 
	 * TODO: is this necessary?
	 * @return number of lines of journey processed.
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
	 * Return the next non-empty journey text line to be processed.
	 * 
	 * @return next non-empty journey text line to be processed.
	 */
	private String getTxtLine() {
		return _txtLine;
	}

	/**
	 * Set the next non-empty journey text line to be processed.
	 * 
	 * @param txtLine the journey text line.
	 * @see #parseOneLine(String)
	 */
	private void setTxtLine(String txtLine) {
		// TODO: should we check if the line is empty here?
		_txtLine = txtLine;
	}

	/**
	 * TODO: documentation
	 * 
	 * TODO: this method is only used internally in this class and
	 * on most occasion the result is .get()-ed.
	 * Is this level of abstraction really necessary?
	 * TODO: Or maybe refactor it to return i-th element directly,
	 * after some checks?
	 * @return
	 */
	private ArrayList<String> getTextArray() {

		return _textArray;
	}

	/**
	 * TODO: documentation
	 */
	private void flushTextArray() {

		getTextArray().clear();
	}

	/**
	 * TODO: documentation
	 * 
	 * @param line
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
	 * @param key key as a String.
	 * @param value value as a String.
	 */
	private void addProperty(String key, String value) {
		_mainObj.addProperty(key, value);
	}

	/**
	 * Add a JsonElement property field to journey object.
	 * 
	 * @param key key as a String.
	 * @param element value as a JsonElement.
	 */
	private void addProperty(String key, JsonElement element) {
		_mainObj.add(key, element);
	}

	/**
	 * TODO: documentation
	 * 
	 * @return
	 */
	private JsonArray getSegmentsArray() {
		return _segmentsArray;
	}

	/**
	 * TODO: documentation
	 * 
	 * @param value
	 */
	private void setSegmentsArray(JsonElement value) {
		_segmentsArray.add(value);
	}

	/**
	 * Add an element for newly encountered journey text line.
	 * 
	 * @see #organizeJson(String)
	 */
	private void addJsonObject() {

		System.out.println("DBG addJsonObject nline:"+_nline+" arraySz:"+_textArray.size()+" txtLine:"+_txtLine);
		
		if (getLine() == 1) {
			addProperty("date", getTxtLine());
			return;
		}

		if (this.getLine() == 3) {
			String[] parts = getTxtLine().split(" ", 2);
			// TODO: should check if |parts[]| >= 2 and fail gracefully.
			addProperty("start", parts[1]);
			return;
		}

		// The str has the time and location and will split into two parts
		String str;
		String[] str_split;

		/** 
		 * TODO: refactor!
		 * 1. Check if _textArray.size() == 0
		 * 2. If not, .get(0) and split()
		 * 3. What if split() gives something other than expected??
		 */
		try {
			str = getTextArray().get(0);
			str_split = str.split(" ", 2); // will just split one time = 2 parts
			System.out.println("DBG: "+str);
		} catch (IndexOutOfBoundsException ex) {
			// this is an extra blank line, just ignore it
			return;
		}

		// TODO: check _textArray.size() >= 2 first!
		if (!getTextArray().get(1).equals("Arrival") &&
			!getTextArray().get(1).equals("Perillä") &&
			!getTextArray().get(1).equals("Ankomst")) {

			//TODO: check str_split[] first!
			JsonObject SegmentsObj = new JsonObject();
			SegmentsObj.addProperty("startTime", str_split[0]);
			SegmentsObj.addProperty("startPoint", str_split[1]);
			SegmentsObj.addProperty("mode", getTextArray().get(1));

			setSegmentsArray(SegmentsObj);

			JsonArray waypointsArray = new JsonArray();

			// TODO: what to do if .size() = 0 ?
			for (int i = 2; i < getTextArray().size(); i++) {

				JsonObject waypointObj = new JsonObject();

				str_split = getTextArray().get(i).split(" ", 2);
				waypointObj.addProperty("time", str_split[0]);
				waypointObj.addProperty("name", str_split[1]);

				if (!getTextArray().get(1).contains("Walking") &&
					!getTextArray().get(1).contains("Kävelyä") &&
					!getTextArray().get(1).contains("Gång")) {
					// If the user is not walking will have a stopCode for each
					// point
					// TODO: This is _NOT_ the case ...
					String stopCode;
					int start, end;
					start = str_split[1].indexOf("(") + 1;
					end = str_split[1].indexOf(")");
					if (start == -1 || end == -1) {
						System.out.println("DBG addJsonObject start/end = -1");
					}
					// TODO: So this might crash
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
	 * Augment journey object with a newly encountered journey text line.
	 * The newly read encountered line should had been passed to
	 * {@link #setTxtLine(String)} and {@link #incrementLine()} should
	 * be called before.
	 * 
	 * @param line journey text line encountered.
	 * @see #addJsonObject()
	 */
	private void organizeJson() {

		System.out.println("DBG organizeJson nline:"+_nline+" arraySz:"+_textArray.size()+" txtLine:"+_txtLine);
		
		switch (getLine()) {
		case 1:
			addJsonObject();
			return; // Add to json object the key value "date"
		case 2:
			return; // "Departure" line
		case 3:
			addJsonObject(); // Add to json object the key value "start"
			break;
		default:
			// TODO: what to do here?
			break;
		}

		/*
		 * isNotBlank - Checks if a String is not empty (""), not null and not
		 * whitespace only
		 */
		if (getTxtLine().trim().equals("")) {
			// The line is blank
			addJsonObject();
			// System.out.println("ARRAY: " + text);
			// System.out.println("FLUSH ********");
			flushTextArray();
			return;
		} else {
			addTextArray(getTxtLine());
		}

		if (getTxtLine().equals("Arrival") ||
			getTxtLine().equals("Perillä") ||
			getTxtLine().equals("Ankomst")) {

			addJsonObject();
			flushTextArray();
		}

	}

	/**
	 * Return the parsed journey in JSON text.
	 * 
	 * @return parsed journey in JSON.
	 */
	public String getJsonText() {
		return _mainObj.toString();
	}

	/**
	 * Write parsed journey in JSON to a file.
	 * 
	 * @param outFileName path to file.
	 * @deprecated are we going to access sdcard ?
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

	/**
	 * Process one line of the journey.
	 * 
	 * @param line the line
	 */
	private void parseOneLine(String line) {
		System.out.println("DBG parseOneLine: "+line);
		setTxtLine(line);
		incrementLine();
		organizeJson();
	}

	/**
	 * Parse a journey supplied in a String with line breaks.
	 * 
	 * @param jsonText plain text journey with line breaks.
	 */
	public void parseString(String jsonText) {
		Scanner scanner = new Scanner(jsonText);
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();
			parseOneLine(line);
			if (line.equals("Arrival") ||
				line.equals("Perillä") ||
				line.equals("Ankomst")) {
				break;
			}
		}
		scanner.close();
	}

	/**
	 * Read and parse a plain text journey from a file.
	 * @param fileName path to the file.
	 * @deprecated are we going to access sdcard ?
	 */
	public void parsingFile(String fileName) {
		// This will reference one line at a time
		String line = null;

		try {
			// FileReader reads text files in the default encoding and wrap
			// FileReader in BufferedReader.
			BufferedReader bufferedReader = new BufferedReader(new FileReader(
					fileName));
			while ((line = bufferedReader.readLine()) != null) {
				parseOneLine(line);
				if (getTxtLine().equals("Arrival") ||
					getTxtLine().equals("Perillä") ||
					getTxtLine().equals("Ankomst")) {
					// Document it was already all parsed
					break;
				}
			}
			bufferedReader.close(); // Close the file
		} catch (FileNotFoundException ex) {
			System.out.println("Unable to open file '" + fileName + "'");
		} catch (IOException ex) {
			System.out.println("Error reading file '" + fileName + "'");
		}
	}

}
