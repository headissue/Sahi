package net.sf.sahi.response;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import net.sf.sahi.stream.filter.ReadWriteThread;
import net.sf.sahi.stream.filter.StreamFilter;
import net.sf.sahi.stream.filter.StreamFilterInputStream;
import net.sf.sahi.util.Utils;

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


public class StreamingHttpResponse extends HttpResponse {

	protected BufferedInputStream in;
	private List<StreamFilter> filters = new ArrayList<StreamFilter>();
	private static ExecutorService pool = Executors.newCachedThreadPool();

	StreamingHttpResponse(){}

    public StreamingHttpResponse(InputStream in, HttpURLConnection connection) throws IOException {
    	if (!(in instanceof BufferedInputStream)){
    		this.in = new BufferedInputStream(in, Utils.BUFFER_SIZE);
    	} else {
    		this.in = (BufferedInputStream) in;
    	}
    	Map<String,List<String>> connheaders = connection.getHeaderFields();
        setHeaders(connection);
        List<String> firstLines = connheaders.get(null); 
        // can have multiple lines, especially if there are unknown connection keys.
        // choose the one with HTTP in it.
        if (firstLines != null) {
	        for (Iterator<String> iterator = firstLines.iterator(); iterator.hasNext();) {
				String line = iterator.next();
				if (line.indexOf("HTTP") != -1)
					firstLine = line;
			}
        }
	    setContentLength(connection.getContentLength());
    }

    public StreamingHttpResponse(HttpResponse response){
    	copyFrom(response);
    	this.in = new BufferedInputStream(new ByteArrayInputStream(response.data()), Utils.BUFFER_SIZE);
    }

    private void setHeaders(HttpURLConnection connection){
    	for (int i=1; true; i++){
    		String key = connection.getHeaderFieldKey(i);
    		if (key == null) break;
			addHeader(key, connection.getHeaderField(i));
    	}
    }
	public byte[] data(){
		return new byte[0];
	}

	int getModifiedContentLength() {
		return contentLength();
	}

	public void modifyHeaders(boolean isKeepAlive){
        proxyKeepAlive(isKeepAlive);
        modifyHeadersViaFilters(this);
		resetRawHeaders();
	}

	public void sendBody(OutputStream out) throws IOException {
        OutputStream outputStreamToBrowser = new BufferedOutputStream(out, Utils.BUFFER_SIZE);
        int length = contentLength();
        pipe(in, outputStreamToBrowser, length);
        outputStreamToBrowser.flush();
	}

	public void copyFrom(StreamingHttpResponse orig){
		this.in = orig.in;
		super.copyFrom(orig);
	}

	private InputStream applyFilters(InputStream in) {
		Iterator<StreamFilter> iter = this.filters.iterator();
		while (iter.hasNext()){
			StreamFilter filter = iter.next();
//			in = filter.filter(in);
			in = new StreamFilterInputStream(in, filter); // Contributed by [Richard Li]
		}
		return in;
	}

	private void modifyHeadersViaFilters(HttpResponse response){
		Iterator<StreamFilter> iter = this.filters.iterator();
		while (iter.hasNext()){
			StreamFilter filter = iter.next();
			try {
				filter.modifyHeaders(response);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void addFilter(StreamFilter filter) {
		this.filters.add(filter);
	}

	public List<StreamFilter> getFilters() {
		return filters;
	}

	private void pipe(InputStream in, OutputStream out, int contentLength) throws IOException {
//		System.out.println("### contentLength="+contentLength);
		InputStream  pin = new PipedInputStream();
		OutputStream pout = new PipedOutputStream((PipedInputStream) pin);
        pin = applyFilters(pin);
        pool.execute(new ReadWriteThread(in, pout, contentLength, true, "Reader"));
        Future<?> future = pool.submit(new ReadWriteThread(pin, out, "Writer"));

        // Join using future.get() so that connection is closed only after stream has been written to.
        try {
			future.get();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	public void cleanUp() {
		try {
			in.close();
		} catch (IOException e) {
			// e.printStackTrace();
		}
	}
}
