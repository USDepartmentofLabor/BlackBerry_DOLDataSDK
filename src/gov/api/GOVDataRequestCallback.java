package gov.data.api;

import java.util.Vector;

public interface GOVDataRequestCallback {
	//Return results
	public void GOVDataResultsCallback(Vector results);
	
	//Error Callback
	public void GOVDataErrorCallback(String error);
	
	public void GOVDataResultsCallbackText(String results);
}
