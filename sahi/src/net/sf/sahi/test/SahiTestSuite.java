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
import net.sf.sahi.issue.IssueCreator;
import net.sf.sahi.issue.IssueReporter;
import net.sf.sahi.report.SahiReporter;
import net.sf.sahi.session.Session;
import net.sf.sahi.session.Status;
import net.sf.sahi.util.Utils;
import net.sf.sahi.command.Player;
import net.sf.sahi.config.Configuration;

public class SahiTestSuite {

    private final String suitePath;
    private final String base;
    private List tests = new ArrayList();
    private Map testsMap = new HashMap();
    private int currentTestIndex = 0;
    private final String sessionId;
    private final String browser;
    private String suiteName;
    private int finishedTests = 0;
    private List listReporter = new ArrayList();
    private IssueReporter issueReporter;
    private String browserOption;
    private int availableThreads = 0;
    private volatile boolean[] freeThreads;
    private boolean killed = false;
    private static HashMap suites = new HashMap();

    public SahiTestSuite(final String suitePath, final String base, final String browser, final String sessionId, final String browseroption) {
        this.suitePath = suitePath;
        this.base = base;
        this.browser = browser;
        this.sessionId = Utils.stripChildSessionId(sessionId);
        this.browserOption = browseroption;

        setSuiteName();
        loadScripts();
        suites.put(this.sessionId, this);
    }

    private void loadScripts() {
        this.tests = new SuiteLoader(suitePath, base).getListTest();
        for (Iterator iterator = tests.iterator(); iterator.hasNext();) {
            TestLauncher script = (TestLauncher) iterator.next();
            script.setSessionId(sessionId);
            script.setBrowser(browser);
            script.setBrowserOption(browserOption);
            testsMap.put(script.getChildSessionId(), script);
        }
    }

    public List getListReporter() {
        return listReporter;
    }

    public static SahiTestSuite getSuite(final String sessionId) {
        return (SahiTestSuite) suites.get(Utils.stripChildSessionId(sessionId));
    }

    private void executeTest(final int threadNo) {
        TestLauncher test = (TestLauncher) tests.get(currentTestIndex);
        test.setThreadNo(threadNo);
        test.execute();
        currentTestIndex++;
    }

    public synchronized void notifyComplete(final String childSessionId) {
        TestLauncher test = ((TestLauncher) (testsMap.get(childSessionId)));
        test.stop();
        try {
            Thread.sleep(Configuration.getTimeBetweenTestsInSuite());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        finishedTests++;
        availableThreads++;
        freeThreads[test.getThreadNo()] = true;
        this.notify();
    }

    private void setSuiteName() {
        this.suiteName = suitePath;
        int lastIndexOfSlash = suitePath.lastIndexOf("/");
        if (lastIndexOfSlash != -1) {
            this.suiteName = suitePath.substring(lastIndexOfSlash + 1);
        }
    }

    private void markSuiteStatus() {
        Status status = Status.SUCCESS;
        Session session;
        for (Iterator iterator = tests.iterator(); iterator.hasNext();) {
            TestLauncher testLauncher = (TestLauncher) iterator.next();
            session = Session.getInstance(testLauncher.getChildSessionId());
            if (Status.FAILURE.equals(session.getStatus())) {
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
        executeSuite();
        waitForSuiteCompletion();
        markSuiteStatus();
        generateSuiteReport();
        createIssues();
        cullInactiveTests();
    }

    private void cullInactiveTests() {
        Iterator keys = testsMap.keySet().iterator();
        long now = System.currentTimeMillis();
        long inactivityLimit = Configuration.getMaxInactiveTimeForScript();
        while (keys.hasNext()) {
            String sessionId = (String) keys.next();
            Session session = Session.getInstance(sessionId);
            long lastActiveTime = session.lastActiveTime();
            if (session.getStatus() != Status.SUCCESS && session.getStatus() != Status.FAILURE && now - lastActiveTime > inactivityLimit) {
                String message = "Forcefully terminating script. \nNo response from browser within expected time (" + inactivityLimit / 1000 + " seconds).";
                System.out.println(message);
                if (session.getReport() != null) {
                    session.getReport().addResult(message, "ERROR", "", "");
                }
                new Player().stop(session);
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
        while (currentTestIndex < tests.size()) {
            if (killed) {
                return;
            }
            for (; availableThreads > 0 && currentTestIndex < tests.size(); availableThreads--) {
                int freeThreadNo = getFreeThreadNo();
                if (freeThreadNo != -1) {
                    freeThreads[freeThreadNo] = false;
                    this.executeTest(freeThreadNo);
                }
            }
            try {
                this.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private synchronized int getFreeThreadNo() {
        for (int i = 0; i < freeThreads.length; i++) {
            if (freeThreads[i]) {
                return i;
            }
        }
        return -1;
    }

    private void waitForSuiteCompletion() {
        while (finishedTests < tests.size() && !killed) {
            synchronized (this) {
                cullInactiveTests();
                try {
                    this.wait(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void generateSuiteReport() {
        for (Iterator iterator = listReporter.iterator(); iterator.hasNext();) {
            SahiReporter reporter = (SahiReporter) iterator.next();
            reporter.generateSuiteReport(tests);
        }
    }

    public void setAvailableThreads(final int availableThreads) {
        this.availableThreads = availableThreads;
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

    public void kill() {
        System.out.println("Shutting down ...");
        killed = true;
    }
}
