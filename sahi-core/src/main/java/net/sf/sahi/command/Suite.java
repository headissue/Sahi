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
package net.sf.sahi.command;

import java.io.IOException;

import net.sf.sahi.config.Configuration;
import net.sf.sahi.issue.JiraIssueCreator;
import net.sf.sahi.report.HtmlReporter;
import net.sf.sahi.report.JunitReporter;
import net.sf.sahi.report.TM6Reporter;
import net.sf.sahi.request.HttpRequest;
import net.sf.sahi.response.HttpResponse;
import net.sf.sahi.response.NoCacheHttpResponse;
import net.sf.sahi.response.SimpleHttpResponse;
import net.sf.sahi.session.Session;
import net.sf.sahi.session.Status;
import net.sf.sahi.test.SahiTestSuite;
import net.sf.sahi.util.BrowserType;
import net.sf.sahi.util.BrowserTypesLoader;

public class Suite {	
	public void startSingleSession(final HttpRequest request) throws Exception {
		final SahiTestSuite suite = getSuite(request);
		suite.launchBrowserForSingleSession();
	}
	
	public SimpleHttpResponse executeTestInSingleSession(final HttpRequest request) throws Exception {
		Session session = request.session();
		final SahiTestSuite suite = session.getSuite();
		String initJS = request.getParameter("initJS");
		System.out.println("Setting initJS" + initJS);
		suite.setInitJS(initJS);
		Status status = suite.executeTestForSingleSession(request.getParameter("testName"), request.getParameter("startURL"));
		return new SimpleHttpResponse(status.getName());
	}
	
	public void stopSingleSession(final HttpRequest request) {
		Session session = request.session();
		final SahiTestSuite suite = session.getSuite();
		suite.killBrowserForSingleSession(true);
	}
	
    public void start(final HttpRequest request) {
    	final SahiTestSuite suite = getSuite(request);
    	suite.loadScripts();
		runSuite(suite);
    }
    
    public void startPreconfiguredBrowser(final HttpRequest request){
    	final SahiTestSuite suite = getPreconfiguredBrowserSuite(request);
    	suite.loadScripts();
    	runSuite(suite);
    }

	private SahiTestSuite getSuite(final HttpRequest request) {
		if (request.getParameter("browserType") != null) {
			return getPreconfiguredBrowserSuite(request);
    	}
        Session session = request.session();
        String suitePath = request.getParameter("suite");
        String base = request.getParameter("base");
        String browser = request.getParameter("browser");
        String browserOption = request.getParameter("browserOption");
        String browserProcessName = request.getParameter("browserProcessName");
        String threads = request.getParameter("threads");
        boolean isSingleSession = "true".equals(request.getParameter("useSingleSession"));
        boolean useSystemProxy = "true".equals(request.getParameter("useSystemProxy"));
        final SahiTestSuite suite = prepareSuite(suitePath, base, browser, session.id(), browserOption, 
				browserProcessName, threads, useSystemProxy, isSingleSession, request);
		return suite;
	}
    
	private SahiTestSuite getPreconfiguredBrowserSuite(final HttpRequest request) {
		SahiTestSuite suite = null;
		BrowserTypesLoader browserLoader = new BrowserTypesLoader();
    	BrowserType browserType = browserLoader.getBrowserType(request);
    	Session session = request.session();
        String suitePath = request.getParameter("suite");
        String base = request.getParameter("base");
        boolean isSingleSession = "true".equals(request.getParameter("useSingleSession"));
        final int threads = getThreads(request.getParameter("threads"), browserType.capacity());

        // launches browser with pre configured browser settings
        if(browserType != null){
	        suite = prepareSuite(suitePath, base, browserType.path(), session.id(), browserType.options(), 
					browserType.processName(), (""+threads), browserType.useSystemProxy(), isSingleSession, request);
        }
        return suite;
	}

	private int getThreads(String threadsStr, int capacity) {
		int threads = 1;
        try {
        	threads = Integer.parseInt(threadsStr);
        } catch (Exception e) {
        }
        return (threads < capacity) ? threads : capacity;
	}
    
    private void runSuite(final SahiTestSuite suite) {
		new Thread(){
        	@Override
        	public void run() {
        		suite.run();
        	}
        }.start();
	}

	private SahiTestSuite prepareSuite(String suitePath, String base, String browser, String sessionId,
			String browserOption, String browserProcessName, String threadCapacity, boolean useSystemProxy,
			boolean isSingleSession, HttpRequest request) {
		final SahiTestSuite suite = new SahiTestSuite(net.sf.sahi.config.Configuration.getAbsoluteUserPath(suitePath),
				base, browser, sessionId, browserOption, browserProcessName, isSingleSession);
		int threads = 1;
		try {
			threads = Integer.parseInt(threadCapacity);
		} catch (Exception e) {}
		suite.setAvailableThreads(threads);
		suite.setUseSystemProxy(useSystemProxy);
		try {
			net.sf.sahi.config.Configuration.copyProfiles();
		} catch (IOException e) {
			e.printStackTrace();
		}
		suite.setExtraInfo(request.getParameter("extraInfo"));
		suite.setInitJS(request.getParameter("initJS"));
		setReporters(suite, request);
		setIssueCreators(suite, request);
		return suite;
	}

    private void setIssueCreators(final SahiTestSuite suite, final HttpRequest request) {
        String propFile = request.getParameter("jira");
        if (propFile != null) {
            suite.addIssueCreator(new JiraIssueCreator(propFile));
        }
    }

    public HttpResponse status(final HttpRequest request) {
        Session session = request.session();
        Status status = session.getStatus();
        if (status == null) status = Status.FAILURE;
        return new NoCacheHttpResponse(status.getName());
    }

    private void setReporters(final SahiTestSuite suite, final HttpRequest request) {
		final String defaultLogDir = Configuration.appendLogsRoot(suite.getLogFolderName());
        String logDir = request.getParameter("junit");
        if (logDir != null) {
        	logDir = getLogDir(defaultLogDir, logDir);
        	suite.setJunitLogDir(logDir);
            suite.addReporter(new JunitReporter(logDir));
        }
        logDir = request.getParameter("html");
        if (logDir != null) {
        	logDir = getLogDir(defaultLogDir, logDir);
        	suite.setHtmlLogDir(logDir);
            suite.addReporter(new HtmlReporter(logDir));
        }
        logDir = request.getParameter("tm6");
        if (logDir != null) {
        	logDir = getLogDir(defaultLogDir, logDir);
        	suite.setTM6LogDir(logDir);
            suite.addReporter(new TM6Reporter(logDir));
        }
    }

	private String getLogDir(final String defaultLogDir, String logDir) {
		return logDir.equals("") ? defaultLogDir : net.sf.sahi.config.Configuration.getAbsoluteUserPath(logDir);
	}

    public void kill(final HttpRequest request) {
        Session session = request.session();
        SahiTestSuite suite = SahiTestSuite.getSuite(session.id());
        if (suite != null) suite.kill();
    }
}
