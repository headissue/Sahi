package com.sahi.response;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * User: nraman Date: May 14, 2005 Time: 1:43:05 AM 
 */
public class HttpModifiedResponse extends HttpResponse {
	private static final String INJECT_TOP = ""  
		+ "<script src='/_s_/spr/util.js'></script>\n"
		+ "<script src='/_s_/spr/exception.js'></script>\n"
		+ "<script src='/_s_/spr/cookie.js'></script>\n"
		+ "<script src='/_s_/dyn/state.js'></script>\n"
		+ "<script src='/_s_/spr/accessor.js'></script>\n"
		+ "<script src='/_s_/spr/handler.js'></script>\n"
		+ "<script src='/_s_/spr/event.js'></script>\n"
		+ "<script src='/_s_/spr/api.js'></script>\n"
		+ "<script src='/_s_/spr/apihelper.js'></script>\n"
		+ "<script src='/_s_/spr/common.js'></script>\n"
		+ "<script src='/_s_/spr/include.js'></script>\n"
		+ "";

	private static final String INJECT_BOTTOM = "" 
		+ "<script src='/_s_/dyn/script.js'></script>\n"
		+ "<script src='/_s_/spr/playback.js'></script>\n" 
		+ "";

	public HttpModifiedResponse(InputStream in) throws IOException {
		super(in);
		if (firstLine().indexOf("30") == -1) { // Response code other than redirects
			headers().remove("Transfer-Encoding");
			headers().put("Cache-Control","no-cache");
			headers().put("Pragma","no-cache");
			headers().put("Expires", "0");
//			headers().remove("ETag");
//			headers().remove("Last-Modified");
			if (isHTML())
				data(getModifiedData());
			else if (isJs()) {
				data(substituteModals(data()));
			}
			headers().put("Content-Length", "" + data().length);
			setRawHeaders(getRebuiltHeaderBytes());
		}
	}

	private boolean isJs() {
		if (contentType() == null
				|| contentType().toLowerCase().indexOf("application/x-javascript") != -1) {
			return hasJsContent();
		}
		return false;
	}

	private byte[] getModifiedData() throws IOException {
		ByteArrayOutputStream s = new ByteArrayOutputStream();
		s.write(INJECT_TOP.getBytes());
		s.write(substituteModals(data()));
		s.write(INJECT_BOTTOM.getBytes());
		s.flush();
		return s.toByteArray();
	}

	byte[] substituteModals(byte[] content) {
		String s = new String(content);
		s = s.replaceAll("(\\W+)((alert)|(confirm)|(prompt))\\s*\\(", "$1sahi_$2(");
		return s.getBytes();
	}

	private boolean isHTML() {
		if (contentType() == null
				|| contentType().toLowerCase().indexOf("text/html") != -1) {
			return hasHtmlContent();
		}
		return false;
	}

	private boolean hasJsContent() {
		int length = 500;
		if (data().length < length)
			length = data().length;
		String s = new String(data(), 0, length).toLowerCase();
		return s.indexOf("var ") != -1 || s.indexOf("function ") != -1;
	}
	
	private boolean hasHtmlContent() {
		int length = 500;
		if (data().length < length)
			length = data().length;
		String s = new String(data(), 0, length).toLowerCase();
		return s.indexOf("<html") != -1 
				|| s.indexOf("<body") != -1
				|| s.indexOf("<table") != -1
				|| s.indexOf("<script") != -1
				|| s.indexOf("<form") != -1;
	}
}
