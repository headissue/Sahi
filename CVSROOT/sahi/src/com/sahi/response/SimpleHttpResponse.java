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

    public SimpleHttpResponse(byte[] data) {
        data(data);
        setFirstLine("HTTP/1.1 200 OK");
        headers().put("Content-Type", "text/html");
		headers().put("Cache-Control","no-cache");
		headers().put("Pragma","no-cache");
		headers().put("Expires", "0");        
        headers().put("Connection", "close");
        headers().put("Content-Length", "" + data().length);
        setRawHeaders(getRebuiltHeaderBytes());
    }
}
