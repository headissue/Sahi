package com.sahi.util;

import com.sahi.util.URLParser;

import junit.framework.TestCase;

public class URLParserTest extends TestCase {
	final String uri = "/_s_/dyn/Log_highlight/sahi_demo_include.sah?n=2";

	public void testScriptFileNamefromURI() {
		assertEquals("../scripts/sahi_demo_include.sah", URLParser
				.scriptFileNamefromURI(uri, "/Log_highlight/"));
	}

	public void testScriptFileNamefromURI2() {
		final String uri2 = "/_s_/dyn/scripts/sahi_demo_include.sah?n=2";
		assertEquals("../scripts/sahi_demo_include.sah", URLParser
				.scriptFileNamefromURI(uri2, "/scripts/"));
	}

	public void testGetCommandFromUri() {
		assertNull(URLParser
				.getCommandFromUri("http://localhost:1000/_s_/spr/Log?msg=aa"));
		assertEquals(
				"Log",
				URLParser
						.getCommandFromUri("http://localhost:1000/_s_/dyn/Log?msg=aa/confuseWithSlash"));
		assertEquals(
				"Player_setScriptFile",
				URLParser
						.getCommandFromUri("http://localhost:1000/_s_/dyn/Player_setScriptFile?file=bb"));
		assertEquals(
				"Player_setScriptFile",
				URLParser
						.getCommandFromUri("http://localhost:1000/_s_/dyn/Player_setScriptFile/"));
		assertEquals(
				"SessionState",
				URLParser
						.getCommandFromUri("http://www.sahidomain.com/_s_/dyn/SessionState/state.js"));

		assertEquals(
				"Log_highlight",
				URLParser
						.getCommandFromUri("http://www.sahidomain.com/_s_/dyn/Log_highlight/ww.sah?n=5"));
	}
}
