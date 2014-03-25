package org.apps8os.trafficsense.util;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.PatternSyntaxException;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Class for parsing plain text journey into a Gson JSON object.
 *
 * JSON object is organize as follow :
 * 
 *"_mainObj"(JsonObject) contains :
 *	Property "date"
 * 	Property "start"
 *	Property "dest"
 *	Property "arrivalTime"
 *	
 *	Property "segments" that is JsonArray called "_segmentsArray" containing:
 * 		"SegmentsObj"(JsonObject) contains:	
 * 			Property "startTime"
 * 			Property "startPoint"
 *			Property "mode"			
 *			
 *			Property "waypoints" that is JsonArray called "waypointsArray" containing:
 *				"waypointObj"(JsonObject ) contains:
 *					Property "time"
 *					Property "name"					
 *					Property "stopCode" (!walking) 
 *
 * @see #parseString(String)
 */

public class JourneyParser {	
	
	/** The Gson JSON object for the current journey. */
	private JsonObject _mainObj;

	private JsonArray _segmentsArray;

	
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
	 * Get the line at this moment 
	 * 
	 * @return the line at this moment 
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
	 * @param txtLine
	 *            the journey text line.
	 * @see #parseOneLine(String)
	 */
	private void setTxtLine(String txtLine) {
		// TODO: should we check if the line is empty here?
		_txtLine = txtLine;
	}

	/**
	 * TODO: documentation
	 * 
	 * TODO: this method is only used internally in this class and on most
	 * occasion the result is .get()-ed. Is this level of abstraction really
	 * necessary? TODO: Or maybe refactor it to return i-th element directly,
	 * after some checks?
	 * 
	 * @return
	 */
	private ArrayList<String> getTextArray() {

		return _textArray;
	}

	/**
	 * Clear all the elements of the text array 
	 * that is a segment of 
	 * 
	 * @see #getTextArray()
	 */
	private void flushTextArray() {

		getTextArray().clear();
	}

	/**
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
	 * 
	 * 
	 * 
	 * */
	
	private boolean areEqual(String s1,String s2){
		
		Collator collator = Collator.getInstance();
		collator.setStrength(Collator.SECONDARY);
		
		if(collator.compare(s1, s2) == 0){
			System.out.println("EQUAL: " + s1 +" = " + s2);
			return true;
		}
		else{
			System.out.println("NOT EQUAL: " + s1 +" != " + s2);
			return false;
		}
	}
	
	
	/**
	 * Will add the elements to the JSON object
	 * 
	 * General Structure :
	 * 	line 1 add the time 
	 * 	line 2 is always the string "Departure" do nothing
	 * 
	 * 	line 3 until  second from the last is always segments and have the  
	 * following struct:
	 *	Time and location
	 * 	Transportation mode (e.g Walking Bus etc)
	 * 	Time location (several lines)
	 *  Stop condition is through a blank line
	 * 
	 * Exception Conditions from the above segments structure:
	 * 		1) Two first lines can be equal and only third line has the mode
	 * 			
	 * Last line always string "Arrival" || "Perillä"(FIN lang) || "Ankomst"(SWE lang)
	 * 
	 * @see #organizeJson(String)
	 */
	private void addJsonObject() {

		
		System.out.println("DBG addJsonObject nline:" + _nline + " arraySz:"
				+ _textArray.size() + " txtLine:" + _txtLine);
		
		// TODO: Try and catch exception
		
		/* temporary array string, store the the line of a segment 
			from the text array in two parts: time and location */
			String[] str_split = null; 
			int exception = 0 ; // Number of the exception case. 0 Normal case
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
		
		System.out.println("************************");
		System.out.println("************************");
		System.out.println("*** ADD JSON OBJECT ****");
		System.out.println("************************");
		System.out.println("************************");
		
		
		
		if(getTextArray().get(1).contains(":")){
			// We are in an exception case



			temp1 = getTextArray().get(0).split(" ",2) ;
			temp2 = getTextArray().get(1).split(" ",2);
			//System.out.println("TEXT ARRAY: " + getTextArray().get(0) );

				System.out.println("TEMP1: " + temp1[1]);
				System.out.println("TEMP2: " + temp2[1]); 
				int q = temp1[1].compareTo(temp2[1]) ;
				System.out.println("Q: " + q);
			// Exception case number 1
			if(  q == 0){
				// split the string in two parts through a " "

				str_split = getTextArray().get(1).split(" ", 2);
				exception = 1;
				System.out.println("EXCEPTION: " + exception);


			}
		}else{
				// Normal case
				System.out.println("ENTREI: " +  getTextArray().get(0));
				try{
					str_split = getTextArray().get(0).split(" ", 2); 
					//System.out.println("NORMAL CASE: " + exception + "->" + str_split[1]);

				}catch(Exception e){
					System.out.println("Dobtttt");
					e.printStackTrace();
				}	



			}
		
	
		System.out.println("SPLIT");
		/**
		 * TODO: refactor! 1. Check if _textArray.size() == 0 2. If not, .get(0)
		 * and split() 3. What if split() gives something other than expected??
		 */
		

		// TODO: check _textArray.size() >= 2 first!
		
//		if (!getTextArray().get(1).equals("Arrival")
//				&& !getTextArray().get(1).equals("Perill√§")
//				&& !getTextArray().get(1).equals("Ankomst")) {
			
		if ( (! this.areEqual(getTextArray().get(1), "Arrival")) &&
			 (!this.areEqual(getTextArray().get(1), "Perillä")) &&
			 (!this.areEqual(getTextArray().get(1), "Ankomst")) ){
		
			// TODO: check str_split[] first!
			
			JsonObject SegmentsObj = new JsonObject();
			SegmentsObj.addProperty("startTime", str_split[0]);
			SegmentsObj.addProperty("startPoint", str_split[1]);
			
			System.out.println("vou adicionar a propriedade");
		
			System.out.println("CASE:" + exception);
			switch(exception) {
			case 0: SegmentsObj.addProperty("mode", getTextArray().get(1));
					break;
			case 1: SegmentsObj.addProperty("mode", getTextArray().get(2)); 
			
			default: //TODO Create an exception; 
					break;
			}
			
			System.out.println("ADICIONEIIIIIII");
			
			setSegmentsArray(SegmentsObj);

			System.out.println("SETTTTTT");
			
			JsonArray waypointsArray = new JsonArray();

			// TODO: what to do if .size() = 0 ?
			System.out.println("ARRAY SIZE: "+ getTextArray().size() + "exception: " + exception );
			for (int i = exception+2; i < getTextArray().size(); i++) {
				System.out.println("DENTRO DO FOR");
				JsonObject waypointObj = new JsonObject();

				str_split = getTextArray().get(i).split(" ", 2);

				System.out.println("oi: " + str_split[0] + " "+ str_split[1]);

				System.out.println("DBG: textArray "+ getTextArray().get(i));

				waypointObj.addProperty("time", str_split[0]);
				waypointObj.addProperty("name", str_split[1]);
				System.out.println("oi2 ");
				
//				if (!getTextArray().get(exception + 1).contains("Walking")
//						&& !getTextArray().get(exception + 1).contains("K√§vely√§")
//						&& !getTextArray().get(exception + 1).contains("G√•ng")) {
					
				if (!getTextArray().get(exception + 1).contains("Walking")
						&& !getTextArray().get(exception + 1).contains("K√§vely√§")
						&& !getTextArray().get(exception + 1).contains("Gång")) {
					
					
					
					// If the user is not walking will have a stopCode for each
					// point
					// TODO: This is _NOT_ the case ...
					
					System.out.println("STRING: " + str_split[0] + " "+ str_split[1]);
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
		System.out.println("*********************");
		System.out.println("*********************");
		System.out.println("FIM");
		System.out.println("*********************");
		System.out.println("*********************");
	}

	/**
	 * Augment journey object with a newly encountered journey text line. The
	 * newly read encountered line should had been passed to
	 * {@link #setTxtLine(String)} and {@link #incrementLine()} should be called
	 * before.
	 * 
	 * @see #addJsonObject()
	 */
	private void organizeJson() {

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
			// System.out.println("ARRAY: " + text);
			// System.out.println("FLUSH ********");
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
	 * @param line line to be parsed      
	 */
	private void parseOneLine(String line) {
		System.out.println("DBG parseOneLine: " + line);
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
		System.out.println("**************************");
		System.out.println("**************************");
		System.out.println("I AM HERE");
		System.out.println("**************************");
		System.out.println("**************************");
		Scanner scanner = new Scanner(jsonText);
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();
			parseOneLine(line);
			if (line.equals("Arrival") || line.equals("Perillä")
					|| line.equals("Ankomst")) {
				break;
			}
		}
		scanner.close();
	}

	/**********************************************************************
	 ***********************	DEPRECATED METHODS	***********************
	 **********************************************************************/
	
	/**
	 * Read and parse a plain text journey from a file.
	 * 
	 * @param fileName path to the file.
	 * @deprecated replaced by {@link #parseString(String)}
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
		}
	}
	
	/**
	 * Write parsed journey in JSON to a file.
	 * 
	 * @param outFileName
	 *            path to file.
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
}
