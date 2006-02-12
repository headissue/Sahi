package com.sahi.response;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * User: nraman Date: May 14, 2005 Time: 1:43:05 AM
 */
public class HttpModifiedResponse extends HttpResponse {
	private static final String INJECT_TOP = ""
			+ "<script src='/_s_/spr/concat.js'></script>\n"
			+ "<script src='http://www.sahidomain.com/_s_/dyn/state.js'></script>\n"
			+ "<script src='http://www.sahidomain.com/_s_/dyn/script.js'></script>\n"
			+ "<script src='/_s_/spr/playback.js'></script>\n"
			+ "<div id='sahi_div' style='display:none'></div>\n" + "";

	private static final String INJECT_BOTTOM = ""
			+ "<script src='/_s_/spr/playback.js'></script>\n" + "";

	public HttpModifiedResponse(InputStream in) throws IOException {
		super(in);
		if (firstLine().indexOf("30") == -1) { // Response code other than
			// redirects
			// removeHeader("Transfer-Encoding");
			// setHeader("Cache-Control","no-cache");
			// setHeader("Pragma","no-cache");
			// setHeader("Expires", "0");
			// removeHeader("ETag");
			// removeHeader("Last-Modified");
			if (isHTML())
				data(getModifiedData());
			else if (isJs()) {
				data(substituteModals(data()));
			}
			setHeader("Content-Length", "" + data().length);
			// System.out.println("$$$$$$$$$$$$$$");
			// System.out.println(new String(rawHeaders()));
			// System.out.println("$$$$$$$$$$$$$$");
			// System.out.println("--------------");
			// System.out.println(new String(getRebuiltHeaderBytes()));
			setRawHeaders(getRebuiltHeaderBytes());
			// System.out.println("--------------");
		}
	}

	private boolean isJs() {
		String contentType = contentType();
		if (contentType == null
				|| contentType.toLowerCase()
						.indexOf("application/x-javascript") != -1) {
			return hasJsContent();
		}
		return false;
	}

	private byte[] getModifiedData() throws IOException {
		ByteArrayOutputStream s = new ByteArrayOutputStream();
		int ix = 0;
		if (isXHTML()) {
			ix = getHTMLTagIndex();
		}
		s.write(data(), 0, ix);
		s.write(INJECT_TOP.getBytes());
		s.write(substituteModals(data()), ix, data().length-ix);
		s.write(INJECT_BOTTOM.getBytes());
		s.flush();
		return s.toByteArray();
	}

	private int getHTMLTagIndex() {
		String s = getSampleContent();
		final int ix = s.indexOf("<html");
		return ix == -1 ? 0 : ix;
	}

	private boolean isXHTML() {
		String s = getSampleContent();
		return s.indexOf("<?xml") != -1;

	}

	byte[] substituteModals(byte[] content) {
		// long start = System.currentTimeMillis();
		String s = new String(content);
		s = s.replaceAll("(\\W+)((alert)|(confirm)|(prompt))\\s*\\(",
				"$1sahi_$2(");
		// System.out.println("substituteModals took ms
		// "+(System.currentTimeMillis() - start));
		return s.getBytes();
	}

	private boolean isHTML() {
		String contentType = contentType();
		if (contentType == null
				|| contentType.toLowerCase().indexOf("text/html") != -1) {
			return hasHtmlContent();
		}
		return false;
	}

	private boolean hasJsContent() {
		String s = getSampleContent();
		return s.indexOf("var ") != -1 || s.indexOf("function ") != -1;
	}

	private boolean hasHtmlContent() {
		String s = getSampleContent();
		return s.indexOf("<html") != -1 || s.indexOf("<body") != -1
				|| s.indexOf("<table") != -1 || s.indexOf("<script") != -1
				|| s.indexOf("<form") != -1;
	}

	private String sampleContent = null;
	private String getSampleContent() {
		if (sampleContent == null) {
			int length = 500;
			if (data().length < length)
				length = data().length;
			sampleContent = new String(data(), 0, length).toLowerCase();
		}
		return sampleContent;
	}
}
