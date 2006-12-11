package net.sf.sahi.report;

import net.sf.sahi.config.Configuration;
import net.sf.sahi.session.Session;
import net.sf.sahi.test.TestLauncher;
import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

import java.util.ArrayList;
import java.util.List;
import java.io.IOException;

/**
 * User: dlewis
 * Date: Dec 11, 2006
 * Time: 4:50:00 PM
 */
public class SahiReporterTest extends MockObjectTestCase {
    private SahiReporter reporter;
    private Mock mockFormatter;

    protected void setUp() throws Exception {
        super.setUp();
        mockFormatter = mock(Formatter.class);
        reporter = new SahiReporter("", (Formatter) mockFormatter.proxy()) {
            public boolean createSuiteLogFolder() {
                return false;
            }
        };
    }

    public void testGenerateSuiteReport() {
        mockFormatter.expects(once()).method("getSuiteLogFileName");
        mockFormatter.expects(once()).method("getFileName").will(returnValue("testFile"));

        mockFormatter.expects(once()).method("getHeader").will(returnValue("data"));
        mockFormatter.expects(once()).method("getSummaryHeader").after("getHeader").will(returnValue("data"));
        mockFormatter.expects(once()).method("getSummaryFooter").after("getSummaryHeader").will(returnValue("data"));
        mockFormatter.expects(once()).method("getFooter").after("getSummaryFooter").will(returnValue("data"));

        reporter.generateSuiteReport(new ArrayList());
    }

    public void testGenerateTestReport() {        
        mockFormatter.expects(once()).method("getFileName").will(returnValue("testFile"));

        mockFormatter.expects(once()).method("getHeader").will(returnValue("data"));
        mockFormatter.expects(once()).method("getSummaryHeader").after("getHeader").will(returnValue("data"));
        mockFormatter.expects(once()).method("getSummaryData").after("getSummaryHeader").will(returnValue("data"));
        mockFormatter.expects(once()).method("getSummaryFooter").after("getSummaryData").will(returnValue("data"));
        mockFormatter.expects(once()).method("getStartScript").after("getSummaryFooter").will(returnValue("data"));
        mockFormatter.expects(once()).method("getResultData").after("getStartScript").will(returnValue("data"));
        mockFormatter.expects(once()).method("getStopScript").after("getResultData").will(returnValue("data"));
        mockFormatter.expects(once()).method("getFooter").after("getStopScript").will(returnValue("data"));

        Report report = new Report("",new ArrayList());
        report.setTestSummary(new TestSummary());
        reporter.generateTestReport(report);
    }

    public void testGetLogDirForNullLogDir() {
        assertEquals(Configuration.getPlayBackLogsRoot(), reporter.getLogDir());
    }

    public void testGetLogDirForCustomLogDir() {
        reporter.setLogDir("customDir");
        assertEquals("customDir", reporter.getLogDir());
    }

    public void testGetLogDirForNullLogDirWithCreateSuiteFolderSetToTrue() {
        reporter = new SahiReporter("", (Formatter) mockFormatter.proxy()) {
            public boolean createSuiteLogFolder() {
                return true;
            }
        };
        reporter.setSuiteName("junit");
        assertTrue(reporter.getLogDir().startsWith(Configuration.getPlayBackLogsRoot() + "\\junit__"));
    }

    public void testWriteTestSummary() throws IOException {
        List listTest = new ArrayList();
        TestLauncher test = new TestLauncher("", "");
        test.setSessionId("1234");
        Report report = new Report("", new ArrayList());
        report.setTestSummary(new TestSummary());        
        Session.getInstance(test.getChildSessionId()).setReport(report);
        listTest.add(test);

        mockFormatter.expects(once()).method("getFileName").will(returnValue("testFile"));
        mockFormatter.expects(once()).method("getSummaryData").will(returnValue("data"));
        reporter.createWriter("");
        reporter.writeTestSummary(listTest);
        assertEquals(true,report.getTestSummary().addLink());
    }
}
