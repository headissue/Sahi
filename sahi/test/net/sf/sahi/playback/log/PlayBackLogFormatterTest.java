package net.sf.sahi.playback.log;

import junit.framework.TestCase;

public class PlayBackLogFormatterTest extends TestCase {

	public static void main(String[] args) {
		junit.textui.TestRunner.run(PlayBackLogFormatterTest.class);
	}

	public void testLinkGeneration() {
		String s = "_assertNotNull(_checkbox(\"c1[1]\")); [sahi_demo.sah&n=58]";
		assertEquals("<a class=\"INFO\" href=\"/_s_/dyn/Log_highlight?href=sahi_demo.sah&n=58\">_assertNotNull(_checkbox(\"c1[1]\"));</a>", new PlayBackLogFormatter().createLink(s, "INFO"));
	}
}
