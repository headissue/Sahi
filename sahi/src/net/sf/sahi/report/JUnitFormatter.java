package net.sf.sahi.report;

import java.util.List;

import net.sf.sahi.util.Utils;

/**
 * @author dlewis
 * 
 */
public class JUnitFormatter implements Formatter {

	public String getFileName(String scriptName) {
		return "TEST-" + scriptName + ".xml";
	}

	public String getFooter() {
		return "</testsuite>";
	}

	public String getHeader() {
		return "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>";
	}

	public String getResultData(List listResult) {
		StringBuffer sb = new StringBuffer();
		if (listResult != null && listResult.size() > 0) {
			for (int i = 0; i < listResult.size(); i++) {
				TestResult result = (TestResult) listResult.get(i);
				sb.append(getStringResult(result)).append("\n");
			}
		}

		return sb.toString();
	}

	public String getStartScript() {
		return "";
	}

	public String getStopScript() {
		return "";
	}

	public String getSummaryData(TestSummary summary) {
		StringBuffer sb = new StringBuffer();
		sb.append("\n<testsuite errors=\"").append(summary.getErrors()).append(
				"\" failures=\"").append(summary.getFailures()).append(
				"\" name=\"").append(
				Utils.escapeQuotes(summary.getScriptName())).append(
				"\" tests=\"").append(summary.getSteps()).append("\">\n");

		return sb.toString();
	}

	public String getStringResult(TestResult result) {
		StringBuffer sb = new StringBuffer();
		sb.append("<testcase name=\"").append(
				Utils.escapeQuotes(result.message)).append("\">");
		if (ResultType.FAILURE.equals(result.type)
				|| ResultType.ERROR.equals(result.type)) {
			sb.append("\n<failure message=\"Assertion Failed\">");
			if (!Utils.isBlankOrNull(result.failureMsg)) {
				sb.append("<![CDATA[").append(result.failureMsg).append("]]>");
			}
			sb.append("</failure>\n");
		}
		sb.append("</testcase>");
		return sb.toString();
	}

}
