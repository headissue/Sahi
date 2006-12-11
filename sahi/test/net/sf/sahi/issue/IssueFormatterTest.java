package net.sf.sahi.issue;

import junit.framework.TestCase;
import net.sf.sahi.report.TestSummary;
import net.sf.sahi.report.ReportUtil;

/**
 * User: dlewis
 * Date: Dec 11, 2006
 * Time: 4:39:57 PM
 */
public class IssueFormatterTest extends TestCase {
    private IssueFormatter formatter = new IssueFormatter();

    public void testGetSummaryDataForSuccessSummary()  {
       assertEquals("",formatter.getSummaryData(new TestSummary()));
    }

    public void testGetSummaryDataForFailureSummary()  {
       String expected = "\n\nScript: test\nFailures: 1\nErrors: 0";
       assertEquals(expected,formatter.getSummaryData(ReportUtil.getTestSummary()));
    }
}
