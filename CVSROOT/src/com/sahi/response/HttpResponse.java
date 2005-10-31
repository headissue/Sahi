package com.sahi.response;

import com.sahi.StreamHandler;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;

/**
 * User: nraman
 * Date: May 13, 2005
 * Time: 10:25:31 PM
 */
public class HttpResponse extends StreamHandler {
	private static final Logger logger = Logger.getLogger("com.sahi.response.HttpResponse");
    protected HttpResponse(){
    }
    
    public HttpResponse(InputStream in) throws IOException {
        populateHeaders(in, true);
        populateData(in);
		logger.fine("First line:"+firstLine());
    }

    public String contentType() {
        return (String) headers().get("Content-Type");
    }
}
