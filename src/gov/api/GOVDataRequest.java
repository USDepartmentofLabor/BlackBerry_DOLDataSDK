package gov.data.api;

import gov.data.util.GOVAPIUtils;
import gov.data.util.HttpConnectionFactory;
import gov.data.util.NoMoreTransportsException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.microedition.io.HttpConnection;

import net.rim.device.api.io.IOUtilities;
import net.rim.device.api.system.DeviceInfo;
import net.rim.device.api.ui.UiApplication;

public class GOVDataRequest {

	// instance variables
	private GOVDataRequestCallback callback;
	private GOVDataContext context;

	/**
	 * @return the context
	 */
	public GOVDataContext getContext() {
		return context;
	}

	/**
	 * @param context
	 *            the context to set
	 */
	public void setContext(GOVDataContext context) {
		this.context = context;
	}

	public GOVDataRequestCallback getCallback() {
		return callback;
	}

	/**
	 * Constructor
	 * 
	 * @param callback
	 * @param context
	 */
	public GOVDataRequest(GOVDataRequestCallback callback,
			GOVDataContext context) {
		super();
		this.callback = callback;
		this.context = context;
	}

	public void setCallback(GOVDataRequestCallback callback) {
		this.callback = callback;
	}

	public void callAPIMethod(String method, Hashtable arguments) {
		StringBuffer url = new StringBuffer(context.getApiHost()
				+ context.getApiURI() + "/" + method);
		
		    String login = "";
	        String longURL = "";
	     
	    StringBuffer query = new StringBuffer(); 
		StringBuffer queryString = new StringBuffer();
		StringBuffer queryData = new StringBuffer();
		
	    
		
		if (arguments != null) {
			Enumeration enumeration = arguments.keys();
			while (enumeration.hasMoreElements()) {
				String key = (String) enumeration.nextElement();
				longURL = (String) arguments.get(key);
				// Store value. URLEncode is needed.
				
				
				String value = GOVAPIUtils.urlEncode((String) arguments
						.get(key));
				
				  login  =  key.toString();
	                  
				   if(queryData.length() == 0) {
                       queryData.append("&");
                       queryData.append(key + "=" + value);
                      
                    }


				if (key.equals("top") || key.equals("skip")
						|| key.equals("select") || key.equals("orderby")
						|| key.equals("filter")) {
					// If it is the first parameter we append a ?, if not we
					// concatenate with &
					if (queryString.length() == 0) {
						queryString.append("?");
					} else {
						queryString.append("&");
					}
					// append the querystring key and value
					queryString.append("$" + key + "=" + value);
				}
			}
		}

		// If there are valid arguments then append it to the URL
		if (queryString.length() > 0) {
			url.append(queryString.toString());
		}
		
		 if (queryString.length() > 0) {
	         query.append(queryData.toString());
	 }
		
		// Enumerate the arguments and add them to the request
if (context.getApiHost().equalsIgnoreCase("http://api.dol.gov"))  {
			
    	// Make request to API. Will run on separate thread
	String dataset = null;
    
	try {
		 dataset = GOVAPIUtils.getRequestHeader(url.toString(),
				context.getApiHost(), context.getApiKey(),
				context.getApiSecret());
	} catch (final Exception e) {
		// Send error to callback
		 e.getMessage();
		 return;
	}
	 
    	 makeHTTPRequestAsync(dataset);
     

} else if(context.getApiHost().equalsIgnoreCase("http://go.usa.gov")) {
	
   	  String dataset =  context.getApiHost()  + context.getApiURI() + "/" + method + '?' + "login=" + login + "&apiKey=" + context.getApiKey() + "&longUrl=" + longURL;
     
     
    		 
    	  makeHTTPRequestAsync(dataset);
   
       
      
} else if(context.getApiHost().equalsIgnoreCase("http://www.ncdc.noaa.gov")) {
   	
	                 // NOAA National Climatic Data Center

 	  
                       String dataset =  context.getApiHost()  + context.getApiURI() + "/" + method + '?' + "token=" + context.getApiKey() + query.toString();
                       makeHTTPRequestAsync(dataset);
                	   
   	           }  else if (context.getApiHost().equalsIgnoreCase("http://api.eia.gov") || context.getApiHost().equalsIgnoreCase("http://developer.nrel.gov") || context.getApiHost().equals("http://api.stlouisfed.org") || context.getApiHost().equalsIgnoreCase("http://healthfinder.gov"))
       		
   	           {
   	        	
   	            
   	            // Energy EIA API (beta)
   	        	//		# Energy NREL
   	        	//		# St. Louis Fed
   	        	//		# NIH Healthfinder
   	            
   	            	 
   	        	   
   	        	   String  dataset = context.getApiHost()  + context.getApiURI() + "/" + method + '?' + "api_key=" + context.getApiKey() + query.toString();
   	        
   	           	makeHTTPRequestAsync(dataset);
   	    
   	           }  else if (context.getApiHost().equalsIgnoreCase("http://api.census.gov") || context.getApiHost().equalsIgnoreCase("http://pillbox.nlm.nih.gov")) {
	    	
	    	
		    //	# Census.gov
			//	# NIH Pillbox
		    
		    
 
           	 // Call go.usa.gov
           		   String dataset =   context.getApiHost()  + context.getApiURI() + "/" + method + '?' + "key=" + context.getApiKey() + query.toString();
           		   String data = dataset.toString();
           		
               		 
               	  makeHTTPRequestAsync(dataset);
               
              
           	 
     } else {
    	 
    	 
    	
    	    String data = url.toString();
    	    
    
    	    	  makeHTTPRequestAsync(data);
    	    	
     }
          
		
	}
		
	
	
			
			
		


	private void makeHTTPRequestAsync(final String url) {
		
		Runnable r = new Runnable() {
			public void run() {
				// Make the request
				HttpConnectionFactory factory = new HttpConnectionFactory(url,
						HttpConnectionFactory.TRANSPORTS_ANY);

				// Authorization Header
				String authHeader = "";
				if (context.getApiHost().equalsIgnoreCase("http://api.dol.gov"))  {
				try {
					authHeader = GOVAPIUtils.getRequestHeader(url,
							context.getApiHost(), context.getApiKey(),
							context.getApiSecret());
				} catch (final Exception e) {
					// Send error to callback
					UiApplication.getUiApplication().invokeLater(
							new Runnable() {
								public void run() {
									callback.GOVDataErrorCallback(e
											.getMessage());
								}
							});
					return;
				}
				} else {
					
					authHeader = url;
				}
					
				// Try all transports
				while (true) {
					try {
						String contentType ="" ;
						HttpConnection connection = factory.getNextConnection();
						try {
							// Set Headers
							connection.setRequestMethod("GET");
							if (context.getApiHost().equalsIgnoreCase("http://api.dol.gov")) { 
							connection.setRequestProperty("Accept",
									"application/json");
							
							}
							connection.setRequestProperty("Authorization",
									authHeader);

							InputStream is = connection.openInputStream();

							final byte[] data = IOUtilities.streamToBytes(is);

							if (is != null) {
								is.close();
							}

							// Check the response code
							
							contentType = connection.getHeaderField("Content-type");
							int responseCode = connection.getResponseCode();
							
							// Close the connection
							if (connection != null) {
								connection.close();
							}
							
							if (responseCode != 200) {
								String errorMessage;

								switch (responseCode) {
								case 401:
									errorMessage = "Unauthorized";
									break;
								case 400:
									errorMessage = "Bad Request";
									break;
								case 404:
									errorMessage = "Request not found";
									break;
								default:
									errorMessage = "Error " + responseCode
											+ " returned";
									break;
								}
								callbackWithError(errorMessage);
							}

							// Return results to the callback
							callbackWithResults(new String(data), contentType);
							break;
						} catch (IOException e) {
							// Log the error or store it for displaying to
							// the end user if no transports succeed
						}
					} catch (NoMoreTransportsException e) {
						// There are no more transports to attempt
						callbackWithError("Unable to perform request");
						break;
					}
				}
			}
		};
		// Start request thread
		new Thread(r).start();
	}

	private void callbackWithResults(final String results, String contentType) {
		// Return results to the callback

	  
	   
		 if(contentType.startsWith("text/")) {
			 
			 System.out.println("Printing str8 text");
			  
        	 UiApplication.getUiApplication().invokeLater(new Runnable() {
     			public void run() {
     				callback.GOVDataResultsCallbackText(results);
     			}
     		});
       }
        	 else if(contentType.startsWith("application/xml")) {
        		 
           
             	 final Vector parsedResults = GOVAPIUtils.parseXML(results);
             	// String data = parsedResults.toString();
             	 if(!parsedResults.isEmpty() && (parsedResults != null)){
            
             	 UiApplication.getUiApplication().invokeLater(new Runnable() {
          			public void run() {
          				callback.GOVDataResultsCallback(parsedResults);
          			}
             	 
          		});
             	 } else {
             		 UiApplication.getUiApplication().invokeLater(new Runnable() {
              			public void run() {
              				callback.GOVDataResultsCallbackText(results);
              			}
              		});
             	 }
        	 }
	
             	 
             	 else if(contentType.startsWith("application/json")) {
                 	
                 	 final Vector parsedResults = GOVAPIUtils.parseJSON(results);
                	 if(!parsedResults.isEmpty() && (parsedResults != null)){
                
                 	 UiApplication.getUiApplication().invokeLater(new Runnable() {
               			public void run() {
               				callback.GOVDataResultsCallback(parsedResults);
               			}
               		});
                 	} else {
                 		 UiApplication.getUiApplication().invokeLater(new Runnable() {
                  			public void run() {
                  				callback.GOVDataResultsCallbackText(results);
                  			}
                  		});
                 	}
                 	 }
      
	
	}

	private void callbackWithError(final String error) {
		// Return error to the callback
		UiApplication.getUiApplication().invokeLater(new Runnable() {
			public void run() {
				callback.GOVDataErrorCallback(error);
			}
		});
	}
}
