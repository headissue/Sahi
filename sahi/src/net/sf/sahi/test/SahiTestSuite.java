/**
 * Sahi - Web Automation and Test Tool
 *
 * Copyright  2006  V Narayan Raman
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.sf.sahi.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;

import net.sf.sahi.config.Configuration;
import net.sf.sahi.issue.IssueCreator;
import net.sf.sahi.issue.IssueReporter;
import net.sf.sahi.report.Report;
import net.sf.sahi.report.SahiReporter;
import net.sf.sahi.rhino.RhinoScriptRunner;
import net.sf.sahi.rhino.ScriptRunner;
import net.sf.sahi.session.Session;
import net.sf.sahi.session.Status;
import net.sf.sahi.util.ProxySwitcher;
import net.sf.sahi.util.Utils;

public class SahiTestSuite {

	private final String suitePath;

	private final String base;

	private List<TestLauncher> tests = new ArrayList<TestLauncher>();

	private Map<String, TestLauncher> testsMap = new HashMap<String, TestLauncher>();

	private int currentTestIndex = 0;

	private final String sessionId;

	private final String browser;

	private String suiteName;

	private List<TestLauncher> finishedTests = new ArrayList<TestLauncher>();

	private List<SahiReporter> listReporter = new ArrayList<SahiReporter>();

	private IssueReporter issueReporter;

	private String browserOption;

	private int availableThreads = 0;

	private volatile boolean[] freeThreads;

	private boolean killed = false;

	private boolean isMultiThreaded;

	private String browserProcessName;

	private static HashMap<String, SahiTestSuite> suites = new HashMap<String, SahiTestSuite>();

	private HashMap<TestLauncher, TestLauncher> completed = new HashMap<TestLauncher, TestLauncher>();

	private Map<String, String> variables;

	private long lastGenerationTime = 0;

	private Semaphore lock = new Semaphore(1, true);

	private String extraInfo;
	private String initJS;
	private boolean useSystemProxy;
	private boolean isSingleSession;
	private String logFolderName;

	private String junitLogDir;
	private String htmlLogDir;
	private String tm6LogDir;

	private String singleSessionChildSessionId;

	private BrowserLauncher singleSessionBrowserLauncher;

	public SahiTestSuite(final String suitePath, final String base,
			final String browser, final String sessionId,
			final String browseroption, String browserProcessName, boolean isSingleSession) {
		this.suitePath = suitePath;
		this.base = base;
		this.browser = browser;
		this.sessionId = Utils.stripChildSessionId(sessionId);
		this.singleSessionChildSessionId = Utils.addChildSessionId(sessionId);

		this.browserOption = browseroption;
		this.browserProcessName = browserProcessName;
		this.isSingleSession = isSingleSession;
		this.variables = new HashMap<String, String>();
		setSuiteName();
		this.logFolderName = Utils.createLogFileName(suiteName);
		suites.put(this.sessionId, this);
	}
	
	public Map<String, String> getInfo(){
		Map<String, String> info = new HashMap<String, String>();
		info.put("suitePath", suitePath);
		info.put("base", base);
		info.put("browser", browser);
		info.put("sessionId", sessionId);
		info.put("browserOption", browserOption);
		info.put("browserProcessName", browserProcessName);
		info.put("suiteName", suiteName);		
		return info;		
	}

	public String getInfoJSON(){
		Map<String, String> info = getInfo();
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		int count = 0;
		for (Iterator<String> iterator = info.keySet().iterator(); iterator.hasNext();) {
			if (count++ != 0) sb.append(",");
			String key = (String) iterator.next();
			sb.append(key);
			sb.append(":");
			sb.append("\"" + Utils.makeString((String) info.get(key)) + "\"");
		}
		sb.append("}");
		return sb.toString();
	}
	
	public void loadScripts() {
		this.tests = new SuiteLoader(suitePath, base).getListTest();
		System.out.println(">>>>>>                Tests size = " + this.tests.size());
		for (Iterator<TestLauncher> iterator = tests.iterator(); iterator.hasNext();) {
			TestLauncher launcher = (TestLauncher) iterator.next();
			prepareTestLauncher(launcher);
			testsMap.put(launcher.getChildSessionId(), launcher);
		}
	}

	private void prepareTestLauncher(TestLauncher launcher) {
		launcher.setSessionId(sessionId, isSingleSession ? singleSessionChildSessionId : Utils.addChildSessionId(sessionId));
		launcher.setBrowser(browser);
		launcher.setBrowserOption(browserOption);
		launcher.setBrowserProcessName(browserProcessName);
		launcher.setIsSingleSession(isSingleSession);
	}

	public List<SahiReporter> getListReporter() {
		return listReporter;
	}

	public static SahiTestSuite getSuite(final String sessionId) {
		return (SahiTestSuite) suites.get(Utils.stripChildSessionId(sessionId));
	}

	private void executeTest(final int threadNo) throws Exception {
		TestLauncher test = (TestLauncher) tests.get(currentTestIndex);
		test.setThreadNo(threadNo, isMultiThreaded);
		Session session = Session.getInstance(test.getChildSessionId());
		session.touch();
		test.execute(session);
		currentTestIndex++;
	}

	public void notifyComplete(final TestLauncher launcher) {
		if (completed.containsKey(launcher)) return;
		try {
			Thread.sleep(Configuration.getTimeBetweenTestsInSuite());
		} catch (InterruptedException e) {
			e.printStackTrace();
		}		
		notifyComplete2(launcher);
		try {
			generateSuiteReport(finishedTests, false);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void notifyComplete2(final TestLauncher launcher) {
		if (completed.containsKey(launcher))
			return;
		launcher.kill();
		synchronized (this){
			try {
				completed.put(launcher, launcher);
				finishedTests.add(launcher);
				availableThreads++;
				freeThreads[launcher.getThreadNo()] = true;
			} finally {
				this.notify();
			}
		}
	}

	private void setSuiteName() {
		this.suiteName = Utils.makePathOSIndependent(suitePath);
		int lastIndexOfSlash = suiteName.lastIndexOf("/");
		if (lastIndexOfSlash != -1) {
			this.suiteName = suiteName.substring(lastIndexOfSlash + 1);
		}
	}
	
	public String getSuiteName(){
		return this.suiteName;
	}

	private void markSuiteStatus() {
		Status status = finishedTests.size() > 0 ? Status.SUCCESS : Status.FAILURE;
		Session session;
		for (Iterator<TestLauncher> iterator = tests.iterator(); iterator.hasNext();) {
			TestLauncher testLauncher = (TestLauncher) iterator.next();
			RhinoScriptRunner scriptRunner = testLauncher.getScriptRunner();
			if (scriptRunner == null || scriptRunner.hasErrors()) {
				status = Status.FAILURE;
				break;
			}
		}
		session = Session.getInstance(this.sessionId);
		session.setStatus(status);
	}

	public void run() {
		Session session = Session.getInstance(this.sessionId);
		session.setStatus(Status.RUNNING);
		if (useSystemProxy) {
			ProxySwitcher.setSahiAsProxy();
		}
		new Thread(new Culler(this)).start();
		executeSuite();
	}

	public void finishCallBack() {
		try {
			markSuiteStatus();
			generateSuiteReport(tests, true);
			createIssues();
			cullInactiveTests();
		} finally {
			if (useSystemProxy) {
				ProxySwitcher.revertSystemProxy();
			}
			suites.remove(sessionId);
		}
	}

	void cullInactiveTests() {
		Iterator<String> keys = testsMap.keySet().iterator();
		long now = System.currentTimeMillis();
		long inactivityLimit = Configuration.getMaxInactiveTimeForScript();
		while (keys.hasNext()) {
			String sessionId = (String) keys.next();
			Session session = Session.getExistingInstance(sessionId);
			if (session == null) continue;
			long lastActiveTime = session.lastActiveTime();
			Status status = session.getStatus();
			if (status != Status.SUCCESS && status != Status.FAILURE
					&& status != Status.INITIAL
					&& now - lastActiveTime > inactivityLimit) {
				String message = "*** Forcefully terminating script. \nNo response from browser within expected time ("
						+ inactivityLimit / 1000 + " seconds).";
				System.out.println(message);
				ScriptRunner scriptRunner = session.getScriptRunner();
				scriptRunner.setStatus(Status.FAILURE);
				Report report = scriptRunner.getReport();
				if (report != null) {
					report.addResult(message, "ERROR", "", "");
				}
			}
		}
	}

	private void createIssues() {
		Session session = Session.getInstance(sessionId);
		if (Status.FAILURE.equals(session.getStatus()) && issueReporter != null) {
			issueReporter.reportIssue(tests);
		}
	}

	private synchronized void executeSuite() {
		try {
			launchBrowserForSingleSession();
		} catch (Exception e1) {
			//e1.printStackTrace();
			abort();
			return;
		}
		while (currentTestIndex < tests.size()) {
			if (killed) {
				return;
			}
			for (; availableThreads > 0 && currentTestIndex < tests.size(); availableThreads--) {
				int freeThreadNo = getFreeThreadNo();
				if (freeThreadNo != -1) {
					freeThreads[freeThreadNo] = false;
					try {
						this.executeTest(freeThreadNo);
					} catch (Exception e) {
						abort();
						return;
					}
				}
			}
			try {
				this.wait(600000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		killBrowserForSingleSession(false);
	}

	private void abort() {
		kill();
		finishCallBack();
	}

	public void launchBrowserForSingleSession() throws Exception {
		if (isSingleSession) {
			singleSessionBrowserLauncher = new BrowserLauncher(browser, browserProcessName, browserOption, useSystemProxy);
			singleSessionBrowserLauncher.openURL(singleSessionBrowserLauncher.getPlayerAutoURL(singleSessionChildSessionId, base, isSingleSession));
		}
	}

	public Status executeTestForSingleSession(String testName, String startURL) throws Exception{
		TestLauncher testLauncher = new TestLauncher(testName, startURL);
		prepareTestLauncher(testLauncher);
		testLauncher.setThreadNo(0, isMultiThreaded);
		Session session = Session.getInstance(testLauncher.getChildSessionId());
		session.touch();
		tests.add(testLauncher);
		testLauncher.execute(session, false, true);
		return testLauncher.getStatus();
	}

	public void killBrowserForSingleSession(boolean isSingleTest) {
		if (isSingleSession) {
			singleSessionBrowserLauncher.kill();
			if(isSingleTest)
				finishCallBack();
		}
	}

	
	private int getFreeThreadNo() {
		for (int i = 0; i < freeThreads.length; i++) {
			if (freeThreads[i]) {
				return i;
			}
		}
		return -1;
	}

	private void generateSuiteReport(List<TestLauncher> listOfTests, boolean force) {
		try {
			if (force) {
				lock.acquire();
			} else {
				long now = System.currentTimeMillis();
				if (now - lastGenerationTime < 5000) return;
				lastGenerationTime = now;
				if (!lock.tryAcquire()) return;
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		try{
			for (Iterator<SahiReporter> iterator = listReporter.iterator(); iterator.hasNext();) {
				SahiReporter reporter = (SahiReporter) iterator.next();
				reporter.generateSuiteReport(listOfTests);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		lock.release();
	}

	public void setAvailableThreads(final int availableThreads) {
		this.availableThreads = availableThreads;
		this.isMultiThreaded = availableThreads > 1;
		freeThreads = new boolean[availableThreads];
		int len = freeThreads.length;
		for (int i = 0; i < len; i++) {
			freeThreads[i] = true;
		}
	}

	public void addReporter(final SahiReporter reporter) {
		reporter.setSuiteName(suiteName);
		listReporter.add(reporter);
	}

	public void addIssueCreator(final IssueCreator issueCreator) {
		if (issueReporter == null) {
			issueReporter = new IssueReporter(suiteName);
		}
		issueReporter.addIssueCreator(issueCreator);
	}

	synchronized boolean isRunning() {
		return finishedTests.size() < tests.size() && !killed;
	}

	public void kill() {
		System.out.println("Shutting down ...");
		killed = true;
	}

    public String getVariable(final String name) {
//    	System.out.println("get name="+name);
//    	System.out.println("get value="+(String) (variables.get(name)));
        return (String) (variables.get(name));
    }

    public void removeVariables(final String pattern) {
        for (Iterator<String> iterator = variables.keySet().iterator(); iterator.hasNext();) {
            String s = (String) iterator.next();
            if (s.matches(pattern)) {
                iterator.remove();
            }
        }
    }

    public void setVariable(final String name, final String value) {
//    	System.out.println("set name="+name);
//    	System.out.println("set value="+value);
        variables.put(name, value);
    }

	public void setExtraInfo(String extraInfo) {
		this.extraInfo = extraInfo;
	}
	
	public String getExtraInfo(){
		return this.extraInfo;
	}

	public void setInitJS(String initJS) {
		this.initJS = initJS;		
	}

	public String getInitJS() {
		return initJS;
	}

	public void setUseSystemProxy(boolean useSystemProxy) {
		this.useSystemProxy = useSystemProxy;
	}

	public String getLogFolderName() {
		return logFolderName;
	}

	public void setJunitLogDir(String logDir) {
		this.junitLogDir = logDir;
	}

	public String getJunitLogDir(){
		return junitLogDir;
	}
	
	public void setHtmlLogDir(String logDir) {
		this.htmlLogDir = logDir;
	}

	public String getHtmlLogDir(){
		return htmlLogDir;
	}
	
	public void setTM6LogDir(String logDir) {
		this.tm6LogDir = logDir;
	}
	
	public String getTM6LogDir(){
		return tm6LogDir;
	}
}

class Culler implements Runnable {
	private SahiTestSuite suite;

	Culler(SahiTestSuite suite) {
		this.suite = suite;
	}

	public void run() {
		waitForSuiteCompletion();
	}

	private void waitForSuiteCompletion() {
		while (suite.isRunning()) {
			suite.cullInactiveTests();
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		suite.finishCallBack();
	}
}
