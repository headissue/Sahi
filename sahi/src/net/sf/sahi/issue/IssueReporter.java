package net.sf.sahi.issue;

import net.sf.sahi.report.SahiReporter;

import java.io.IOException;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * User: dlewis
 * Date: Dec 7, 2006
 * Time: 1:55:54 PM
 */
public class IssueReporter extends SahiReporter {
    private List listIssueCreator = new ArrayList();

    public void addIssueCreator(IssueCreator issueCreator) {
        listIssueCreator.add(issueCreator);
    }

    public IssueReporter(String suiteName) {
        super(new IssueFormatter());
        this.suiteName = suiteName;
    }

    protected void createWriter(String file) throws IOException {
        writer = new StringWriter();
    }

    public boolean createSuiteLogFolder() {
        return false;
    }

    public void reportIssue(List tests) {
        Issue issue = prepareIssue(tests);
        try {
            for (Iterator iterator = listIssueCreator.iterator(); iterator.hasNext();) {
                IssueCreator issueCreator = (IssueCreator) iterator.next();
                issueCreator.createIssue(issue);
            }
        } catch (Exception e) {
        }
    }

    private Issue prepareIssue(List tests) {
        generateSuiteReport(tests);
        StringWriter sw = (StringWriter) writer;
        String summary = suiteName + " failed on " + new SimpleDateFormat("ddMMMyy_HH:mm:ss").format(new Date());
        return new Issue(summary, sw.getBuffer().toString());
    }
}
