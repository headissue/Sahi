package net.sf.sahi.issue;

import java.util.ArrayList;
import java.util.List;

import net.sf.sahi.test.TestLauncher;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;


/**
 * User: dlewis
 * Date: Dec 8, 2006
 * Time: 5:57:45 PM
 */
public class IssueReporterTest extends MockObjectTestCase {
    private IssueReporter issueReporter;
    public void testCreateIssue()  {
        Mock mockIssueCreator = mock(IssueCreator.class);
        issueReporter = new IssueReporter("junit.suite");
        issueReporter.addIssueCreator((IssueCreator)mockIssueCreator.proxy());

        mockIssueCreator.expects(once()).method("login").withNoArguments();
        mockIssueCreator.expects(once()).method("createIssue").with(isA(Issue.class)).after("login");
        mockIssueCreator.expects(once()).method("logout").withNoArguments().after("createIssue");
        issueReporter.createIssue(new Issue("",""));
    }

    private List createTestList() {
        List listTest = new ArrayList();
        listTest.add(new TestLauncher("t1.sah","blah"));
        listTest.add(new TestLauncher("t2.sah","blah"));
        return listTest;
    }
}
