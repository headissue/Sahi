package net.sf.sahi.test;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import net.sf.sahi.util.Utils;

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


	public SahiTestSuite(String suiteURL, String base, String browser,
			String sessionId) {
		this.suiteURL = suiteURL;
		this.base = base;
		this.browser = browser;
		this.sessionId = stripSah(sessionId);
		setSuiteName(suiteURL);
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
			try {
				processLine(line.trim());
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		}
	}

	private void processLine(String line) throws MalformedURLException {
		if (line.startsWith("#") || line.startsWith("//") || line.trim().equals(""))
			return;
		int ix = line.indexOf(' ');
		if (ix == -1)
			ix = line.indexOf('\t');
		String testName = null;
		String startURL = null;
		if (ix != -1) {
			testName = line.substring(0, ix).trim();
			startURL = line.substring(ix).trim();
		} else {
			testName = line;
			startURL = "";
		}
		if (!(startURL.startsWith("http://") || startURL.startsWith("https://"))) {
				startURL = new URL(new URL(base), startURL).toString();
		}
		TestLauncher sahiTest = new TestLauncher(testName, startURL, browser, sessionId);
		tests.add(sahiTest);
		testsMap.put(testName, sahiTest);
	}

	public synchronized boolean executeNext() {
		boolean hasMoreTests = currentTestIndex < tests.size();
		if (hasMoreTests) {
			TestLauncher test = (TestLauncher) tests.get(currentTestIndex);
			currentTestIndex++;
			test.execute();
		}
		return hasMoreTests;
	}

	public boolean isRunning() {
		return (finishedTests < tests.size());
	}

	public synchronized void stop(String scriptName) {
		((TestLauncher) (testsMap.get(scriptName))).stop();
		finishedTests++;
	}

	public String getSuiteName() {
		return suiteName;
	}

	public String getSuiteURL() {
		return suiteURL;
	}

	protected void setSuiteName(String url) {
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
