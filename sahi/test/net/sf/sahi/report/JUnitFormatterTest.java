package net.sf.sahi.report;

import junit.framework.TestCase;

public class JUnitFormatterTest extends TestCase {
	private JUnitFormatter formatter = null;

	protected void setUp() throws Exception {
		super.setUp();
		formatter = new JUnitFormatter();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testGetFileName() {
		assertEquals("TEST-test.xml", formatter.getFileName("test"));
	}

	public void testGetFooter() {
		String expected = "</testsuite>";
		assertEquals(expected, formatter.getFooter());
	}

	public void testGetHeader() {
		String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>";
		assertEquals(expected, formatter.getHeader());
	}

	public void testGetResultDataForEmptyList() {
		assertEquals("", formatter.getResultData(null));
	}
	
	public void testGetResultDataForListWithAllTypesOfResults() {
		String expected = new StringBuffer(formatter.getStringResult(ReportUtil
				.getInfoResult())).append("\n").append(
				formatter.getStringResult(ReportUtil.getSuccessResult()))
				.append("\n").append(
						formatter.getStringResult(ReportUtil.getFailureResult()))
				.append("\n").toString();

		assertEquals(expected, formatter.getResultData(ReportUtil
				.getListResult()));
	}

	public void testGetSummaryDataForEmptyList() {
		String expected = "\n<testsuite errors=\"0\" failures=\"0\" name=\"test\" tests=\"0\">\n";
		TestSummary summary = new TestSummary();
		summary.setScriptName("test");
		assertEquals(expected, formatter.getSummaryData(summary));
	}

	public void testGetSummaryDataForAllTypes() {
		String expected = "\n<testsuite errors=\"0\" failures=\"1\" name=\"test\" tests=\"3\">\n";
		assertEquals(expected, formatter.getSummaryData(ReportUtil
				.getTestSummary()));
	}

	public void testGetStringResultForSuccessResult() {
		String expected = "<testcase name=\"_assertNotNull(_textarea(&quot;t2&quot;));\"></testcase>";
		assertEquals(expected, formatter.getStringResult(ReportUtil
				.getSuccessResult()));
	}

	public void testGetStringResultForFailureResult() {
		String expected = "<testcase name=\"_call(testAccessors()); Assertion Failed.\">\n<failure message=\"Assertion Failed\"></failure>\n</testcase>";
		assertEquals(expected, formatter.getStringResult(ReportUtil
				.getFailureResult()));
	}
}
