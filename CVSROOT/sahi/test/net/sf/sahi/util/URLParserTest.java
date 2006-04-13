package net.sf.sahi.util;

import net.sf.sahi.util.URLParser;

import junit.framework.TestCase;

public class URLParserTest extends TestCase {
	final String uri = "/_s_/dyn/Log_highlight/sahi_demo_include.sah?n=2";

	public void testScriptFileNamefromURI() {
		assertEquals("../scripts/sahi_demo_include.sah", URLParser.scriptFileNamefromURI(uri, "/Log_highlight/"));
	}

	public void testScriptFileNamefromURI2() {
		final String uri2 = "/_s_/dyn/scripts/sahi_demo_include.sah?n=2";
		assertEquals("../scripts/sahi_demo_include.sah", URLParser.scriptFileNamefromURI(uri2, "/scripts/"));
	}

}
