package net.sf.sahi.request;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * User: nraman
 * Date: May 18, 2005
 * Time: 9:18:19 PM
 */
public class MultiPartRequest {
    private final HttpRequest httpRequest;
    List subRequests;

    public MultiPartRequest(HttpRequest httpRequest) throws IOException {
        this.httpRequest = httpRequest;
        populateSubParts();
    }

    private void populateSubParts() throws IOException {
        String dataStr = new String(httpRequest.data()).trim();
        String delim = getDelimiter(dataStr);
        int nextIx = -1;
        int prevIx = delim.length();
        subRequests = new ArrayList();
        while (prevIx + 1 < dataStr.length() && (nextIx = dataStr.indexOf(delim, prevIx + 1)) != -1) {
            String subReqStr = dataStr.substring(prevIx, nextIx);
            MultiPartSubRequest multiPartSubRequest = new MultiPartSubRequest(new ByteArrayInputStream(subReqStr.getBytes()));
            subRequests.add(multiPartSubRequest);
            prevIx = nextIx + delim.length();
        }
    }


    private String getDelimiter(String dataStr) {
        return dataStr.substring(0, dataStr.indexOf("\n")).trim();
    }

    public HttpRequest getSimpleHttpRequest() {
        return httpRequest;
    }

    public List getMultiPartSubRequests() {
        return subRequests;
    }

    public final int contentLength() {
        return httpRequest.contentLength();
    }

    public final Map headers() {
        return httpRequest.headers();
    }

    public final byte[] rawHeaders() {
        return httpRequest.rawHeaders();
    }

    public byte[] rawHeaders(byte[] bytes) {
        return httpRequest.setRawHeaders(bytes);
    }

    public String host() {
        return httpRequest.host();
    }

    public int port() {
        return httpRequest.port();
    }

    public boolean isPost() {
        return httpRequest.isPost();
    }

    public boolean isSSL() {
        return httpRequest.isSSL();
    }

    public String method() {
        return httpRequest.method();
    }

    public String uri() {
        return httpRequest.uri();
    }

    public String protocol() {
        return httpRequest.protocol();
    }
}
