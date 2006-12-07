package net.sf.sahi.report;

import net.sf.sahi.config.Configuration;
import junit.framework.TestCase;

/**
 * User: dlewis
 * Date: Dec 6, 2006
 * Time: 4:46:00 PM
 */
public class SahiReporterTest extends TestCase {
    private SahiReporter reporter;


    protected void setUp() throws Exception {
        super.setUp();
        reporter = new SahiReporter(null, null)  ;
    }

    public void testGetAbsoluteLogFileNameForDefaultLogDir() {
		assertEquals(Configuration.getPlayBackLogsRoot(), reporter.getLogDir());
	}

	public void testGetAbsoluteLogFileNameForCustomLogDir() {
		String logDir = "testDir";
		reporter.setLogDir(logDir);
		assertEquals(logDir, reporter.getLogDir());
	}
}
