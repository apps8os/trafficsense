package com.org.hslapitest;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;

public class Request implements AsyncResponse{
	
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
	
	JSONObject json = null;
	JSONArray jarray = null;
	
   
    
   


   public void setContext (Context x){
		mContext = x;
   	}
   
   ;

	public JSONObject getGeocoding(String responseLimit, String cities , String key, String locType) {
		
		String url = "http://api.reittiopas.fi/hsl/prod/?request=geocode"+ "&user=" + USER + "&pass=" + PASS + "&format=" + FORMAT 
		+"&loc_types=" + locType +"&epsg_out=" + EPSG_OUT + "&p=" + responseLimit + "&cities=" + cities + "&lang=" + LANG[defLang] + "&key=";
		RequestTask task = new RequestTask(this); 				
			task.execute(url, key);	
			return null;
	}
	
   
	
	public void getReverseGeocoding (String responseLimit, String x, String y){
			String url = "http://api.reittiopas.fi/hsl/prod/?request=reverse_geocode"+ "&user=" + USER + "&pass=" + PASS + "&format=" + FORMAT + 
					"&key="+ "&coordinate=" + x +","+ y +"&epsg_in=" + EPSG_IN + "&epsg_out=" + EPSG_OUT + responseLimit;
			RequestTask task = new RequestTask(this); 
			task.execute(url, x, y);
		
	}

	public void getStopInfo (String responseLimit, String stopCode){
			String url = "http://api.reittiopas.fi/hsl/prod/?request=stop"+ "&user=" + USER + "&pass=" + PASS + "&format=" + FORMAT + 
			"&epsg_out=" + EPSG_OUT + "&p=" + responseLimit + "&code=" + stopCode;
			System.out.println(url);
			RequestTask task = new RequestTask(this); 
			task.execute(url);
		
	}
	
	public void getRoute (String lineName){
		
	}

	@Override
	public void returnInfo(String info) {	
		
	}

}
	

