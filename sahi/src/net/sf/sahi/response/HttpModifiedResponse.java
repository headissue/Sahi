
package net.sf.sahi.response;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import net.sf.sahi.util.Utils;

/**
 * User: nraman Date: May 14, 2005 Time: 1:43:05 AM
 */
public class HttpModifiedResponse extends HttpResponse {
	boolean isSSL = false;
	private static byte[] INJECT_TOP_SSL = null;
	private static byte[] INJECT_BOTTOM_SSL = null;
	private static byte[] INJECT_TOP;
	private static byte[] INJECT_BOTTOM;
	static {
		INJECT_TOP = Utils.readFile("../config/inject_top.txt");
		INJECT_BOTTOM = Utils.readFile("../config/inject_bottom.txt");
		INJECT_TOP_SSL = makeHTTPS(INJECT_TOP);
		INJECT_BOTTOM_SSL = makeHTTPS(INJECT_BOTTOM);
	}

	private static byte[] makeHTTPS(byte[] content) {
		return new String(content).replaceAll("http", "https").getBytes();
	}

	public HttpModifiedResponse(InputStream in, boolean isSSL) throws IOException {
		super(in);
		this.isSSL = isSSL;
		if (firstLine().indexOf("30") == -1) { // Response code other than
			boolean html = isHTML();
			boolean js = isJs();
			if (html || js) {
				if (html) {
					removeHeader("Transfer-Encoding");
					removeHeader("ETag");
					removeHeader("Last-Modified");
					setHeader("Cache-Control", "no-cache");
					setHeader("Pragma", "no-cache");
					setHeader("Expires", "-1");
					setData(getModifiedData());
				} else if (js) {
					setData(substituteModals(data()));
				}
				setHeader("Content-Length", "" + data().length);
				resetRawHeaders();
			}
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
		s.write(isSSL ? INJECT_TOP_SSL : INJECT_TOP);
		s.write(substituteModals(data()), ix, data().length - ix);
		s.write(isSSL ? INJECT_BOTTOM_SSL : INJECT_BOTTOM);
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
		return s.indexOf("<?xml") != -1 || s.indexOf("<!doctype") != -1;

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
		if (contentType != null && contentType.toLowerCase().indexOf("text/html") != -1 && hasNoContent()) {
			return true;
		}
		if (contentType == null
				|| contentType.toLowerCase().indexOf("text/html") != -1) {
			return hasHtmlContent();
		}
		return false;
	}

	private boolean hasNoContent() {
		return "".equals(getSampleContent().trim());
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
