package net.sf.sahi.response;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Properties;

import net.sf.sahi.config.Configuration;
import net.sf.sahi.util.Utils;

/**
 * User: nraman Date: May 15, 2005 Time: 10:14:34 PM
 */
public class HttpFileResponse extends HttpResponse {
	private String fileName;
	boolean addCacheHeader = false;

	public HttpFileResponse(String fileName, Properties substitutions, boolean addCacheHeader, boolean cacheFileInMemory) {
		this.fileName = fileName;
		byte[] bytes;
		if (cacheFileInMemory && !Configuration.isDevMode())
			bytes = Utils.readCachedFile(fileName);
		else
			bytes = Utils.readFile(fileName);
		setData(bytes);
		if (substitutions != null) {
			setData(substitute(new String(data()), substitutions).getBytes());
		}
		this.addCacheHeader = addCacheHeader;
		setHeaders();
	}

	static String substitute(String content, Properties substitutions) {
		Enumeration keys = substitutions.keys();
		while (keys.hasMoreElements()) {
			String key = (String) keys.nextElement();
			content = content.replaceAll("\\$" + key, substitutions
					.getProperty(key));
		}
		return content;
	}

	public HttpFileResponse(String fileName) {
		this(fileName, null, true, true);
	}

	private void setHeaders() {
		setFirstLine("HTTP/1.1 200 OK");
		setHeader("Content-Type", MimeType.getMimeTypeOfFile(fileName));
		if (addCacheHeader) {
			setHeader("Expires", formatForExpiresHeader(new Date(
							System.currentTimeMillis() + 10 * 60 * 1000)));
		}
		
		setHeader("Connection", "close");
		setHeader("Content-Length", "" + data().length);
		setRawHeaders(getRebuiltHeaderBytes());
	}

	static String formatForExpiresHeader(Date date) {
		return new SimpleDateFormat("EEE, dd MMM yyyy hh:mm:ss z").format(date);
	}
}
