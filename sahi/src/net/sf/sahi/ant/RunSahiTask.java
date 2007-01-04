/**
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
    private String suite;

    private String browser;

    private String baseURL;

    private String sahiHost;

    private String sahiPort;

    private String failureProperty;

    private String haltOnFailure;

    private String stop;

    private String threads = "1";

    private String browserOption;

    private CreateIssue createIssue;

    private List listReport = new ArrayList();

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
        System.out.println("Setting port");
        this.sahiPort = port;
    }

    public void setSahiHost(String sahiHost) {
        this.sahiHost = sahiHost;
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
            URL url = new URL("http://" + sahiHost + ":" + sahiPort
                    + "/_s_/dyn/stopserver");
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
            TestRunner testRunner = new TestRunner(suite, browser, baseURL,
                    sahiHost, sahiPort, threads, browserOption, listReport, createIssue);
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
        System.out.println("Setting report");
        if (!("junit".equalsIgnoreCase(report.getType()) || "html".equalsIgnoreCase(report.getType()))) {
            throw new BuildException("Valid valued for attribute 'type' of tag 'reporter' are html or junit");
        }        
        this.listReport.add(report);
    }
}
