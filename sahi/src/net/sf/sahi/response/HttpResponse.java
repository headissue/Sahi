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
package net.sf.sahi.response;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.sf.sahi.StreamHandler;
import net.sf.sahi.config.Configuration;
import net.sf.sahi.util.TrafficLogger;
import net.sf.sahi.util.Utils;

/**
 * User: nraman
 * Date: May 13, 2005
 * Time: 10:25:31 PM
 */
public class HttpResponse extends StreamHandler {
    protected HttpResponse() {
    }

    public HttpResponse(InputStream in, HttpURLConnection connection) {
        setHeaders(connection);
        setFirstLine(connection);
//	    int contentLength = connection.getContentLength();
//	    System.out.println("))))))))))))) contentLength="+contentLength);
		setContentLength(-1);   
		try{
			populateData(in);
		}catch(IOException ioe){
			// suppress;
		}
	}

	private void setFirstLine(HttpURLConnection connection) {
		Map<String,List<String>> connheaders = connection.getHeaderFields();
        List<String> firstLines = connheaders.get(null); 
        // can have multiple lines, especially if there are unknown connection keys.
        // choose the one with HTTP in it.
        if (firstLines == null) return;
        for (Iterator<String> iterator = firstLines.iterator(); iterator.hasNext();) {
			String line = iterator.next();
			if (line.indexOf("HTTP") != -1)
				setFirstLine(line);
		}
	}

    private void setHeaders(HttpURLConnection connection){
    	for (int i=1; true; i++){
    		String key = connection.getHeaderFieldKey(i);
    		if (key == null) break;
			addHeader(key, connection.getHeaderField(i));
    	}
    }
    
	public String contentTypeHeader() {
        return getLastSetValueOfHeader("Content-Type");
    }

    public void keepAlive(final boolean keepAliveEnabled) {
        setFirstLine(firstLine().replaceAll("HTTP/1.0", "HTTP/1.1"));
        removeHeader("Content-length");
        int len = data() != null ? getModifiedContentLength() : 0;
        setHeader("Content-Length", "" + len);
        removeHeader("Connection");
        removeHeader("Accept-ranges");
        setHeader("Connection", keepAliveEnabled ? "Keep-Alive" : "close");
        resetRawHeaders();
    }

    public void proxyKeepAlive(final boolean keepAliveEnabled) {
        setFirstLine(firstLine().replaceAll("HTTP/1.0", "HTTP/1.1"));
        removeHeader("Connection");
        removeHeader("Accept-ranges");
        removeHeader("Accept-Ranges");
        setHeader("Accept-Ranges", "none");
        setHeader("Proxy-Connection", keepAliveEnabled ? "Keep-Alive" : "close");
    }

    public boolean isAttachment(){
    	if (Configuration.downloadIfContentDispositionIsAttachment()){
	        String contentDisposition = getLastSetValueOfHeader("Content-Disposition");
	        if (contentDisposition == null) return false;
	        return contentDisposition.toLowerCase().indexOf("attachment") != -1;
    	}
    	return false;
    }

    public void sendHeaders(OutputStream out, boolean isKeepAlive) throws IOException {
        OutputStream outputStreamToBrowser = new BufferedOutputStream(out);
        modifyHeaders(isKeepAlive);
        resetRawHeaders();
//        System.out.println("--\n" + new String(rawHeaders()) + "\n--");
        TrafficLogger.storeResponseHeader(rawHeaders(), "modified");
        outputStreamToBrowser.write(rawHeaders());
        outputStreamToBrowser.flush();
    }

    public void modifyHeaders(boolean isKeepAlive){
        proxyKeepAlive(isKeepAlive);
        // The Transfer-Encoding should never be chunked, since we are sending it sequentially
        removeHeader("Transfer-Encoding");
        removeHeader("Transfer-encoding");
        setContentLength(getModifiedContentLength());
    }

    int getModifiedContentLength() {
        return data() == null ? 0 : data().length;
    }

    public void sendBody(OutputStream out) throws IOException {
        OutputStream bufferedOut = new BufferedOutputStream(out);
        final byte[] data = data();
        if (data != null) {
            int start = 0;
            int limit = Utils.BUFFER_SIZE;
            int left = data.length;
            while (left > 0){
                if (left < limit) limit = left;
                bufferedOut.write(data, start, limit);
                bufferedOut.flush();
                start = start + limit;
                left = left - limit;
            }
        }
        bufferedOut.flush();
        TrafficLogger.storeResponseBody(data, "modified");
    }

	public void cleanUp() {
		
	}
}
