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

class RequestTask extends AsyncTask <String, String, String> {
	
	String responseString = null;
	private Context mContext;
	HttpResponse response;
	HttpClient httpclient = new DefaultHttpClient();
	
   
	
    @Override
    protected String doInBackground(String... uri) {
    	try {
        	System.out.println("Doing getRequest");
            response = httpclient.execute(new HttpGet(uri[0]));
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
            e.getStackTrace();
        } catch (IOException e) {
            e.getStackTrace();
        }
      
        //saveToFile();
        return responseString;
    }
        
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
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        
        
        
    }
    
    protected void onProgressUpdate(){
    	
    }
}