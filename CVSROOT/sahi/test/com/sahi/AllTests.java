package com.sahi;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.sahi.playback.FileScriptTest;
import com.sahi.playback.SahiScriptTest;
import com.sahi.playback.ScriptHandlerTest;
import com.sahi.playback.URLScriptTest;
import com.sahi.record.SahiScriptFormatTest;
import com.sahi.response.HttpFileResponseTest;

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
        suite.addTestSuite(SahiScriptFormatTest.class);
        suite.addTestSuite(HttpFileResponseTest.class);
		return suite;
	}

	public static void main(String[] args) {
		junit.textui.TestRunner.run(suite());
	}	
}
