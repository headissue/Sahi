package net.sf.sahi.response;

import junit.framework.TestCase;

public class MimeTypeTest extends TestCase {
	public void testGetExtension() {
		assertEquals(".js", MimeType.getExtension("aa.js"));
		assertEquals(".JS", MimeType.getExtension("aa.JS"));
		assertEquals(".htm", MimeType.getExtension("aa.htm"));
		assertEquals("", MimeType.getExtension("aa"));
	}

	public void testMimeTypeMapping() {
		assertEquals("application/x-javascript", MimeType.get(".JS", "text/plain"));
		assertEquals("text/html", MimeType.get(".htm", "text/plain"));
	}
	
	public void testGetMimeTypeOfFile() {
		assertEquals("application/x-javascript", MimeType.getMimeTypeOfFile("qq.JS"));
		assertEquals("text/html", MimeType.getMimeTypeOfFile("a.b.c.htm"));
		assertEquals("text/plain", MimeType.getMimeTypeOfFile("xxxx"));
	}
}
