package net.sf.sahi.request;

import java.util.LinkedHashMap;
import java.util.Map;
import java.net.URLDecoder;
import java.io.UnsupportedEncodingException;

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


    public void testUnicode() throws UnsupportedEncodingException {
        String s = URLDecoder.decode("abc", "sadalkdjlaksjdfl");
    }

    public void testSetUri(){
        assertEquals("/login?service=http://www.hostname.com/landing",
                new HttpRequest().stripHostName("/login?service=http://www.hostname.com/landing", "www.hostname.com", false));
        assertEquals("/login?service=http://www.hostname.com/landing",
                new HttpRequest().stripHostName("http://www.hostname.com/login?service=http://www.hostname.com/landing",
                        "www.hostname.com", false));
        assertEquals("/netdirector/",
                new HttpRequest().stripHostName("http://localhost:8080/netdirector/", "localhost", false));
        assertEquals("/netdirector/",
                new HttpRequest().stripHostName("/netdirector/", "localhost", false));
        assertEquals("/netdirector/?service=http://localhost:8080/landing",
                new HttpRequest().stripHostName("http://localhost:8080/netdirector/?service=http://localhost:8080/landing", "localhost", false));
    }
}
