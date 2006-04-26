package net.sf.sahi.request;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import net.sf.sahi.StreamHandler;
import net.sf.sahi.session.Session;
import net.sf.sahi.test.SahiTestSuite;
import net.sf.sahi.util.Utils;

/**
 * User: nraman Date: May 13, 2005 Time: 10:01:13 PM
 */
public class HttpRequest extends StreamHandler {
	private String host;
	private int port;
	private String uri;
	private String queryString = null;
	private Map params = new HashMap();
	private Map cookies = null;
	private static final Logger logger = Logger.getLogger("net.sf.sahi.request.HttpRequest");
	private final boolean isSSLSocket;

	public HttpRequest(InputStream in) throws IOException {
		this(in, false);
	}
	public HttpRequest(InputStream in, boolean isSSLSocket) throws IOException {
		this.isSSLSocket = isSSLSocket;
		populateHeaders(in, true);
		if (isPost())
			populateData(in);
		if (isPost() || isGet() || isConnect()) {
			setHostAndPort();
			setUri();
		}
		logger.fine("\nFirst line:"+firstLine());
		logger.fine("isSSL="+isSSL());
	}

	public String host() {
		return host;
	}

	public int port() {
		return port;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public boolean isPost() {
		boolean isPost = "post".equalsIgnoreCase(method());
		return isPost;
	}

	public boolean isGet() {
		boolean isPost = "get".equalsIgnoreCase(method());
		return isPost;
	}

	public boolean isConnect() {
		boolean isConnect = "connect".equalsIgnoreCase(method());
		return isConnect;
	}

	public boolean isSSL() {
		return isSSLSocket || (firstLine().indexOf("HTTPS") != -1) || isConnect();
	}

	public String method() {
		return firstLine().substring(0, firstLine().indexOf(" "));
	}

	private void setUri() {
		String withHost = firstLine().substring(firstLine().indexOf(" "),
			firstLine().lastIndexOf(" ")).trim();
		uri = withHost;
		int indexOfHost = withHost.indexOf(host);
		if (indexOfHost != -1) {
			int indexOfSlash = withHost.indexOf("/", indexOfHost + 3);
			if (indexOfSlash != -1) // will happen when the host is embedded in
				// the querystring too.
				uri = withHost.substring(indexOfSlash);
		}
	}

	public String uri() {
		return uri;
	}

	public String protocol() {
		return firstLine().substring(firstLine().lastIndexOf(" "));
	}

	private void setHostAndPort() {
		String hostWithPort = (String) getLastSetValueOfHeader("Host");
		host = hostWithPort;
		port = 80;
		if (isSSL()) port = 443;
		int indexOfColon = hostWithPort.indexOf(":");
		if (indexOfColon != -1) {
			host = hostWithPort.substring(0, indexOfColon);
			try {
				port = Integer.parseInt(hostWithPort
						.substring(indexOfColon + 1).trim());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		host = SahiTestSuite.stripSah(host);
	}

	private void setQueryString() {
		if (uri == null)
			return;
		int qIx = uri.indexOf("?");
		if (qIx != -1 && qIx + 1 < uri.length()) {
			queryString = uri.substring(qIx + 1);
			return;
		}
		queryString = "";
		return;
	}

	private void setGetParameters() {
		StringTokenizer tokenizer = new StringTokenizer(queryString(), "&");
		while (tokenizer.hasMoreTokens()) {
			String keyVal = tokenizer.nextToken();
			int eqIx = keyVal.indexOf('=');
			if (eqIx != -1) {
				String key = keyVal.substring(0, eqIx);
				String value = "";
				if (eqIx + 1 <= keyVal.length())
					value = keyVal.substring(eqIx + 1);
				try {
					params.put(key, URLDecoder.decode(value, "UTF8"));
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public String queryString() {
		if (queryString == null) {
			setQueryString();
		}
		return queryString;
	}

	public String getParameter(String key) {
		if (params.size() == 0) {
			setGetParameters();
		}
		return (String) params.get(key);
	}

	private void setCookies() {
		cookies = new LinkedHashMap();
		String cookieString = (String) getLastSetValueOfHeader("Cookie");
		if (cookieString == null)
			return;
		StringTokenizer tokenizer = new StringTokenizer(cookieString, ";");
		while (tokenizer.hasMoreTokens()) {
			String keyVal = tokenizer.nextToken();
			int eqIx = keyVal.indexOf('=');
			if (eqIx != -1) {
				String key = keyVal.substring(0, eqIx).trim();
				String value = "";
				if (eqIx + 1 <= keyVal.length())
					value = keyVal.substring(eqIx + 1).trim();
				cookies.put(key, value);
			}
		}
	}

	public String getCookie(String key) {
		if (cookies == null) {
			setCookies();
		}
		return (String) cookies.get(key);
	}

	String rebuildCookies() {
		return rebuildCookies(cookies);
	}

	static String rebuildCookies(Map cookies2) {
        StringBuffer sb = new StringBuffer();
        if (cookies2.size() == 0) return "";
        Iterator keys = cookies2.keySet().iterator();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            String value = (String) cookies2.get(key);
            sb.append(" ").append(key).append("=").append(value).append(";");
        }
        String cookieStr = sb.toString().trim();
        if (cookieStr.endsWith(";")) {
        	cookieStr = cookieStr.substring(0, cookieStr.length()-1);
        }
		return cookieStr;
	}

	public Map cookies() {
		if (cookies == null) {
			setCookies();
		}
		return cookies;
	}

	public HttpRequest modifyForFetch() {
		setFirstLine(firstLine().replaceAll("HTTP/1.1", "HTTP/1.0"));
        removeHeader("Proxy-Connection");
        removeHeader("Accept-Encoding");
        removeHeader("Keep-Alive");
        removeHeader("ETag");
        removeHeader("If-Modified-Since");
        removeHeader("If-None-Match");
        setHeader("Connection", "close");
		cookies().remove("sahisid");
		setHeader("Cookie", rebuildCookies());
        setRawHeaders(getRebuiltHeaderBytes());
		logger.fine("\n------------\n\nRequest Headers:\n"+headers());
		return this;
	}

	public Session session() {
		String sessionId = null;
		sessionId = getParameter("sahisid");
		// System.out.println("1:"+sessionId);
		if (Utils.isBlankOrNull(sessionId))
			sessionId = getCookie(new String("sahisid"));
		if (Utils.isBlankOrNull(sessionId))
			sessionId = "sahi_" + System.currentTimeMillis();
		// System.out.println("2:"+sessionId);
		return Session.getInstance(sessionId);
	}
}
