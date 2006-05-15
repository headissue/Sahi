package net.sf.sahi.request;

import java.util.LinkedHashMap;
import java.util.Map;

import junit.framework.TestCase;

public class HttpRequestTest extends TestCase {

	public void testRebuildCookies() {
		Map cookies = new LinkedHashMap();
		cookies.put("_session_id", "cookieVal");
		assertEquals("_session_id=cookieVal", HttpRequest.rebuildCookies(cookies));
		cookies.put("sahisid", "cookieVal2");
		assertEquals("_session_id=cookieVal; sahisid=cookieVal2", HttpRequest.rebuildCookies(cookies));
		cookies.put("cookieName3", "cookieVal3");
		assertEquals("_session_id=cookieVal; sahisid=cookieVal2; cookieName3=cookieVal3", HttpRequest.rebuildCookies(cookies));
	}
}
