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
	 */
	private int _nline = 0;
	/**
	 * Next non-empty journey text line to be processed.
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
	 * @return
	 */
	private ArrayList<String> getTextArray() {

		return _textArray;
	}

	/**
	 * TODO: documentation
	 */
	private void flushTextArray() {

		this.getTextArray().clear();
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
	 * 
	 * @return
	 */
	private JsonArray getSegmentsArray() {
		return _segmentsArray;
	}

	/**
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

		if (this.getLine() == 1) {
			this.addProperty("date", this.getTxtLine());
			return;
		}

		if (this.getLine() == 3) {
			String[] parts = this.getTxtLine().split(" ", 2);
			this.addProperty("start", parts[1]);
			return;
		}

		// The str has the time and location and will split into two parts
		String str;
		String[] str_split;

		try {
			str = getTextArray().get(0);
			str_split = str.split(" ", 2); // will just split one time = 2 parts
		} catch (IndexOutOfBoundsException ex) {
			// this is an extra blank line, just ignore it
			return;
		}

		if (!getTextArray().get(1).equals("Arrival") &&
			!getTextArray().get(1).equals("Perillä") &&
			!getTextArray().get(1).equals("Ankomst")) {

			JsonObject SegmentsObj = new JsonObject();
			SegmentsObj.addProperty("startTime", str_split[0]);
			SegmentsObj.addProperty("startPoint", str_split[1]);
			SegmentsObj.addProperty("mode", getTextArray().get(1));

			this.setSegmentsArray(SegmentsObj);

			JsonArray waypointsArray = new JsonArray();

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
					String stopCode;
					int start, end;
					start = str_split[1].indexOf("(") + 1;
					end = str_split[1].indexOf(")");
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
			this.addProperty("dest", str_split[1]);
			this.addProperty("arrivalTime", str_split[0]);
			this.addProperty("segments", this.getSegmentsArray());
		}
	}

	/**
	 * Augment journey object with a newly encountered journey text line.
	 * 
	 * @param line journey text line encountered.
	 * @see #addJsonObject()
	 */
	private void organizeJson(String line) {

		switch (this.getLine()) {
		case 1:
			this.addJsonObject();
			return; // Add to json object the key value "date"
		case 2:
			return; // "Departure" line
		case 3:
			this.addJsonObject(); // Add to json object the key value "start"
			break;
		default:
			// TODO: what to do here?
			break;
		}

		/*
		 * isNotBlank - Checks if a String is not empty (""), not null and not
		 * whitespace only
		 */
		if (line.trim().equals("")) {
			// The line is blank
			this.addJsonObject();
			// System.out.println("ARRAY: " + text);
			// System.out.println("FLUSH ********");
			this.flushTextArray();
			return;
		} else {
			this.addTextArray(line);
		}

		if (this.getTxtLine().equals("Arrival") ||
			this.getTxtLine().equals("Perillä") ||
			this.getTxtLine().equals("Ankomst")) {

			this.addJsonObject();
			this.flushTextArray();
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
			file.write(this.getJsonObj().toString());
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
		System.out.println("Line: "+line);
		this.setTxtLine(line);
		this.incrementLine();
		this.organizeJson(line);
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
