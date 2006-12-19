package net.sf.sahi.report;

import junit.framework.TestCase;
import net.sf.sahi.config.Configuration;

/**
 * User: dlewis
 * Date: Dec 7, 2006
 * Time: 4:31:15 PM
 */
public class HtmlReporterTest extends TestCase {
    private SahiReporter reporter;

    public void testGetSuiteDirForDefaultLogDir()   {
        reporter = new HtmlReporter();
        assertEquals(Configuration.getPlayBackLogsRoot(), reporter.getLogDir());
    }

    public void testGetSuiteDirForCustomLogDir()   {
        reporter = new HtmlReporter("testDir");
        assertEquals("testDir", reporter.getLogDir());
    }

    public void testGetSuiteDirForCreatedSuiteLogDir()   {
        reporter = new HtmlReporter(true);
        reporter.setSuiteName("testSuite");
        assertTrue(reporter.getLogDir().indexOf("testSuite")!=-1);
    }
}
