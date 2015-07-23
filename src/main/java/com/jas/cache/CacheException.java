package com.jas.cache;

public class CacheException extends Exception {

	private static final long serialVersionUID = 4425790451479048812L;

    private String responseMessage;
    private String responseCode;
    private String json;
    private int httpCode;
    private boolean failedConnection;

    public CacheException() {
        super();    
    }

    // TODO Have this take the rest easy repsonse?
    /*
    public CacheException(String messengerResponse) {
        this.xml = messengerResponse.getXml();
        this.messengerResponse = messengerResponse;
    }
    */

    public CacheException(String message) {
        super(message);    //To change body of overridden methods use File | Settings | File Templates.
    }

    public CacheException(String message, Throwable cause) {
        super(message, cause);    //To change body of overridden methods use File | Settings | File Templates.
    }

    public CacheException(Throwable cause) {
        super(cause);    //To change body of overridden methods use File | Settings | File Templates.
    }

    public String getResponseMessage() {
        return responseMessage;
    }

    public void setResponseMessage(String responseMessage) {
        this.responseMessage = responseMessage;
    }

    public String getResponseCode() {
		return responseCode;
	}

	public void setResponseCode(String responseCode) {
		this.responseCode = responseCode;
	}

	public String getJson() {
		return json;
	}

	public void setJson(String json) {
		this.json = json;
	}

	public int getHttpCode() {
        return httpCode;
    }

    public void setHttpCode(int httpCode) {
        this.httpCode = httpCode;
    }

    public boolean isFailedConnection() {
        return failedConnection;
    }

    public void setFailedConnection(boolean failedConnection) {
        this.failedConnection = failedConnection;
    }

    public String toString(){
        return super.toString()
                + "\n\thttpCode=" + httpCode
                + "\n\tresponseCode = " + responseCode
                + "\n\tresponseMessage type: " + (null == responseMessage ? "null" : responseMessage.getClass().getName())
                + "\n\txml: " + json;
    }

    
}
