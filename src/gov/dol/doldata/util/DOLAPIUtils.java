package gov.dol.doldata.util;

import java.util.*;

import org.json.me.JSONArray;
import org.json.me.JSONException;
import org.json.me.JSONObject;


import net.rim.device.api.crypto.*;
import net.rim.device.api.i18n.MessageFormat;
import net.rim.device.api.i18n.SimpleDateFormat;


public class DOLAPIUtils {
	
	private static String getAPITimestamp()
	{
		SimpleDateFormat dateFormatGmt = new SimpleDateFormat("yyyy-MM-ddTHH:mm:ssZ");
	
		//Time in GMT
		return dateFormatGmt.format(new Date());
	}
	
	public static String getRequestHeader(String apiURI, String apiHost, String apiKey, String sharedSecret) throws CryptoTokenException, CryptoUnsupportedOperationException
	{
		//Timestamp
		String timestamp = getAPITimestamp();
		
		//Remove API_HOST from the apiURI
		//That part is not signed
		apiURI = apiURI.substring(apiHost.length());
		
		//Signature
		MessageFormat formatter = new MessageFormat("{0}&Timestamp={1}&ApiKey={2}");
		String dataToSign = formatter.format(new String[] {apiURI, timestamp, apiKey});
		String signature = getAPISignature(dataToSign, sharedSecret);
		
		//Final header
		formatter = new MessageFormat("Timestamp={0}&ApiKey={1}&Signature={2}");
		String result = formatter.format(new String[] {timestamp, apiKey, signature});
		
		return result;
	}
	
	private static String getAPISignature(String data, String sharedSecret) throws CryptoTokenException, CryptoUnsupportedOperationException
	{
	    byte[] keyData = sharedSecret.getBytes();
	    
	    // Create the key
	    HMACKey key = new HMACKey( keyData );

	    // Create the SHA digest
	    SHA1Digest digest = new SHA1Digest();

	    // Now an HMAC can be created, passing in the key and the
	    // SHA digest.
	    HMAC hMac = new HMAC( key, digest );

	    // The HMAC can be updated much like a digest
	    hMac.update(data.getBytes());

	    // Now get the MAC value.
	    String signature = toHexString(hMac.getMAC());
	    
	    return signature;
	}
	private static String toHexString(byte bytes[]) {
	    if (bytes == null) {
	        return null;
	    }

	    StringBuffer sb = new StringBuffer();
	    for (int iter = 0; iter < bytes.length; iter++) {
	        byte high = (byte) ( (bytes[iter] & 0xf0) >> 4);
	        byte low =  (byte)   (bytes[iter] & 0x0f);
	        sb.append(nibble2char(high));
	        sb.append(nibble2char(low));
	    }

	    return sb.toString();
	}

	private static char nibble2char(byte b) {
	    byte nibble = (byte) (b & 0x0f);
	    if (nibble < 10) {
	        return (char) ('0' + nibble);
	    }
	    return (char) ('a' + nibble - 10);
	}
	
	// The following methods are used to encode the URL String, URLEncoder is missing from JavaME
	// Obtained from Forum Nokia
	// http://wiki.forum.nokia.com/index.php/How_to_encode_URL_in_Java_ME_%3F
	
	//This method returns the encoded URL string. 
	public static String urlEncode(String s) {
	    StringBuffer sbuf = new StringBuffer();
	    int len = s.length();
	    for (int i = 0; i < len; i++) {
	        int ch = s.charAt(i);
	        if ('A' <= ch && ch <= 'Z') { // 'A'..'Z'
	            sbuf.append((char)ch);
	        } else if ('a' <= ch && ch <= 'z') { // 'a'..'z'
	            sbuf.append((char)ch);
	        } else if ('0' <= ch && ch <= '9') { // '0'..'9'
	            sbuf.append((char)ch);
	        } else if (ch == ' ') { // space
	            sbuf.append('+');
	        } else if (ch == '-' || ch == '_'   //these characters don't need encoding
	                || ch == '.' || ch == '*') {
	            sbuf.append((char)ch);
	        } else if (ch <= 0x007f) { // other ASCII
	            sbuf.append(hex(ch));
	        } else if (ch <= 0x07FF) { // non-ASCII <= 0x7FF
	            sbuf.append(hex(0xc0 | (ch >> 6)));
	            sbuf.append(hex(0x80 | (ch & 0x3F)));
	        } else { // 0x7FF < ch <= 0xFFFF
	            sbuf.append(hex(0xe0 | (ch >> 12)));
	            sbuf.append(hex(0x80 | ((ch >> 6) & 0x3F)));
	            sbuf.append(hex(0x80 | (ch & 0x3F)));
	        }
	    }
	    return sbuf.toString();
	}
	 
	//get the encoded value of a single symbol, each return value is 3 characters long
	static String hex(int sym)
	 {
	     return(hex.substring(sym*3, sym*3 + 3));
	 }
	 
	// Hex constants concatenated into a string, messy but efficient
	final static String hex = 
	"%00%01%02%03%04%05%06%07%08%09%0a%0b%0c%0d%0e%0f%10%11%12%13%14%15%16%17%18%19%1a%1b%1c%1d%1e%1f" + 
	"%20%21%22%23%24%25%26%27%28%29%2a%2b%2c%2d%2e%2f%30%31%32%33%34%35%36%37%38%39%3a%3b%3c%3d%3e%3f" + 
	"%40%41%42%43%44%45%46%47%48%49%4a%4b%4c%4d%4e%4f%50%51%52%53%54%55%56%57%58%59%5a%5b%5c%5d%5e%5f" + 
	"%60%61%62%63%64%65%66%67%68%69%6a%6b%6c%6d%6e%6f%70%71%72%73%74%75%76%77%78%79%7a%7b%7c%7d%7e%7f" + 
	"%80%81%82%83%84%85%86%87%88%89%8a%8b%8c%8d%8e%8f%90%91%92%93%94%95%96%97%98%99%9a%9b%9c%9d%9e%9f" +
	"%a0%a1%a2%a3%a4%a5%a6%a7%a8%a9%aa%ab%ac%ad%ae%af%b0%b1%b2%b3%b4%b5%b6%b7%b8%b9%ba%bb%bc%bd%be%bf" +
	"%c0%c1%c2%c3%c4%c5%c6%c7%c8%c9%ca%cb%cc%cd%ce%cf%d0%d1%d2%d3%d4%d5%d6%d7%d8%d9%da%db%dc%dd%de%df" +
	"%e0%e1%e2%e3%e4%e5%e6%e7%e8%e9%ea%eb%ec%ed%ee%ef%f0%f1%f2%f3%f4%f5%f6%f7%f8%f9%fa%fb%fc%fd%fe%ff";
	
	
	public static Vector parseJSON(String jsonString) {
		try {
			//Get JSON Object
			JSONObject object = new JSONObject(jsonString);
			
			//pass the "d" security wrapper
			JSONArray wrapper = object.optJSONArray("d");
			
			//Special case when .Net adds an extra "results" wrapper
			if (wrapper == null) {
				object = object.getJSONObject("d");
				wrapper = object.optJSONArray("results");
			}
			
			// if Wrapper is an array then it's DOL standard API response.
			if(wrapper != null)
			{
				//We'll store the list of Maps here
				Vector resultVector = new Vector();
				
				//Loop through all the results and store them in the map
				for (int i = 0; i < wrapper.length(); i++) {
					JSONObject obj = wrapper.getJSONObject(i);
	
				    Hashtable h = new Hashtable();
				    
				    //Iterate through all the columns. Convert JSONObject to Map
				    Enumeration enum = obj.keys();
				    while(enum.hasMoreElements()){
				        String key = (String) enum.nextElement();
				        Object value = obj.get(key);
				        h.put(key,value);
				    }
				    resultVector.addElement(h);
				}
				return resultVector;
			}
			else
			{
				//IF Wrapper is an object then it's DOL service operation result
				// use different algorith to parse the json result.
				return parseDOLServiceJsonResult(jsonString);
			}
			
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	

	public static Vector parseDOLServiceJsonResult(String jsonString) {
		try {
			//all objects are prefixed with o for oData.
			
			//We'll store the list of Maps here
			Vector resultList = new Vector();
			
			//Get JSON Object, original parse of root object
			JSONObject object = new JSONObject(jsonString);
			
			//A list of paths that will need to be flattened.
		     Vector oPaths = new Vector();
			//Current list of keys for the root object.  oMap will be used when a node has been resolved
			JSONArray oMap = object.names();
			JSONArray oArr; //temporary object to handle JSON array data.	
			
			Hashtable map;//key, value pair
			
			//encode root paths
			map = new Hashtable();
			for(int i=0; i<oMap.length();i++)
			{
				//all keys are strings, lets get the name of the key first.
				String oKey = oMap.optString(i);
			
				//lets test if the object that was returned is an array.
				oArr = object.optJSONArray(oKey);
				if(null !=oArr)
				{
					//Lets assume that all items in an array are the same type, then test if this an object array.
					if(null != oArr.optJSONObject(0))
					{
						//if so add each object in the array to the path list.
						for(int j=0;j<oArr.length(); j++)
						{
							//We add the pipe to separate the index from the key name.  This will later be used to construct a path from the root object.
							oPaths.addElement(oKey + "|" + Integer.toString(j));
							
						}
					} else {
						//could be an array of primitives
						for(int j=0;j<oArr.length(); j++)
						{
							//lets support only primitive arrays for now.
							 //need to generate some kind of naming convention for arrays, otherwise the Map object will overwrite entries.
							 map.put(oKey + "|" + Integer.toString(j), oArr.optString(j));
						}
					}
					
					continue;
				}
				
				//if this node is an object add it to the path list.
				if(null != object.optJSONObject(oKey))
				{
					//oPaths.add(oKey);
					oPaths.addElement(oKey);
					continue;
				}

				//if this node is a primitive, add it to the result list 
				String oVal = object.optString(oKey);
				if(null != oVal)
				{
					 map.put(oKey, oVal);
					continue;
				}
			}//end root path encoding
			
			//add all properties for this object to results list.
			resultList.addElement(map);
						
			//now that the root list has been added its time to cycle through this list.
			for(int i=0; i<oPaths.size();i++)
			{
				JSONObject oObj;
				oObj = navigateJson(oPaths.elementAt(i).toString(), object);

				
				if(null != oObj)
				{
					map = new Hashtable();
					//get the list of key for this object.
					oMap = oObj.names();
					for(int j=0; j<oMap.length();j++)
					{
						//get key name for later.
						String oKey =  oMap.optString(j);
						
						//test to see if this key is an array
						oArr = oObj.optJSONArray(oKey);
						if(null !=oArr)
						{
							//test if this an object array.
							if(null != oArr.optJSONObject(0))
							{
								//if so add each object path to the collection
								for(int k=0;k<oArr.length(); k++)
								{
									oPaths.addElement(oPaths.elementAt(i).toString() + "/" + oKey + "|" + Integer.toString(k));
								}
							} else {
								//could be an array of primitives
								for(int k=0;j<oArr.length(); k++)
								{
									//lets support only primitive arrays for now.
									//need to generate some kind of naming convention for arrays, otherwise the Map object will overwrite entries.
									 map.put(oKey + "|" + Integer.toString(k), oArr.optString(j));
								}
							}		
							
							continue;
						}
						
						//test if the item is a nested object, if so add it to the path list with the current path.
						if(null != oObj.optJSONObject(oKey))
						{
							oPaths.addElement(oPaths.elementAt(i).toString() + "/" + oKey);
							continue;
						}

						//test if item is a primitive value if so add it to the map.
						String oVal = oObj.optString(oKey);
						if(null != oVal)
						{
							 map.put(oKey, oVal);
							continue;
						}
					}//end name loop
					
					//add object map to list of results.
					resultList.addElement(map);
				} //end of object
			}// end of path loop
			
			return resultList;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	//navigates the JSON DOM starting from the root object given a path to the object node.
	private static JSONObject navigateJson(String path, JSONObject rootObj)
	{
		//need to reconstruct the path since there is no traversing mechanism for the android JSON object tokenizer. 
		
		String[] tokens = split(path, "/");
		JSONObject oObj;
		
		//while loop is used to traverse the path to find the object or an array. Array have a pipe to separate the index and the key name.
		//start at the root object.
		oObj = rootObj;
		for(int i=0; i<tokens.length;i++)
		{
			String t = tokens[i];
			int pipe = t.indexOf("|");  //needed a way to select an index on an array to go deeper.
			if(0 < pipe) //only arrays will have Pipe
			{
				String  KeyName = t.substring(0, pipe);
				int _i=Integer.parseInt(t.substring( pipe +1));
				oObj = oObj.optJSONArray(KeyName).optJSONObject(_i);
			} else {
				oObj = oObj.optJSONObject(t);
			}
		}
		
		return oObj;
	}
	
	public static String[] split(String inString, String delimeter) {
		String[] retAr;
		try {
			Vector vec = new Vector();
			int indexA = 0;
			int indexB = inString.indexOf(delimeter);

			while (indexB != -1) {
				vec.addElement(new String(inString.substring(indexA, indexB)));
				indexA = indexB + delimeter.length();
				indexB = inString.indexOf(delimeter, indexA);
			}
			vec.addElement(new String(inString.substring(indexA, inString
					.length())));
			retAr = new String[vec.size()];
			for (int i = 0; i < vec.size(); i++) {
				retAr[i] = vec.elementAt(i).toString();
			}
		} catch (Exception e) {
			String[] ar = { e.toString() };
			return ar;
		}
		return retAr;
	}
}
