package com.sahi;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import com.sahi.util.Utils;

/**
 * User: nraman Date: May 13, 2005 Time: 7:24:06 PM
 */
public abstract class StreamHandler {
	Map headers = new LinkedHashMap();
	private byte[] rawHeaders;
	private int contentLength = -1;
	private byte[] data;
	private String firstLine;

	protected void populateData(InputStream in) throws IOException {
		data = Utils.getBytes(in, contentLength());
	}

	protected void populateHeaders(InputStream in,
			boolean handleFirstLineSpecially) throws IOException {
		setRawHeaders(in);
		setHeaders(new String(rawHeaders), handleFirstLineSpecially);
		setContentLength();
	}

	private void setContentLength() {
		String contentLenStr = (String) (getLastSetValueOfHeader("Content-Length"));
		if (contentLenStr != null)
			contentLength = Integer.parseInt(contentLenStr);
	}

	public final byte[] data() {
		return data;
	}

	protected byte[] data(byte[] bytes) {
		data = bytes;
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
		sb.append(firstLine());
		sb.append("\r\n");
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

	protected void setHeader(String key, String value) {
		List entry = new ArrayList();
		entry.add(value);
		headers.put(key, entry);
	}

	protected void addHeader(String key, String value) {
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

}
