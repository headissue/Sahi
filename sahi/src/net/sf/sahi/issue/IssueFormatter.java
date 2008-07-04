/**
 * User: dlewis
 * Date: Dec 7, 2006
 * Time: 1:57:00 PM
 */
package net.sf.sahi.issue;

import net.sf.sahi.report.Formatter;
import net.sf.sahi.report.TestSummary;

import java.util.List;

public class IssueFormatter implements Formatter {

    public String getSummaryData(TestSummary summary) {
        if (summary.hasFailed()) {
            StringBuffer sb = new StringBuffer("\n\n");
            sb.append("Script: ").append(summary.getScriptName());
            sb.append("\nFailures: ").append(summary.getFailures());
            sb.append("\nErrors: ").append(summary.getErrors());
            return sb.toString();
        }
        return "";
    }

    public String getFileName(String scriptName) {
        return "";
    }

    public String getHeader() {
        return "";
    }

    public String getResultData(List listResult) {
        return "";
    }

    public String getSummaryHeader() {
        return "List of Failed Scripts:";
    }

    public String getSummaryFooter() {
        return "";
    }

    public String getStartScript() {
        return "";
    }

    public String getStopScript() {
        return "";
    }

    public String getFooter() {
        return "";
    }

    public String getSuiteLogFileName() {
        return "";
    }
}
