package com.sahi;

import junit.framework.TestCase;

public class ProxyProcessorHelperTest extends TestCase {
	final String uri = "/_s_/dyn/highlighted/sahi_demo_include.sah?n=2";
	
	public void testScriptFileNamefromURI() {
		assertEquals("../scripts/sahi_demo_include.sah", ProxyProcessorHelper.scriptFileNamefromURI(uri, "/highlighted/"));		
	}

	public void testScriptFileNamefromURI2() {
		final String uri2 = "/_s_/dyn/scripts/sahi_demo_include.sah?n=2";
		assertEquals("../scripts/sahi_demo_include.sah", ProxyProcessorHelper.scriptFileNamefromURI(uri2, "/scripts/"));		
	}
	
	public void testHighlight() {
		assertEquals("<b>one</b>\ntwo\nthree\nfour", ProxyProcessorHelper.highlight("one\ntwo\nthree\nfour", 1));
//		assertEquals("", ProxyProcessorHelper.highlight("one\ntwo\nthree\nfour", 1));
		assertEquals("one\n<b>two</b>\nthree\nfour", ProxyProcessorHelper.highlight("one\ntwo\nthree\nfour", 2));
		assertEquals("one\ntwo\nthree\n<b>four</b>", ProxyProcessorHelper.highlight("one\ntwo\nthree\nfour", 4));
//		assertEquals("", ProxyProcessorHelper.highlight("one\ntwo\nthree\nfour", 4));
	}
}
