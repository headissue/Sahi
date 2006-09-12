/**
 * Copyright V Narayan Raman
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

import net.sf.sahi.config.Configuration;
import net.sf.sahi.request.HttpRequest;
import net.sf.sahi.session.Session;
import net.sf.sahi.util.Utils;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

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
    private String suiteLogDir;

    private static HashMap suites = new HashMap();


    private SahiTestSuite(String suitePath, String base, String browser, String logDir,
                          String sessionId) {
        this.suitePath = suitePath;
        this.base = base;
        this.browser = browser;
        this.suiteLogDir = logDir;
        this.sessionId = stripSah(sessionId);
        setSuiteName(suitePath);
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
        File suite = new File(suitePath);
        if (suite.isDirectory()) {
            processSuiteDir(suite);

        } else {
            processSuiteFile();
        }
        if (suiteLogDir != null) {
            deleteLogDir(suiteLogDir);
        }

    }

    private void processSuiteDir(File suite) {
        File[] fileNames = suite.listFiles();
        Arrays.sort(fileNames, new Comparator() {
            public int compare(Object o1, Object o2) {
                return ((File) o1).getName().compareToIgnoreCase(((File) o1).getName());
            }
        });
        for (int i = 0; i < fileNames.length; i++) {
            File file = fileNames[i];
            if (!file.isDirectory()) {
                String testName = file.getAbsolutePath();
                if (testName.endsWith(".sah") || testName.endsWith(".sahi")) {
                    addTest(testName, base);
                }
            }
        }
        for (int i = 0; i < fileNames.length; i++) {
            File file = fileNames[i];
            if (file.isDirectory()) {
                processSuiteDir(file);
            }
        }
    }

    private void processSuiteFile() {
        String contents = new String(Utils.readFile(suitePath));
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
        String testName;
        String startURL;
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
        addTest(Utils.concatPaths(suitePath, testName), startURL);
    }

    private void addTest(String testName, String startURL) {
        TestLauncher sahiTest = new TestLauncher(testName, startURL, browser, sessionId);
        tests.add(sahiTest);
        testsMap.put(new File(testName).getName(), sahiTest);
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

    public synchronized boolean isRunning() {
        return (finishedTests < tests.size());
    }

    public synchronized void stop(String scriptName) {
        ((TestLauncher) (testsMap.get(scriptName))).stop();
        finishedTests++;
    }

    public String getSuiteName() {
        return suiteName;
    }

    public String getSuitePath() {
        return suitePath;
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

    private void deleteLogDir(String logDir) {
        try {
            if (suiteLogDir == null || suiteLogDir.equals("")) {
                return;
            }
            File logDirFile = new File(Utils.concatPaths(Configuration.getPlayBackLogsRoot(), suiteLogDir));
            if (logDirFile.exists()) {
                File[] files = logDirFile.listFiles();
                for (int i = 0; i < files.length; i++) {
                    files[i].delete();
                }
                logDirFile.delete();
            }
        } catch (Exception e) {
        }
    }

    public static void startSuite(HttpRequest request) {
        Session session = request.session();
        String suitePath = request.getParameter("suite");
        String browser = request.getParameter("browser");
        String base = request.getParameter("base");
        String threadsStr = request.getParameter("threads");
        String logDir = request.getParameter("logDir");
        int threads = 1;
        try {
            threads = Integer.parseInt(threadsStr);
        } catch (Exception e) {
        }
        logDir = "".equals(logDir) ? null : logDir;
        SahiTestSuite suite = new SahiTestSuite(suitePath, base, browser, logDir, session.id());
        for (int i = 0; i < threads; i++) {
            suite.executeNext();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
