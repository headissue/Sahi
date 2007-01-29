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

package net.sf.sahi.command;

import net.sf.sahi.issue.JiraIssueCreator;
import net.sf.sahi.report.HtmlReporter;
import net.sf.sahi.report.JunitReporter;
import net.sf.sahi.request.HttpRequest;
import net.sf.sahi.response.HttpResponse;
import net.sf.sahi.response.NoCacheHttpResponse;
import net.sf.sahi.session.Session;
import net.sf.sahi.session.Status;
import net.sf.sahi.test.SahiTestSuite;

public class Suite {

    public void start(HttpRequest request) {
        Session session = request.session();
        String suitePath = request.getParameter("suite");
        String base = request.getParameter("base");
        String browser = request.getParameter("browser");
        String browserOption = request.getParameter("browserOption");

        SahiTestSuite suite = new SahiTestSuite(suitePath, base, browser,
                session.id(), browserOption);
        int threads = 1;
        try {
            threads = Integer.parseInt(request.getParameter("threads"));
        } catch (Exception e) {
        }
        suite.setAvailableThreads(threads);
        setReporters(suite, request);
        setIssueCreators(suite, request);
        suite.run();
    }

    private void setIssueCreators(SahiTestSuite suite, HttpRequest request) {
        String propFile = request.getParameter("jira");
        if (propFile != null) {
            suite.addIssueCreator(new JiraIssueCreator(propFile));
        }
    }

    public HttpResponse status(HttpRequest request) {
        Session session = request.session();
        SahiTestSuite suite = SahiTestSuite.getSuite(session.id());
        Status status = Status.FAILURE;
        if (suite != null) {
            status = session.getStatus();
        }
        return new NoCacheHttpResponse(status.getName());
    }

    private void setReporters(SahiTestSuite suite, HttpRequest request) {
        String logDir = request.getParameter("junit");
        if (logDir != null) {
            suite.addReporter(new JunitReporter(logDir));
        }
        logDir = request.getParameter("html");
        if (logDir != null) {
            suite.addReporter(new HtmlReporter(true));
        }
    }
}
