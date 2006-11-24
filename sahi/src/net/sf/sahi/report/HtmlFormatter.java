package net.sf.sahi.report;

import java.util.List;

import net.sf.sahi.config.Configuration;
import net.sf.sahi.util.Utils;

/**
 * @author dlewis
 * 
 */
public class HtmlFormatter implements Formatter {

	public String getFileName(String scriptName) {
		return scriptName + ".htm";
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

	public String getHeader() {
		return new StringBuffer("<style>\n").append(
				new String(Utils.readFile(Configuration
						.getPlaybackLogCSSFileName(true)))).append(
				new String(Utils.readFile(Configuration
						.getConsolidatedLogCSSFileName(true)))).append(
				"</style>\n").toString();
	}

	public String getStringResult(TestResult result) {
		StringBuffer sb = new StringBuffer();
		sb.append("<div class=\"").append(result.type.getName()).append(
				"\"><a class=\"").append(result.type.getName());
		if (!Utils.isBlankOrNull(result.debugInfo)) {
			sb.append("\" href=\"/_s_/dyn/Log_highlight?href=").append(
					result.debugInfo);
		}
		sb.append("\">").append(result.message).append("</a></div>");

		return sb.toString();
	}

	public String getSummaryData(TestSummary summary) {
		StringBuffer sb = new StringBuffer();
		boolean fail = summary.getErrors() > 0 || summary.getFailures() > 0;
		int successRate = summary.getSteps() != 0 ? ((summary.getSteps() - (summary
				.getFailures() + summary.getErrors())) * 100)
				/ summary.getSteps()
				: 100;

		sb.append("<table><tr><td>Test</td><td>Total Steps</td>");
		sb.append("<td>Failures</td><td>Errors</td><td>Success Rate</td></tr>");
		sb.append("<tr class=\"");
		sb.append(fail ? ResultType.FAILURE.getName() : ResultType.SUCCESS
				.getName());
		sb.append("\"><td><a>").append(summary.getScriptName()).append(
				"</a></td><td>").append(summary.getSteps()).append("</td><td>")
				.append(summary.getFailures()).append("</td><td>").append(
						summary.getErrors()).append("</td><td>").append(
						successRate).append("%</td></tr></table>");
		return sb.toString();
	}

	public String getFooter() {
		return "";
	}

	public String getStartScript() {
		return "\n<br><div class=\"START\"><a class=\"START\">Starting script</a></div>";
	}

	public String getStopScript() {
		return "<div class=\"STOP\"><a class=\"STOP\">Stopping script</a></div>";
	}
}
