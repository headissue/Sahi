package net.sf.sahi.report;

import junit.framework.TestCase;
import net.sf.sahi.config.Configuration;
import net.sf.sahi.util.Utils;

/**
 * @author dlewis
 * 
 */
public class HtmlFormatterTest extends TestCase {
	private HtmlFormatter formatter = null;

	private String expectedSummaryForEmptyList = new StringBuffer(
			"<tr class=\"SUCCESS\"><td><a>test</a></td><td>0</td>").append(
			"<td>0</td><td>0</td><td>100%</td></tr>").toString();

	private String expectedSummaryForAllTypes = new StringBuffer("<tr class=\"FAILURE\"><td><a>test</a></td><td>3</td>").append(
			"<td>1</td><td>0</td><td>66%</td></tr>").toString();

	protected void setUp() throws Exception {
		super.setUp();
		formatter = new HtmlFormatter();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testGetFileName() {
		assertEquals("test.htm", formatter.getFileName("test"));
	}

	public void testGetStringResultForSuccessResult() {
		String expected = "<div class=\"SUCCESS\"><a class=\"SUCCESS\">_assertNotNull(_textarea(\"t2\"));</a></div>";
		assertEquals(expected, formatter.getStringResult(ReportUtil
				.getSuccessResult()));
	}

	public void testGetStringResultForFailureResult() {
		String expected = "<div class=\"FAILURE\"><a class=\"FAILURE\">_call(testAccessors()); Assertion Failed.</a></div>";
		assertEquals(expected, formatter.getStringResult(ReportUtil
				.getFailureResultWithoutDebugInfo()));
	}

	public void testGetStringResultForInfoResult() {
		String expected = "<div class=\"INFO\"><a class=\"INFO\" href=\"/_s_/dyn/Log_highlight?href=blah\">_click(_link(\"Form Test\"));</a></div>";
		assertEquals(expected, formatter.getStringResult(ReportUtil
				.getInfoResult()));
	}

	public void testGetResultDataForEmptyList() {
		assertEquals("", formatter.getResultData(null));
	}

	public void testGetResultDataForListWithAllTypesOfResults() {
		String expected = new StringBuffer(formatter.getStringResult(ReportUtil
				.getInfoResult())).append("\n").append(
				formatter.getStringResult(ReportUtil.getSuccessResult()))
				.append("\n").append(
						formatter.getStringResult(ReportUtil.getFailureResultWithoutDebugInfo()))
				.append("\n").toString();

		assertEquals(expected, formatter.getResultData(ReportUtil
				.getListResult()));
	}

	public void testGetHeader() {
		String expected = new StringBuffer("<style>\n").append(
				new String(Utils.readFile(Configuration
						.getPlaybackLogCSSFileName(true)))).append(
				new String(Utils.readFile(Configuration
						.getConsolidatedLogCSSFileName(true)))).append(
				"</style>\n").toString();
		assertEquals(expected, formatter.getHeader());
	}
	
	public void testGetSummaryHeader()	{
		String expected = "<table><tr><td>Test</td><td>Total Steps</td><td>Failures</td><td>Errors</td><td>Success Rate</td></tr>";
		assertEquals(expected, formatter.getSummaryHeader());		
	}
	
	public void testGetSummaryFooter()	{
		String expected = "</table>";
		assertEquals(expected, formatter.getSummaryFooter());		
	}

	public void testGetSummaryDataForEmptyList() {
		TestSummary summary = new TestSummary();
		summary.setScriptName("test");
		assertEquals(expectedSummaryForEmptyList, formatter
				.getSummaryData(summary));
	}

	public void testGetSummaryDataForAllTypes() {
		assertEquals(expectedSummaryForAllTypes, formatter
				.getSummaryData(ReportUtil.getTestSummary()));
	}
}
