package com.aalto.hslapitest;

import java.util.concurrent.ExecutionException;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

public class Request {
	
	private Context mContext;
	private final String URI ="http://api.reittiopas.fi/hsl/prod/?";
	
	private final String USER = "trafficsenseuser";
	private final String PASS = "trafficsense";
	private final String FORMAT = "json";
	private final String EPSG_OUT = "4326";
	private final String EPSG_IN = "4326";
	private String [] locTypes = {""};
	private final String [] LANG = {"fi", "sv", "en", "slangi"};
	private final int defLang = 2;
	private int disableErrorCorr = 0;
	private int disableUniqStopNames =  0;
   
    
   


   public void setContext (Context x){
		mContext = x;
   	}
   
   ;

	public JSONObject getGeocoding(String responseLimit, String cities , String key, String locType) {
		String url = "http://api.reittiopas.fi/hsl/prod/?request=geocode"+ "&user=" + USER + "&pass=" + PASS + "&format=" + FORMAT + 
				"&key="+ key +"&epsg_out=" + EPSG_OUT + "&p=" + responseLimit + "&cities=" + cities + "&lang=" + LANG[defLang] + "&loc_types=" + locType;
		RequestTask task = new RequestTask(); 				
			task.execute(url);	
			return null;
	}
	
   
	
	public void getReverseGeocoding (String responseLimit, String x, String y){
			String url = "http://api.reittiopas.fi/hsl/prod/?request=reverse_geocode"+ "&user=" + USER + "&pass=" + PASS + "&format=" + FORMAT + 
					"&key="+ "&coordinate=" + x +","+ y +"&epsg_in=" + EPSG_IN + "&epsg_out=" + EPSG_OUT + responseLimit;
			RequestTask task = new RequestTask(); 
			task.execute(url);
		
	}

	public void getStopInfo (String responseLimit, String stopCode){
			String url = "http://api.reittiopas.fi/hsl/prod/?request=stop"+ "&user=" + USER + "&pass=" + PASS + "&format=" + FORMAT + 
					"&key="+ "&code=" + stopCode +"&epsg_out=" + EPSG_OUT + responseLimit;	
			RequestTask task = new RequestTask(); 
			task.execute(url);
		
	}
	
	public void getLineInfo(String lineName){
		
	}

}
	

