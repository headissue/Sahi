package net.sf.sahi.response;

import net.sf.sahi.StreamHandler;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;

/**
 * User: nraman
 * Date: May 13, 2005
 * Time: 10:25:31 PM
 */
public class HttpResponse extends StreamHandler {
	private static final Logger logger = Logger.getLogger("net.sf.sahi.response.HttpResponse");
    protected HttpResponse(){
    }

    public HttpResponse(InputStream in) throws IOException {
        populateHeaders(in, true);
        populateData(in);
		logger.fine("First line:"+firstLine());
    }

    public String contentType() {
        return getLastSetValueOfHeader("Content-Type");
    }
}
