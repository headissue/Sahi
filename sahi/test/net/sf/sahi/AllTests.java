package net.sf.sahi;

import junit.framework.Test;
import junit.framework.TestSuite;

import net.sf.sahi.command.CommandExecuterTest;
import net.sf.sahi.playback.FileScriptTest;
import net.sf.sahi.playback.SahiScriptHTMLAdapterTest;
import net.sf.sahi.playback.SahiScriptTest;
import net.sf.sahi.playback.ScriptHandlerTest;
import net.sf.sahi.playback.URLScriptTest;
import net.sf.sahi.playback.log.PlayBackLogFormatterTest;
import net.sf.sahi.response.HttpFileResponseTest;
import net.sf.sahi.util.URLParserTest;
import net.sf.sahi.util.UtilsTest;

public class AllTests extends TestSuite {
	public AllTests(String name) {
		super(name);
	}

	public static Test suite() {
		TestSuite suite = new TestSuite();
		suite.addTestSuite(FileScriptTest.class);
        suite.addTestSuite(SahiScriptTest.class);
        suite.addTestSuite(ScriptHandlerTest.class);
        suite.addTestSuite(URLScriptTest.class);
        suite.addTestSuite(HttpFileResponseTest.class);
        suite.addTestSuite(SahiScriptHTMLAdapterTest.class);
        suite.addTestSuite(UtilsTest.class);
        suite.addTestSuite(PlayBackLogFormatterTest.class);
        suite.addTestSuite(URLParserTest.class);
        suite.addTestSuite(CommandExecuterTest.class);
		return suite;
	}

	public static void main(String[] args) {
		junit.textui.TestRunner.run(suite());
	}
}
