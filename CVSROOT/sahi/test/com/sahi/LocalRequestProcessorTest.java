package com.sahi;

import junit.framework.TestCase;

public class LocalRequestProcessorTest extends TestCase {
	public void testGetCommandFromUri() {
		assertNull(new LocalRequestProcessor()
				.getCommandFromUri("http://localhost:1000/_s_/spr/Log?msg=aa"));
		assertEquals("Log", new LocalRequestProcessor()
				.getCommandFromUri("http://localhost:1000/_s_/dyn/Log?msg=aa/confuseWithSlash"));
		assertEquals(
				"Player_setScriptFile",
				new LocalRequestProcessor()
						.getCommandFromUri("http://localhost:1000/_s_/dyn/Player_setScriptFile?file=bb"));
		assertEquals(
				"Player_setScriptFile",
				new LocalRequestProcessor()
						.getCommandFromUri("http://localhost:1000/_s_/dyn/Player_setScriptFile/"));
		assertEquals(
				"SessionState",
				new LocalRequestProcessor()
						.getCommandFromUri("http://www.sahidomain.com/_s_/dyn/SessionState/state.js"));

		assertEquals(
				"Log_highlight",
				new LocalRequestProcessor()
						.getCommandFromUri("http://www.sahidomain.com/_s_/dyn/Log_highlight/ww.sah?n=5"));
	}
}
