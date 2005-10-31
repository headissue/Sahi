package com.sahi.request;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;

import com.sahi.test.SahiTestSuite;

/**
 * User: nraman
 * Date: May 14, 2005
 * Time: 2:12:49 AM
 */
public class HttpModifiedRequest extends HttpRequest {
	private static final Logger logger = Logger.getLogger("com.sahi.request.HttpModifiedRequest");
	
    public HttpModifiedRequest(InputStream in) throws IOException {
        super(in);
        setFirstLine(getModifiedMethod() + " " + uri().replaceAll("_b_", "_s_") + " " + "HTTP/1.0");
        headers().remove("Proxy-Connection");
        headers().remove("Accept-Encoding");
        headers().remove("Keep-Alive");
        headers().remove("ETag");
        headers().remove("If-Modified-Since");
        headers().remove("If-None-Match");
        headers().put("Connection", "close");
        setRawHeaders(getRebuiltHeaderBytes());
		logger.fine("\nRequest Headers:\n"+headers());        
    }

	private String getModifiedMethod() {
		return method();
	}

	public String host() {
		return SahiTestSuite.stripSah(super.host());
	}	
}
