package hsl;

import java.io.*;
import java.util.ArrayList;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.apache.commons.lang3.StringUtils;




public class JSON {

	
	private JSONObject _mainObj ; // Declare the JSON object 
	private JSONArray _segmentsArray;
	
	
	private ArrayList<String> _textArray = new ArrayList<String>();
	private int _nline = 0 ; // Line number of txt file 
	private String _txtLine ; // the line read it from the txt
 
	public JSON() {
		super();
		_mainObj = new JSONObject(); 
		_segmentsArray = new JSONArray();
		
	}
	
	
	private int getLine(){
		return _nline ;
	}
	
	private void incrementLine(){
		_nline++;
	}
	
	private String getTxtLine(){
		return _txtLine ;
	}
	
	private void setTxtLine(String txtLine){
		_txtLine = txtLine ;
	}
	
	private ArrayList<String> getTextArray(){
		
		return _textArray ;
	}
	
	private void flushTextArray(){			
		
		this.getTextArray().clear();
	}
	 
	
	private void addTextArray(String line){		
		// trim removes the white space at the beginning and end
		this.getTextArray().add(line.trim());
	}


	public JSONObject getJSONObj(){		
		return _mainObj ;
	}
	
	private void setJSONObj(Object key, Object value){		
		_mainObj.put(key, value);
	}
	
	private JSONArray getSegmentsArray(){
		return _segmentsArray;
	}
	
	private void setSegmentsArray(Object value){
		 _segmentsArray.add(value);
	}
	
	
		
	public void addJSONObject(){
		
		if(this.getLine() == 1){
			this.setJSONObj("date", this.getTxtLine());
			return;
		}
		
		if(this.getLine() == 3){
			
			String[] parts = this.getTxtLine().split(" ",2);
			this.setJSONObj("start",parts[1]);
			return;
		}
		
	
		// The str has the time and location and will split into two parts 
		String str = getTextArray().get(0);
		String[] str_split = str.split(" ",2); // will just split one time = 2 parts
		
		
		
		if(! getTextArray().get(1).equals("Arrival")){
		
			JSONObject SegmentsObj = new JSONObject();
			SegmentsObj.put("startTime", str_split[0]);
			SegmentsObj.put("startPoint", str_split[1]);
			SegmentsObj.put("mode", getTextArray().get(1));
			
			this.setSegmentsArray(SegmentsObj);
			
			JSONArray waypointsArray = new JSONArray();	
				
			for(int i=2 ; i < getTextArray().size() ; i++) {
	
				JSONObject waypointsObj = new JSONObject();
				
				str_split = getTextArray().get(i).split(" ",2);
				waypointsObj.put("time",str_split[0]);
				waypointsObj.put("name",str_split[1]);
				
				
				if(! getTextArray().get(1).contains("Walking")){
					// If the user is not walking will have a stopCode for each point
				;
					String stopCode ;
					int start,end;
					start = str_split[1].indexOf( "(" )+1;
					end = str_split[1].indexOf( ")" );
					stopCode = str_split[1].substring(start,end); 
							
					waypointsObj.remove("name");
					waypointsObj.put("name",str_split[1].substring(0, start-1));
					waypointsObj.put("stopCode",stopCode);
				}
				
				waypointsArray.add(waypointsObj);
			}
			
			SegmentsObj.put("waypoints", waypointsArray);
			
		}else{
			// Last line of the file, "Arrival" line 
			
			//System.out.println("ARRAY " + this.getSegmentsArray());
			this.setJSONObj("dest",str_split[1]);
			this.setJSONObj("arrivalTime",str_split[0]);
	
			this.setJSONObj("segments", this.getSegmentsArray());
			System.out.println("OBJECT" + this.getJSONObj());
		}
	}
	
	
	
	/**
	 *Will add the different objects and arrays to JSON file
	 */
	
	public void organizeJSON(String line){
		
		
		switch(this.getLine()){
		
		case 1 : this.addJSONObject();return; // Add to json object the key value "date"
		case 2: return; // "Departure" line
		case 3: this.addJSONObject(); // Add to json object the key value "start"
		}
			
		
		/* isNotBlank - Checks if a String is not empty (""), not null and not whitespace only
		 *  
		 */
			if(!StringUtils.isNotBlank(line)){
				// The line is blank 
				this.addJSONObject();
				//System.out.println("ARRAY: " + text);
				//System.out.println("FLUSH ********");
				this.flushTextArray();
				return;
			}else{
				this.addTextArray(line);
			}
			
			if( this.getTxtLine().equals("Arrival")) {
				this.addJSONObject();
				this.flushTextArray();
				// End of the txt file and will write to the json file 
				this.writeJSONFile(); 
				return;
			}
	
	}
	
	
	public void writeJSONFile(){
		
		try {
			 
			FileWriter file = new FileWriter("/Users/catarinamoura/Desktop/trafficsense/JSON/hsl/src/hsl/itenerary.json");
			file.write(this.getJSONObj().toJSONString());
			file.flush();
			file.close();
	 
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	/**
	 * The parsing method will parse the txt hsl file 
	 */
	
	public void parsingFile(String fileName){
       
		//Path to txt file 
        //String fileName = "/Users/catarinamoura/Desktop/trafficsense/experiment/json/sample.txt";
    	
        // This will reference one line at a time
        String line = null;
        
        try {
            
        	// FileReader reads text files in the default encoding and wrap FileReader in BufferedReader.
            BufferedReader bufferedReader =  new BufferedReader(new FileReader(fileName));
          

            
            while((line = bufferedReader.readLine()) != null) {
            	
            	this.setTxtLine(line);
            	this.incrementLine(); // will increment a read line on txt file, counter starts 0
            	
            	this.organizeJSON(line);
         
            	if(getTxtLine().equals("Arrival")){
            		// Document it was already all parsed 
            		break;
            	}
            	
            }	
             
           // System.out.println(this.getJSONObj());
            
            bufferedReader.close(); // Close the file			
        }
        catch(FileNotFoundException ex) {
            System.out.println("Unable to open file '" + fileName + "'");				
        }
        catch(IOException ex) {
            System.out.println("Error reading file '" + fileName + "'");					
            // ex.printStackTrace();   
        }
	}
}
