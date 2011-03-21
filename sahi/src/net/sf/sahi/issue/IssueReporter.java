/**
 * User: dlewis
 * Date: Dec 7, 2006
 * Time: 1:55:54 PM
 */
package net.sf.sahi.issue;

import net.sf.sahi.report.SahiReporter;
import net.sf.sahi.test.TestLauncher;

import java.io.IOException;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

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


public class IssueReporter extends SahiReporter {

    private List<IssueCreator> listIssueCreator = new ArrayList<IssueCreator>();

    public void addIssueCreator(final IssueCreator issueCreator) {
        listIssueCreator.add(issueCreator);
    }

    public IssueReporter(final String suiteName) {
        super(new IssueFormatter());
        this.suiteName = suiteName;
    }

    protected void createWriter() throws IOException {
    	suiteWriter = new StringWriter();
    }

    public boolean createSuiteLogFolder() {
        return false;
    }

    public void reportIssue(List<TestLauncher> tests) {
        Issue issue = prepareIssue(tests);
        createIssue(issue);
    }

    void createIssue(final Issue issue) {
        try {
            for (Iterator<IssueCreator> iterator = listIssueCreator.iterator(); iterator.hasNext();) {
                IssueCreator issueCreator = (IssueCreator) iterator.next();
                issueCreator.login();
                issueCreator.createIssue(issue);
                issueCreator.logout();
            }
        } catch (Exception e) {
        }
    }

    Issue prepareIssue(final List<TestLauncher> tests) {
        generateSuiteReport(tests);
        StringWriter sw = (StringWriter) suiteWriter;
        String summary = suiteName + " failed on " + new SimpleDateFormat("ddMMMyy_HH:mm:ss").format(new Date());
        return new Issue(summary, sw.getBuffer().toString());
    }
}
