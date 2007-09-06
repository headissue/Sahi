/**
 * Sahi - Web Automation and Test Tool
 *
 * Copyright  2006  V Narayan Raman
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
            suite.addReporter(new HtmlReporter(logDir));
        }
    }
}
