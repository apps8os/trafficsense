package org.apps8os.trafficsense.core;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;



public class RequestThread implements Runnable{

	
	//private final String URI ="http://api.reittiopas.fi/hsl/prod/?";
	
	private final String USER = "trafficsenseuser";
	private final String PASS = "trafficsense";
	private final String FORMAT = "json";
	private final String EPSG_OUT = "4326";
	private final String EPSG_IN = "4326";
	//private String [] locTypes = {""};
	private final String [] LANG = {"fi", "sv", "en", "slangi"};
	private final int defLang = 2;
	//private int disableErrorCorr = 0;
	//private int disableUniqStopNames =  0;
	private String responseString = "";
	private String URL;
	//private String [] KEY;
	private HttpResponse response;
	private HttpClient httpclient = new DefaultHttpClient();
	
	@Override
	public void run() {
    	//for (int i = 0; i < KEY.length; i++) {
       		//String finalURL =URL +KEY[i];
			try {
				response = httpclient.execute(new HttpGet(URL));
				
				StatusLine statusLine = response.getStatusLine();
            	if(statusLine.getStatusCode() == HttpStatus.SC_OK){
            		ByteArrayOutputStream out = new ByteArrayOutputStream();
            		response.getEntity().writeTo(out);
            		out.close();
            		responseString = out.toString();            
            	} 
            	else{
            		System.out.println("Something failed.");
            		//Closes the connection.
            		response.getEntity().getContent().close();
                	throw new IOException(statusLine.getReasonPhrase());
            	}
			} catch (ClientProtocolException e) {e.getStackTrace();
			} catch (IOException e) {   e.getStackTrace();
        }
		//}
    //System.out.println("ResponseString is: "+ responseString);
	}


	public String getGeocoding(String responseLimit, String cities , String locType, String key) {
		URL = "http://api.reittiopas.fi/hsl/prod/?request=geocode"+ "&user=" + USER + "&pass=" + PASS + "&format=" + FORMAT 
		+"&loc_types=" + locType +"&epsg_out=" + EPSG_OUT + "&p=" + responseLimit + "&cities=" + cities + "&lang=" + LANG[defLang] + "&key=";			
			this.run();
			return responseString;
	}
	
	
	public String getLineInfo(String responseLimit, String query){
		URL = "http://api.reittiopas.fi/hsl/prod/?request=lines"+ "&user=" + USER + "&pass=" + PASS + "&format=" + FORMAT + 
				"&epsg_out=" + EPSG_OUT + "&p=" + responseLimit + "&query="+query;
		this.run();
		return responseString;
	}
	
	public String getReverseGeocoding (String responseLimit, String x, String y){
		URL= "http://api.reittiopas.fi/hsl/prod/?request=reverse_geocode"+ "&user=" + USER + "&pass=" + PASS + "&format=" + FORMAT + 
		"&key="+ "&coordinate=" + x +","+ y +"&epsg_in=" + EPSG_IN + "&epsg_out=" + EPSG_OUT + responseLimit;
		this.run();
		return responseString;	
	}
	
	public String getStopInfo(String responseLimit, String stopCode){
		URL = "http://api.reittiopas.fi/hsl/prod/?request=stop"+ "&user=" + USER + "&pass=" + PASS + "&format=" + FORMAT + 
				"&epsg_out=" + EPSG_OUT + "&p=" + responseLimit + "&code="+stopCode;
		this.run();
		return responseString;		
	}
	
	
}
