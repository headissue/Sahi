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
package net.sf.sahi.ant;

import net.sf.sahi.test.TestRunner;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class RunSahiTask extends Task {

    protected String suite;
    protected String browser;
    protected String baseURL;
    protected String sahiHost;
    protected String sahiPort;
    protected String failureProperty;
    protected String haltOnFailure;
    protected String stop;
    protected String threads = "1";
    private String browserOption;
    private String browserProcessName;
    private CreateIssue createIssue;
    private List<Report> listReport = new ArrayList<Report>();
	private String browserType;
	private String singleSession;

    public void setBrowserOption(String browserOption) {
        this.browserOption = browserOption;
    }

    public void setBaseURL(String baseURL) {
        this.baseURL = baseURL;
    }

    public void setBrowser(String browser) {
        this.browser = browser;
    }

    public void setSuite(String suite) {
        this.suite = suite;
    }

    public void setSahiPort(String port) {
        this.sahiPort = port;
    }

    public void setSahiHost(String sahiHost) {
        this.sahiHost = sahiHost;
    }
    
    public void setBrowserType(String browserType) {
		this.browserType = browserType;
    }
    
    public String getBrowserType() {
    	return this.browserType;
    }

    public void setSingleSession(String singleSession) {
		this.singleSession = singleSession;
	}

	public String getSingleSession() {
		return singleSession;
	}

	public void execute() throws BuildException {
        if (stop != null) {
            stopServer();
            return;
        }
        startServer();
    }

    private void stopServer() {
        try {
            URL url = new URL("http://" + sahiHost + ":" + sahiPort + "/_s_/dyn/stopserver");
            InputStream s = url.openStream();
            s.close();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
        }
    }

    private void startServer() {
        String status = "FAILURE";
        try {
        	TestRunner testRunner;
        	if (this.browserType != null) {
        		testRunner = new TestRunner(suite, browserType, baseURL, sahiHost, sahiPort, threads, listReport, createIssue);
        	} else {
	            testRunner = new TestRunner(suite, browser, baseURL,
	                    sahiHost, sahiPort, threads, browserOption, browserProcessName, listReport, createIssue);
        	}
        	testRunner.setIsSingleSession("true".equals(singleSession));
            status = testRunner.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("STATUS:" + status);
        if (!"SUCCESS".equals(status)) {
            if (failureProperty != null) {
                getProject().setProperty(failureProperty, "true");
            }
            if ("true".equalsIgnoreCase(haltOnFailure)) {
                throw new BuildException(status);
            }
        }
    }

    public void setFailureProperty(String failureProperty) {
        this.failureProperty = failureProperty;
    }

    public void setHaltOnFailure(String haltOnFailure) {
        this.haltOnFailure = haltOnFailure;
    }

    public void setStop(String stop) {
        this.stop = stop;
    }

    public void setThreads(String threads) {
        this.threads = threads;
    }

//    public Report createReporter() {
//        this.reporter = new Report();
//        return this.reporter;
//    }

//    public CreateIssue createIssueRaiser() {
//        this.createIssue = new CreateIssue();
//        return this.createIssue;
//    }
    public void addConfiguredCreateIssue(CreateIssue createIssue) {
        System.out.println("Setting createIssue");
        if (!"jira".equalsIgnoreCase(createIssue.getTool())) {
            throw new BuildException("tool attribute is mandatory and must be 'jira'");
        }
        this.createIssue = createIssue;
    }

    public void addConfiguredReport(Report report) {
        if (!("junit".equalsIgnoreCase(report.getType()) || "html".equalsIgnoreCase(report.getType()))) {
            throw new BuildException("Valid valued for attribute 'type' of tag 'reporter' are html or junit");
        }
        this.listReport.add(report);
    }

	public void setBrowserProcessName(String browserProcessName) {
		this.browserProcessName = browserProcessName;
	}
}
