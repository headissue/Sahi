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

package net.sf.sahi;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.StringTokenizer;

import net.sf.sahi.util.Utils;

/**
 * User: nraman Date: May 13, 2005 Time: 7:24:06 PM
 */
public abstract class StreamHandler {
	HttpHeaders headers = new HttpHeaders();
    private byte[] rawHeaders;
    private int contentLength = -1;
    private byte[] data;
    protected String firstLine;

    protected void populateData(InputStream in) throws IOException {
        data = Utils.getBytes(in, contentLength());
        setContentLength(data.length);
        //System.out.println("## Contentlength = "+ contentLength);
    }

    protected void populateHeaders(InputStream in,
                                   boolean handleFirstLineSpecially) throws IOException {
        setRawHeaders(in);
        setHeaders(new String(rawHeaders), handleFirstLineSpecially);
        setContentLengthFromHeader();
    }

    protected void setContentLength(int length) {
		removeHeader("Content-Length");
    	if (length != -1){
    		setHeader("Content-Length", "" + length);
    	}
        contentLength = length;
    }

    private void setContentLengthFromHeader() {
        String contentLenStr = getLastSetValueOfHeader("Content-Length");
        if (contentLenStr != null)
            contentLength = Integer.parseInt(contentLenStr);
    }

    public byte[] data() {
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

    public final HttpHeaders headers() {
        return headers;
    }
    
    public boolean hasHeader(String key){
    	return headers.hasHeader(key);
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
    	ByteArrayOutputStream byteArOut = new ByteArrayOutputStream();
		BufferedOutputStream bout = new BufferedOutputStream(byteArOut);
		byte prev = ' ';
        byte c;
        while ((c = (byte) in.read()) != -1) {
        	bout.write(c);
            if (c == '\r' && prev == '\n') {
            	bout.write((char) in.read());
                break;
            }
            prev = c;
        }
		bout.flush();
		bout.close();
		rawHeaders = byteArOut.toByteArray();
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

    protected String setFirstLine(final String s) {
        return (firstLine = s);
    }

    protected byte[] getRebuiltHeaderBytes() {
        StringBuilder sb = new StringBuilder();
        if (firstLine() != null) {
            sb.append(firstLine());
            sb.append("\r\n");
        }
        sb.append(headers.toString());
        sb.append("\r\n");
//        try {
			return sb.toString().getBytes();
//		} catch (UnsupportedEncodingException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			return sb.toString().getBytes();
//		}
    }

    public void setHeader(final String key, final String value) {
        headers.setHeader(key, value);
    }

//    public void setHeaders(Map headers) {
//        this.headers = new HashMap(headers);
//    }

    public void addHeader(String key, String value) {
    	headers.addHeader(key, value);
    }

    public void removeHeader(final String key) {
        headers.removeHeader(key);
    }

	public void removeHeader(String key, String value) {
        List<String> values = headers.getHeaders(key);
        if (values == null) return;
        int size = values.size();
        int removeIx = -1;
        for (int i = 0; i < size; i++) {
            String value2 = (String) values.get(i);
            if (value.equals(value2)){
            	removeIx = i;
            }
        }	
    	if (removeIx != -1) values.remove(removeIx);
	}

    
    protected String getLastSetValueOfHeader(final String key) {
    	return headers.getLastHeader(key);
    }

    protected void copyFrom(final StreamHandler orig) {
        this.headers = orig.headers;
        this.rawHeaders = orig.rawHeaders;
        this.contentLength = orig.contentLength;
        this.data = orig.data;
        this.firstLine = orig.firstLine;
    }

}
