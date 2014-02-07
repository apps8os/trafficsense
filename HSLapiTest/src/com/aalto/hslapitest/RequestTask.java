package com.aalto.hslapitest;

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

import android.app.Dialog;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.TextView;

class RequestTask extends AsyncTask<String, String, String>{
	String responseString = null;
	private Context mContext;
	
    public RequestTask (Context context){
         mContext = context;
    }
	
    @Override
    protected String doInBackground(String... uri) {
    	GeocodingRequest geo = new GeocodingRequest();
    	
    	geo.setContext(mContext);
    	geo.getRequest();
    	
    	System.out.println("doing stuff");
    	responseString = geo.getRequest();
    	return responseString;
    	
    }
    	

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        
        
        
    }
    
    protected void onProgressUpdate(){
    	
    }
}