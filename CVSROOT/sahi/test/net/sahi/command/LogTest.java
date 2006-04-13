package com.sahi.command;

import junit.framework.TestCase;

public class LogTest extends TestCase {	
	public void testHighlight() {
		assertEquals("<b>one</b>\ntwo\nthree\nfour", Log.highlight("one\ntwo\nthree\nfour", 1));
//		assertEquals("", ProxyProcessorHelper.highlight("one\ntwo\nthree\nfour", 1));
		assertEquals("one\n<b>two</b>\nthree\nfour", Log.highlight("one\ntwo\nthree\nfour", 2));
		assertEquals("one\ntwo\nthree\n<b>four</b>", Log.highlight("one\ntwo\nthree\nfour", 4));
//		assertEquals("", ProxyProcessorHelper.highlight("one\ntwo\nthree\nfour", 4));
	}
}
