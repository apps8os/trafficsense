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

import android.content.Context;

public class GeocodingRequest extends Request{
	Context mContext;
	String responseString;
	
    HttpClient httpclient = new DefaultHttpClient();
    HttpResponse response;
    String uri[] =new String [10];
   
    
    public String getRequest () {
    	
    try {
    	System.out.println("Doing getRequest");
        response = httpclient.execute(new HttpGet(this.URI));
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
    } catch (ClientProtocolException e) {
        //TODO Handle problems..
    } catch (IOException e) {
        //TODO Handle problems..
    }
  
    saveToFile();
    return responseString;
}
    
   public void setContext (Context x){
	 mContext = x;
   }
   
   public void saveToFile(){
	   try {
	    	File file = new File(mContext.getFilesDir().getAbsolutePath());
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
   }
	
}
