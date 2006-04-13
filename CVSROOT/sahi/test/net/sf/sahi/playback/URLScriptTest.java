package com.sahi.playback;

import junit.framework.TestCase;

public class URLScriptTest extends TestCase {
	private static final long serialVersionUID = 9089031044975786876L;

	public void testFQN() {
		MockURLScript urlScript = new MockURLScript("http://abc/def/a.sah");
		assertEquals("http://abc/def/b.sah", urlScript.getFQN("b.sah"));
	}

	public void testFQNWithFullURL() {
		MockURLScript urlScript = new MockURLScript("http://abc/def/a.sah");
		assertEquals("http://xxx/b.sah", urlScript.getFQN("http://xxx/b.sah"));
	}

	private class MockURLScript extends URLScript {
		public MockURLScript(String fileName) {
			super(fileName);
		}

		protected void loadScript(String fileName) {
		}
	}

}
