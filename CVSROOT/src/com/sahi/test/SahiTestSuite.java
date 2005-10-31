package com.sahi.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import com.sahi.util.Utils;

public class SahiTestSuite {
	private final String suiteURL;
	private final String base;
	private List tests = new ArrayList();
	private Map testsMap = new HashMap();
	private int currentTestIndex = 0;
	private final String sessionId;
	private final String browser;
	private String suiteName;
	private int finishedTests = 0;
	private String suiteLogDir;
	private static HashMap suites = new HashMap();

	public SahiTestSuite(String suiteURL, String base, String browser, String sessionId) {
		this.suiteURL = suiteURL;
		this.base = base;
		this.browser = browser;
		this.sessionId = stripSah(sessionId);
		setNameAndBase(suiteURL);
		init();
		suites.put(this.sessionId, this);
	}

	public static SahiTestSuite getSuite(String sessionId) {
		return (SahiTestSuite) suites.get(stripSah(sessionId));
	}
	
	
	public static String stripSah(String s) {
		return s.replaceFirst("sahix[0-9]+x", "");
	}

	private void init() {
		String contents = new String(Utils.readFile(suiteURL));
		StringTokenizer tokens = new StringTokenizer(contents, "\n");
		while (tokens.hasMoreTokens()) {
			String line = tokens.nextToken();
			processLine(line.trim());
		}
	}

	private void processLine(String line) {
		if (line.startsWith("#") || line.startsWith("//"))
			return;
		int ix = line.indexOf(' ');
		if (ix == -1)
			ix = line.indexOf('\t');
		if (ix != -1) {
			String testName = line.substring(0, ix).trim();
			String startURL = line.substring(ix).trim();
			if (!startURL.startsWith("http://"))
				startURL = base + startURL;
			SahiTest sahiTest = new SahiTest(testName, startURL, base, browser, sessionId);
			tests.add(sahiTest);
			testsMap.put(testName, sahiTest);
		}
	}

	public synchronized boolean executeNext() {
		boolean hasMoreTests = currentTestIndex < tests.size();
		if (hasMoreTests) {
			SahiTest test = (SahiTest) tests.get(currentTestIndex);
			currentTestIndex++;
			test.execute();
		}
		return hasMoreTests;
	}

	public boolean isRunning() {
		return (finishedTests < tests.size());
	}

	public synchronized void stop(String scriptName) {
		((SahiTest)(testsMap.get(scriptName))).stop();
		finishedTests++;
	}

	public String getSuiteName() {
		return suiteName;
	}

	public String getSuiteURL() {
		return suiteURL;
	}

	protected void setNameAndBase(String url) {
		this.suiteName = url;
		int lastIndexOfSlash = url.lastIndexOf("/");
		if (lastIndexOfSlash != -1) {
			this.suiteName = url.substring(lastIndexOfSlash + 1);
		}
	}
	public String getSuiteLogDir() {
		if (suiteLogDir == null)
			suiteLogDir = Utils.createLogFileName(getSuiteName());
		return suiteLogDir;
	}	
}
