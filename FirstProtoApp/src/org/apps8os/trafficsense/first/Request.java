package org.apps8os.trafficsense.first;

import java.util.concurrent.ExecutionException;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

public class Request {
	
	private Context mContext;
	private String responseString;
	
	
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
			RequestTask task = new RequestTask(this.mContext); 				
				String myString=null;
				JSONObject json = null;
				try {
					myString = task.execute(url).get();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ExecutionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				try {
					json = new JSONObject(myString);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return json;
	}
	
   
	
	public JSONObject getReverseGeocoding (String responseLimit, String x, String y){
			String url = "http://api.reittiopas.fi/hsl/prod/?request=reverse_geocode"+ "&user=" + USER + "&pass=" + PASS + "&format=" + FORMAT + 
					"&key="+ "&coordinate=" + x +","+ y +"&epsg_in=" + EPSG_IN + "&epsg_out=" + EPSG_OUT + responseLimit;
			RequestTask task = new RequestTask(this.mContext); 
			String myString=null;
			JSONObject json = null;
			try {
				myString = task.execute(url).get();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				json = new JSONObject(myString);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return json;
}
	public void getStopInfo (String responseLimit, String stopCode){
			String url = "http://api.reittiopas.fi/hsl/prod/?request=stop"+ "&user=" + USER + "&pass=" + PASS + "&format=" + FORMAT + 
					"&key="+ "&code=" + stopCode +"&epsg_out=" + EPSG_OUT + responseLimit;	
			RequestTask task = new RequestTask(this.mContext); 
			task.execute(url);
		
	}

}
	

