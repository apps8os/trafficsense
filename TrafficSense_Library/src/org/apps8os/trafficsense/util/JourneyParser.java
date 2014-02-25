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

public class JourneyParser {
	private JsonObject _mainObj; // Declare the JSON object
	private JsonArray _segmentsArray;

	private ArrayList<String> _textArray = new ArrayList<String>();
	private int _nline = 0; // Line number of txt file
	private String _txtLine; // the line read it from the txt

	public JourneyParser() {
		_mainObj = new JsonObject();
		_segmentsArray = new JsonArray();
	}

	private int getLine() {
		return _nline;
	}

	private void incrementLine() {
		_nline++;
	}

	private String getTxtLine() {
		return _txtLine;
	}

	private void setTxtLine(String txtLine) {
		_txtLine = txtLine;
	}

	private ArrayList<String> getTextArray() {

		return _textArray;
	}

	private void flushTextArray() {

		this.getTextArray().clear();
	}

	private void addTextArray(String line) {
		// trim removes the white space at the beginning and end
		this.getTextArray().add(line.trim());
	}

	public JsonObject getJsonObj() {
		return _mainObj;
	}

	private void setJsonObj(String key, String value) {
		_mainObj.addProperty(key, value);
	}

	private void setJsonObj(String key, JsonElement element) {
		_mainObj.add(key, element);
	}

	private JsonArray getSegmentsArray() {
		return _segmentsArray;
	}

	private void setSegmentsArray(JsonElement value) {
		_segmentsArray.add(value);
	}

	private void addJsonObject() {

		if (this.getLine() == 1) {
			this.setJsonObj("date", this.getTxtLine());
			return;
		}

		if (this.getLine() == 3) {
			String[] parts = this.getTxtLine().split(" ", 2);
			this.setJsonObj("start", parts[1]);
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
			this.setJsonObj("dest", str_split[1]);
			this.setJsonObj("arrivalTime", str_split[0]);
			this.setJsonObj("segments", this.getSegmentsArray());
		}
	}

	/**
	 * Will add the different objects and arrays to JSON file
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

	public String getJsonText() {
		return _mainObj.toString();
	}

	public void writeJsonFile(String outFileName) {
		try {

			FileWriter file = new FileWriter(outFileName);
			file.write(this.getJsonObj().toString());
			file.flush();
			file.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void parseOneLine(String line) {
		System.out.println("Line: "+line);
		this.setTxtLine(line);
		this.incrementLine(); // will increment a read line on txt file, counter
								// starts 0
		this.organizeJson(line);
	}

	/* parse a journey supplied in a String */
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

	/* parse a journey in a text file */
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
