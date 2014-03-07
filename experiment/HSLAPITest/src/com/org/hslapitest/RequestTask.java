package com.org.hslapitest;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Dialog;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.TextView;

public class RequestTask extends AsyncTask <String, String, String> {
	
	String responseString = null;
	private Context mContext;
	HttpResponse response;
	HttpClient httpclient = new DefaultHttpClient();
	private AsyncResponse listener;

	
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
	
	
	public RequestTask(AsyncResponse listener){
        this.listener=listener;
    }
	
    @Override
    protected String doInBackground(String...params) {
    	String uri = (String) params [0];
    	try {
    		responseString = null;
        	//System.out.println("Doing getRequest " + uri);
            response = httpclient.execute(new HttpGet(uri));
            StatusLine statusLine = response.getStatusLine();
            	if(statusLine.getStatusCode() == HttpStatus.SC_OK){
     
            		ByteArrayOutputStream out = new ByteArrayOutputStream();
            		response.getEntity().writeTo(out);
            		out.close();
            		responseString = out.toString();            
            		//System.out.println("result: " + responseString);
            	} 
            	else{
            		System.out.println("Something failed.");
                //Closes the connection.
                response.getEntity().getContent().close();
                throw new IOException(statusLine.getReasonPhrase());
            }
        } catch (ClientProtocolException e) {
            e.getStackTrace();
        } catch (IOException e) {
            e.getStackTrace();
        }
      
        //saveToFile();
    	System.out.println("ResponseString is: "+ responseString);
        return responseString;    }
        
       public void setContext (Context x){
    	 mContext = x;
       }
       
       /*
       public void saveToFile(){
    	   try {
    	    	File file = new File(Context.getFilesDir().getAbsolutePath());
    	    	System.out.println("path: " + file.getAbsolutePath().toString());
    	    	file.createNewFile();
    	    	FileWriter writer = new FileWriter(file.getAbsolutePath()+File.separator+"myFile2.json");
    	    	writer.write(responseString);
    	    	writer.close();
    	    	System.out.println(responseString);
    	    }
    	     catch(Exception e) {
    	    	 e.printStackTrace();
    	     }  
    }*/
    	

    @Override
    protected void onPostExecute(String data) {
       listener.returnInfo(data);    
    }
   
    
    protected void onProgressUpdate(){
    	
    }
    
    
    public void getStopInfo (String responseLimit, String stopCode){
		String url = "http://api.reittiopas.fi/hsl/prod/?request=stop"+ "&user=" + USER + "&pass=" + PASS + "&format=" + FORMAT + 
		"&epsg_out=" + EPSG_OUT + "&p=" + responseLimit + "&code=" + stopCode;
		//System.out.println(url);
		this.execute(url);
	
    }
}