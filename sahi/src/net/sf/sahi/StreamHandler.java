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

package net.sf.sahi;

import net.sf.sahi.util.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * User: nraman Date: May 13, 2005 Time: 7:24:06 PM
 */
public abstract class StreamHandler {
    Map headers = new LinkedHashMap();
    private byte[] rawHeaders;
    private int contentLength = -1;
    private byte[] data;
    protected String firstLine;

    protected void populateData(InputStream in) throws IOException {
        data = Utils.getBytes(in, contentLength());
    }

    protected void populateHeaders(InputStream in,
                                   boolean handleFirstLineSpecially) throws IOException {
        setRawHeaders(in);
        setHeaders(new String(rawHeaders), handleFirstLineSpecially);
        setContentLength();
    }

    private void setContentLength(int length) {
        setHeader("Content-Length", "" + length);
        contentLength = length;
    }

    private void setContentLength() {
        String contentLenStr = getLastSetValueOfHeader("Content-Length");
        if (contentLenStr != null)
            contentLength = Integer.parseInt(contentLenStr);
    }

    public final byte[] data() {
        return data;
    }

    public byte[] setData(byte[] bytes) {
        data = bytes;
        setContentLength(bytes.length);
        resetRawHeaders();
        return data;
    }

    public final int contentLength() {
        return contentLength;
    }

    public final Map headers() {
        return headers;
    }

    public final byte[] rawHeaders() {
        return rawHeaders;
    }

    public byte[] setRawHeaders(byte[] bytes) {
        return rawHeaders = bytes;
    }

    public void resetRawHeaders() {
        setRawHeaders(getRebuiltHeaderBytes());
    }

    private void setRawHeaders(InputStream in) throws IOException {
        StringBuffer sb = new StringBuffer();
        byte prev = ' ';
        byte c;
        while ((c = (byte) in.read()) != -1) {
            sb.append((char) c);
            if (c == '\r' && prev == '\n') {
                sb.append((char) in.read());
                break;
            }
            prev = c;
        }
        rawHeaders = sb.toString().getBytes();
    }

    private void setHeaders(String s, boolean handleFirstLineSpecially) {
        StringTokenizer tokenizer = new StringTokenizer(s, "\r\n");
        boolean isFirst = true;
        while (tokenizer.hasMoreTokens()) {
            String line = tokenizer.nextToken();
            if ("".equals(line.trim()))
                continue;
            if (handleFirstLineSpecially && isFirst) {
                firstLine = line;
                isFirst = false;
                continue;
            }
            int ix = line.indexOf(":");
            if (ix != -1) {
                String key = line.substring(0, ix);
                String value = line.substring(ix + 1).trim();
                addHeader(key, value);
            }
        }
    }

    protected final String firstLine() {
        return firstLine;
    }

    protected String setFirstLine(String s) {
        return (firstLine = s);
    }

    protected byte[] getRebuiltHeaderBytes() {
        StringBuffer sb = new StringBuffer();
        if (firstLine() != null) {
            sb.append(firstLine());
            sb.append("\r\n");
        }
        Iterator keys = headers.keySet().iterator();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            List values = (List) headers.get(key);
            int size = values.size();
            for (int i = 0; i < size; i++) {
                String value = (String) values.get(i);
                sb.append(key).append(": ").append(value).append("\r\n");

            }
        }
        sb.append("\r\n");
        return sb.toString().getBytes();
    }

    public void setHeader(String key, String value) {
        List entry = new ArrayList();
        entry.add(value);
        headers.put(key, entry);
    }

    public void addHeader(String key, String value) {
        List entry = (List) headers.get(key);
        if (entry == null) {
            entry = new ArrayList();
            headers.put(key, entry);
        }
        entry.add(value);
    }

    protected void removeHeader(String key) {
        headers.remove(key);
    }

    protected String getLastSetValueOfHeader(String key) {
        List entry = (List) headers.get(key);
        if (entry == null)
            return null;
        return (String) entry.get(entry.size() - 1);
    }

    protected void copyFrom(StreamHandler orig) {
        this.headers = orig.headers;
        this.rawHeaders = orig.rawHeaders;
        this.contentLength = orig.contentLength;
        this.data = orig.data;
        this.firstLine = orig.firstLine;
    }

}
