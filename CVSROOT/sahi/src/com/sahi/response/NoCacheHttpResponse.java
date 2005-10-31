package com.sahi.response;

import java.util.logging.Logger;

/**
 * User: nraman
 * Date: May 18, 2005
 * Time: 11:04:06 PM
 */
public class NoCacheHttpResponse extends HttpResponse {
	private static final Logger logger = Logger.getLogger("com.sahi.response.NoCacheHttpResponse");
	private int status = 200;
	private String statusMessage = "OK";

	public NoCacheHttpResponse(String dataStr) {
		setNoCacheHeaders(dataStr.getBytes());
    }

	public NoCacheHttpResponse(int status, String statusMessage, String dataStr) {
		this.status = status;
		if (status != 200) this.statusMessage = "";
		this.statusMessage = statusMessage == null ? "" : statusMessage;
		setNoCacheHeaders(dataStr.getBytes());
	}
	
    protected void setNoCacheHeaders(byte[] data) {
        data(data);
		setFirstLine("HTTP/1.1 " + status + " " + statusMessage);
        headers().put("Content-Type", "text/html");
        headers().put("Cache-control", "no-cache");
        headers().put("Cache-control", "no-store");
        headers().put("Pragma", "no-cache");
        headers().put("Expires", "0");
        headers().put("Connection", "close");
        headers().put("Content-Length", "" + data().length);
        setRawHeaders(getRebuiltHeaderBytes());
        logger.fine(new String(rawHeaders()));
    }
}
