package net.sf.sahi.report;

import junit.framework.TestCase;
import net.sf.sahi.config.Configuration;

/**
 * @author dlewis
 * 
 */
public class ReportTest extends TestCase {
	private Report report = null;

	protected void setUp() throws Exception {
		super.setUp();
		report = new Report("test", new HtmlReporter(null));
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testAddResult() {
		report.addResult("a", "success", "", null);
		assertEquals(1, report.getListResult().size());
	}

	/*
	 * public void xtestGenerateReportForDefaultLogDir() throws
	 * FileNotFoundException { report.addResult(ReportUtil.getListResult());
	 * report.generateReport(); assertNotNull(new
	 * FileReader(Configuration.appendLogsRoot(report
	 * .getFormatter().getFileName(Utils.createLogFileName("test"))))); }
	 * 
	 * public void xtestGenerateReportForCustomLogDir() throws
	 * FileNotFoundException { report.addResult(ReportUtil.getListResult());
	 * report.generateReport(); assertNotNull(new
	 * FileReader(Configuration.appendLogsRoot(report
	 * .getFormatter().getFileName(Utils.createLogFileName("test"))))); }
	 */

	public void testSummarizeResultsForEmptyList() {
		TestSummary summary = report.summarizeResults();
		assertEquals(0, summary.getSteps());
	}

	public void testSummarizeResultsForAllTypes() {
		report.addResult(ReportUtil.getListResult());
		TestSummary summary = report.summarizeResults();
		assertEquals(3, summary.getSteps());
		assertEquals(1, summary.getFailures());
		assertEquals(0, summary.getErrors());
		assertEquals("test", summary.getScriptName());
	}

	
}
