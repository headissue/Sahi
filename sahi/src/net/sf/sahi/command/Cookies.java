package net.sf.sahi.command;

import java.util.Properties;
import java.util.StringTokenizer;

import net.sf.sahi.config.Configuration;
import net.sf.sahi.request.HttpRequest;
import net.sf.sahi.response.HttpFileResponse;
import net.sf.sahi.response.HttpModifiedResponse2;
import net.sf.sahi.response.HttpResponse;
import net.sf.sahi.response.SimpleHttpResponse;
import net.sf.sahi.util.Utils;

public class Cookies {
	public HttpResponse showAll(final HttpRequest request) {
		final String cookies = getCookies(request);
        Properties props = new Properties();
        props.setProperty("cookies", cookies);
        return new HttpModifiedResponse2(new HttpFileResponse(Utils.concatPaths(Configuration.getHtdocsRoot(), "spr/cookies.htm"), props, false, false)
        		, request.isSSL(), "htm"
        );
	}
	
	public HttpResponse read(final HttpRequest request) {
		final String name = request.getParameter("name");
		String cookieValue = readCookie(name, request);
		cookieValue = cookieValue == null ? "null" : Utils.makeString(cookieValue);
		return new SimpleHttpResponse(cookieValue);
	}

	private String readCookie(final String name, HttpRequest request) {
		final String cookieString = getCookies(request);
		if (cookieString == null)  return null;

		StringTokenizer tokenizer = new StringTokenizer(cookieString, ";");
		while (tokenizer.hasMoreTokens()) {
			String keyVal = tokenizer.nextToken();
			int eqIx = keyVal.indexOf('=');
			if (eqIx != -1) {
				String key = keyVal.substring(0, eqIx).trim();
				String value = "";
				if (eqIx + 1 <= keyVal.length()) {
					value = keyVal.substring(eqIx + 1).trim();
				}
				
				if (key.equals(name)) return value;
			}
		}
		return null;

	}
	
	private String getCookies(final HttpRequest request) {
		return request.headers().getLastHeader("Cookie");
	}

	public HttpResponse create(final HttpRequest request) {
		final String name = request.getParameter("name");
		final String value = request.getParameter("value");
		final String path = request.getParameter("path");
		final String domain = request.getParameter("domain");
		final String expires = request.getParameter("expires");
		final SimpleHttpResponse response = new SimpleHttpResponse("");

		String cookieStr = name+"=" + value + "; ";
		if (expires != null) cookieStr += "expires=" + expires + "; ";
		if (path != null) cookieStr += "path="+ path +"; ";
		if (domain != null) cookieStr += "domain="+domain+";";
		response.addHeader("Set-Cookie", cookieStr);
        
		return response;
	}

	
	public HttpResponse delete(final HttpRequest request) {
		final String name = request.getParameter("name");
		String path = request.getParameter("path");
		if (path == null) path = "/";
		final String domain = request.host();
		final SimpleHttpResponse response = new SimpleHttpResponse("");
		if (readCookie(name, request) != null) {
			response.addHeader("Set-Cookie", name+"=; expires=Thu, 01 Jan 1970 00:00:01 GMT; path=" + path + ";");
			response.addHeader("Set-Cookie", name+"=; expires=Thu, 01 Jan 1970 00:00:01 GMT; path=" + path + "; domain="+domain+";");
			response.addHeader("Set-Cookie", name+"=; expires=Thu, 01 Jan 1970 00:00:01 GMT; path=" + path + "; domain=."+domain+";");
			response.addHeader("Set-Cookie", name+"=; expires=Thu, 01 Jan 1970 00:00:01 GMT; path=" + path + "; domain="+domain+"; secure");
			response.addHeader("Set-Cookie", name+"=; expires=Thu, 01 Jan 1970 00:00:01 GMT; path=" + path + "; domain=."+domain+"; secure");
		}
        return response;
	}
}
