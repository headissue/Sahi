/**
 * Sahi - Web Automation and Test Tool
 * 
 * Copyright  2006  V Narayan Raman
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.sf.sahi.request;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * User: nraman
 * Date: May 18, 2005
 * Time: 9:18:19 PM
 */
public class MultiPartRequest {

    private final HttpRequest httpRequest;
    List<MultiPartSubRequest> subRequests;
    public String delimiter;

    public MultiPartRequest(HttpRequest httpRequest) throws IOException {
        this.httpRequest = httpRequest;
        populateSubParts();
    }

    private void populateSubParts() throws IOException {
        String dataStr = new String(httpRequest.data()).trim();
        delimiter = getDelimiter(dataStr);
        int nextIx;
        int prevIx = delimiter.length();
        subRequests = new ArrayList<MultiPartSubRequest>();
        while (prevIx + 1 < dataStr.length() && (nextIx = dataStr.indexOf(delimiter, prevIx + 1)) != -1) {
            String subReqStr = dataStr.substring(prevIx, nextIx).trim();
            MultiPartSubRequest multiPartSubRequest = new MultiPartSubRequest(new ByteArrayInputStream(subReqStr.getBytes()));
            subRequests.add(multiPartSubRequest);
            prevIx = nextIx + delimiter.length();
        }
    }

    private String getDelimiter(final String dataStr) {
        return dataStr.substring(0, dataStr.indexOf("\n")).trim();
    }

    public HttpRequest getSimpleHttpRequest() {
        return httpRequest;
    }

    public List<MultiPartSubRequest> getMultiPartSubRequests() {
        return subRequests;
    }

    public final int contentLength() {
        return httpRequest.contentLength();
    }

    public final byte[] rawHeaders() {
        return httpRequest.rawHeaders();
    }

    public byte[] rawHeaders(final byte[] bytes) {
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

    public HttpRequest getRebuiltRequest() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] delimBytes = (delimiter + "\r\n").getBytes();
        try {
            for (Iterator<MultiPartSubRequest> iterator = subRequests.iterator(); iterator.hasNext();) {
                out.write(delimBytes);
                MultiPartSubRequest part = (MultiPartSubRequest) iterator.next();
                part.resetRawHeaders();
                out.write(part.rawHeaders());
                out.write(part.data());
                out.write("\r\n".getBytes());
            }
            out.write((delimiter + "--\r\n").getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
        httpRequest.setData(out.toByteArray());
        return httpRequest;
    }
}
