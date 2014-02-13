package com.aalto.hslapitest;

import java.util.HashMap;

import android.content.Context;

public class Request {
	
	private Context mContext;
	private String responseString;
	
	
	private final String URI ="http://api.reittiopas.fi/hsl/prod/?";
	
	private final String USER = "trafficsenseuser";
	private String PASS = "trafficsense";
	private String FORMAT = "json";
	private String EPSG_OUT = "4326";
	private String EPSG_IN = "4326";
	private String [] locTypes = {""};
	private String [] LANG = {"fi", "sv", "en", "slangi"};
	private int defLang = 2;
	private int disableErrorCorr = 0;
	private int disableUniqStopNames =  0;
   
    
   


   public void setContext (Context x){
		mContext = x;
   	}
   
   ;

	public void getGeocoding(String responseLimit, String cities , String key) {
	
			String url = "http://api.reittiopas.fi/hsl/prod/?request=geocode"+ "&user=" + USER + "&pass=" + PASS + "&format=" + FORMAT + 
					"&key="+ key +"&epsg_out=" + EPSG_OUT + responseLimit + "&cities=" + cities + "&lang=" + LANG[defLang];
			System.out.println(url);
			RequestTask task = new RequestTask(this.mContext); 
			task.execute(url);	
	}
	
   
	
	public void getReverseGeocoding (String responseLimit, String x, String y){
			String url = "http://api.reittiopas.fi/hsl/prod/?request=reverse_geocode"+ "&user=" + USER + "&pass=" + PASS + "&format=" + FORMAT + 
					"&key="+ "&coordinate=" + x +","+ y +"&epsg_in=" + EPSG_IN + "&epsg_out=" + EPSG_OUT + responseLimit;
			RequestTask task = new RequestTask(this.mContext); 
			task.execute(url);
		
	}

	public void getStopInfo (String responseLimit, String stopCode){
			String url = "http://api.reittiopas.fi/hsl/prod/?request=stop"+ "&user=" + USER + "&pass=" + PASS + "&format=" + FORMAT + 
					"&key="+ "&code=" + stopCode +"&epsg_out=" + EPSG_OUT + responseLimit;	
			RequestTask task = new RequestTask(this.mContext); 
			task.execute(url);
		
	}

}
	

