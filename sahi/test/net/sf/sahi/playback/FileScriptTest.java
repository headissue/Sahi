package net.sf.sahi.playback;

import junit.framework.TestCase;

public class FileScriptTest extends TestCase {
	private static final long serialVersionUID = -1687495110846340213L;
	public void testFQN() {
		FileScript fileScript = new MockFileScript("c:/abc/def/a.sah");
		assertEquals("c:/abc/def/b.sah", fileScript.getFQN("b.sah"));
	}
	private class MockFileScript extends FileScript{
		public MockFileScript(String fileName) {
			super(fileName);
		}

		protected void loadScript(String fileName) {}
	}
}
