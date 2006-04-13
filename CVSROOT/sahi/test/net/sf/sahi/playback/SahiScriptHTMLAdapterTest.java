package com.sahi.playback;

import junit.framework.TestCase;

public class SahiScriptHTMLAdapterTest extends TestCase{
	public void testCreateHTML() {
		assertEquals("<pre>a<br/>b</pre>", SahiScriptHTMLAdapter.createHTML("a\r\nb"));
	}
}
