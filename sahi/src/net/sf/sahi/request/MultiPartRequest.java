/**
 * Sahi - Web Automation and Test Tool
 *
 * Copyright  2006  V Narayan Raman
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.sf.sahi.request;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
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
        subRequests = new ArrayList();
        while (prevIx + 1 < dataStr.length() && (nextIx = dataStr.indexOf(delimiter, prevIx + 1)) != -1) {
            String subReqStr = dataStr.substring(prevIx, nextIx).trim();
            MultiPartSubRequest multiPartSubRequest = new MultiPartSubRequest(new ByteArrayInputStream(subReqStr.getBytes()));
            subRequests.add(multiPartSubRequest);
            prevIx = nextIx + delimiter.length();
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


    public HttpRequest getRebuiltRequest() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] delimBytes = (delimiter + "\r\n").getBytes();
        try {
            for (Iterator iterator = subRequests.iterator(); iterator.hasNext();) {
                out.write(delimBytes);
                MultiPartSubRequest part = (MultiPartSubRequest) iterator.next();
                part.resetRawHeaders();
                out.write(part.rawHeaders());
                out.write(part.data());
                out.write("\r\n".getBytes());
            }
            out.write((delimiter+"--\r\n").getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
        httpRequest.setData(out.toByteArray());
        return httpRequest;
    }
}
