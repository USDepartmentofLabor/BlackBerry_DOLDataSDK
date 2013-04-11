package gov.data.api;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;

import gov.data.example.GOVExtendDataMainScreen;


public class GOVExtendData extends Thread {
    	private String url;
	    private String method; // GET or POST
	    private GOVExtendDataMainScreen screen;
	    
	   public GOVExtendData(String url, String method, GOVExtendDataMainScreen screen) {
	           this.setUrl(url);
	           this.setMethod(method);
	           this.setScreen(screen);
	}
	   
	 public void run() {
		             try {
		            	 HttpConnection connection = (HttpConnection)Connector.open(url);
		            	                connection.setRequestMethod(method);
		            	                
		            	                int responseCode = connection.getResponseCode();
		            	                    if(responseCode != HttpConnection.HTTP_OK) {
		            	                    	screen.requestFailed("Unexpected response code: " + responseCode);
		            	                    	connection.close();
		            	                    	return;
		            	                    }
		            	                    
		            	                String contentType = connection.getHeaderField("Content-type");
		            	                ByteArrayOutputStream baos = new ByteArrayOutputStream();
		            	                
		            	                
		            	                InputStream responseData = connection.openInputStream();
		            	                byte[] buffer = new byte[1000]; 
		            	                int bytesRead = responseData.read(buffer);
		            	                 while(bytesRead > 0) {
		            	                	 baos.write(buffer, 0, bytesRead);
		            	                	 bytesRead = responseData.read(buffer);
		            	                 }
		            	                  baos.close();
		            	                  connection.close();
		            	                  
		            	                  screen.requestSucceeded(baos.toByteArray(), contentType);
		            	                  
		                     } catch (IOException ex) {
		            	                  screen.requestFailed(ex.toString());
		                     }
		             
		             
	       }
	     
	          public String getUrl() {
		                 return url;
	              }
	          
	          public void setUrl(String url) {
		                 this.url = url;
	             }
	          
	          public String getMethod() {
		                 return method;
	            }
	          
	          public void setMethod(String method) {
		                 this.method = method;
	           }
	          
	          public GOVExtendDataMainScreen getScreen() {
		                  return screen;
	          }
	          
	          public void setScreen(GOVExtendDataMainScreen screen) {
	                    	this.screen = screen;
	               }
	
	
	
}
