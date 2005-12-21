package com.sahi.response;

/**
 * User: nraman
 * Date: May 18, 2005
 * Time: 11:04:06 PM
 */
public class SimpleHttpResponse extends HttpResponse {
    public SimpleHttpResponse(String dataStr) {
        this(dataStr.getBytes());
    }

    public SimpleHttpResponse(String dataStr, boolean closeConnection) {
        this(dataStr.getBytes(), closeConnection);
    }

    public SimpleHttpResponse(byte[] data) {
    	this(data, true);
    }
    
    public SimpleHttpResponse(byte[] data, boolean closeConnection) {
        data(data);
        setFirstLine("HTTP/1.1 200 OK");
        setHeader("Content-Type", "text/html");
		setHeader("Cache-Control","no-cache");
		setHeader("Pragma","no-cache");
		setHeader("Expires", "0");        
        if (closeConnection) {
        	setHeader("Connection", "close");
        }
        setHeader("Content-Length", "" + data().length);
        setRawHeaders(getRebuiltHeaderBytes());
    }
}
