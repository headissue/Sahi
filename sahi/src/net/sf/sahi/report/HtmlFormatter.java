/**
 * @author dlewis
 */
package net.sf.sahi.report;

import net.sf.sahi.command.Command;
import net.sf.sahi.config.Configuration;
import net.sf.sahi.util.Utils;

import java.text.DateFormat;
import java.util.List;

/**
 * Sahi - Web Automation and Test Tool
 * 
 * Copyright  2006  V Narayan Raman
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


public class HtmlFormatter implements Formatter {

    public String getFileName(String scriptName) {
        return scriptName + ".htm";
    }

    public String getResultData(List<TestResult> listResult) {
        StringBuffer sb = new StringBuffer();
        if (listResult != null && listResult.size() > 0) {
            for (int i = 0; i < listResult.size(); i++) {
                TestResult result = listResult.get(i);
                sb.append(getStringResult(result)).append("\n");
            }
        }

        return sb.toString();
    }

    public String getHeader() {
        return new StringBuffer("<head><meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\" />\n<style>\n").append(
                new String(Utils.readFile(Configuration.getPlaybackLogCSSFileName(true)))).append(
                new String(Utils.readFile(Configuration.getConsolidatedLogCSSFileName(true)))).append(
                "</style></head>\n").toString();
    }

    public String getStringResult(final TestResult result) {
    	if (result.type.equals(ResultType.RAW)) {
    		return result.message;
    	}
        StringBuffer sb = new StringBuffer();
        sb.append("<div class=\"").append(result.type.getName()).append(
                "\"><a class=\"").append(result.type.getName());
        String title = result.debugInfo;
        if (title == null) title = "";
		if (!Utils.isBlankOrNull(title)) {
            sb.append("\" href=\"/_s_/dyn/").append(Command.LOG_HIGHLIGHT).append("?href=").append(title).append("#selected");
        }
        sb.append("\" title=\"" + title + "\">").append(result.message.trim().replaceAll("\n", "<br/>"))
	        .append(result.failureMsg != null ? "<br/>" + result.failureMsg.trim().replaceAll("\n", "<br/>") : "")
	        .append("</a>" +
        		" <span class='extra'> at " + DateFormat.getDateTimeInstance().format(result.time) + "</span>" +
        		"</div>");

        return sb.toString();
    }

    public String getSummaryHeader() {
        return "<table><tr><td>Test</td><td>Total Steps</td><td>Failures</td><td>Errors</td><td>Success Rate</td><td>Time Taken (ms)</td></tr>";
    }

    public String getSummaryData(TestSummary summary) {
        StringBuffer sb = new StringBuffer();
        int successRate = summary.getSteps() != 0 ? ((summary.getSteps() - (summary.getFailures() + summary.getErrors())) * 100) / summary.getSteps()
                : 100;
        sb.append("<tr class=\"");
        sb.append(summary.hasFailed() ? ResultType.FAILURE.getName() : ResultType.SUCCESS.getName());
        sb.append("\"><td>");
        if (summary.addLink()) {
            sb.append("<a class=\"SCRIPT\" href=\"").append(getFileName(summary.getLogFileName())).append("\">").append(summary.getScriptName()).append(
                    "</a>");
        } else {
            sb.append(summary.getScriptName());
        }
        sb.append("</td><td>").append(summary.getSteps())
        	.append("</td><td>").append(summary.getFailures())
        	.append("</td><td>").append(summary.getErrors())
        	.append("</td><td>").append(successRate)
        	.append("%</td><td>").append(summary.getTimeTaken())
        	.append("</td></tr>");
        return sb.toString();
    }

    public String getSummaryFooter() {
        return "</table>";
    }

    public String getFooter() {
        return "";
    }

    public String getSuiteLogFileName() {
        return "index";
    }

    public String getStartScript() {
        return "\n<br/><div class=\"START\"><a class=\"START\">Starting script</a></div>";
    }

    public String getStopScript() {
        return "<div class=\"STOP\"><a class=\"STOP\">Stopping script</a></div>";
    }
}
