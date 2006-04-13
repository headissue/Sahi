package com.sahi;

import junit.framework.TestCase;

public class ProxyProcessorHelperTest extends TestCase {
	final String uri = "/_s_/dyn/Log_highlight/sahi_demo_include.sah?n=2";
	
	public void testScriptFileNamefromURI() {
		assertEquals("../scripts/sahi_demo_include.sah", ProxyProcessorHelper.scriptFileNamefromURI(uri, "/Log_highlight/"));		
	}

	public void testScriptFileNamefromURI2() {
		final String uri2 = "/_s_/dyn/scripts/sahi_demo_include.sah?n=2";
		assertEquals("../scripts/sahi_demo_include.sah", ProxyProcessorHelper.scriptFileNamefromURI(uri2, "/scripts/"));		
	}

}
