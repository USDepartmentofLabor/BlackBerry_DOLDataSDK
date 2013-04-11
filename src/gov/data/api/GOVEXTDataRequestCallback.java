package gov.data.api;

public interface GOVEXTDataRequestCallback {
	//Return results
	
	
	public void requestSucceeded(byte[] result, String contentType);

	

    public void requestFailed(String message);

	
}
