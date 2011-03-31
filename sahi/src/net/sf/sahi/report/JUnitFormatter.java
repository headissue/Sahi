/**
 * @author dlewis
 * 
 */
package net.sf.sahi.report;

import java.util.List;

import net.sf.sahi.util.Utils;

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


public class JUnitFormatter implements Formatter {

    public String getFileName(String scriptName) {
        return "TEST-" + scriptName + ".xml";
    }

    public String getFooter() {
        return "</testcase></testsuite>";
    }

    public String getSuiteLogFileName() {
        return "";
    }

    public String getHeader() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>";
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

    public String getStartScript() {
        return "";
    }

    public String getStopScript() {
        return "";
    }

    public String getSummaryData(TestSummary summary) {
        StringBuffer sb = new StringBuffer();
        int testCases = 1; //summary.getFailures() + summary.getErrors() + summary.getSuccesses();
		int failureCount = summary.getFailures() == 0 ? 0 : (summary.getErrors() > 0 ? 0 : 1);
		sb.append("\n<testsuite errors=\"").append(summary.getErrors() > 0 ? 1 : 0).append(
                "\" failures=\"").append(failureCount).append(
                "\" name=\"").append(
                Utils.escapeQuotesForXML(summary.getSuiteName() + "." + summary.getScriptName().replace(".sah", ""))).append(
                "\" tests=\"").append(testCases).append(
                "\" time=\"").append(new Double(summary.getTimeTaken())/1000).append("\">\n")
                .append("<testcase classname=\"").append(
                        Utils.escapeQuotesForXML(summary.getSuiteName() + "." + summary.getScriptName().replace(".sah", ""))).append(
                		"\" name=\"").append(
                        Utils.escapeQuotesForXML(summary.getScriptName())).append(
                "\" time=\"").append(new Double(summary.getTimeTaken())/1000).append("\">");

        return sb.toString();
    }

    public String getStringResult(final TestResult result) {
    	if (!(ResultType.FAILURE.equals(result.type) || ResultType.ERROR.equals(result.type))) 
    		return "";
        StringBuffer sb = new StringBuffer();
//        sb.append("<testcase name=\"").append(
//                Utils.escapeQuotesForXML(result.message)).append("\">");
        if (ResultType.FAILURE.equals(result.type) || ResultType.ERROR.equals(result.type)) {
            String tag = ResultType.FAILURE.equals(result.type) ? "failure" : "error";
			sb.append("\n<" + tag + " message=\"Assertion Failed\">");
            if (!Utils.isBlankOrNull(result.failureMsg)) {
                sb.append("<![CDATA[").append(result.failureMsg).append("]]>");
            }
            sb.append("</" + tag + ">\n");
        }
//        sb.append("</testcase>");
        return sb.toString();
    }

    public String getSummaryFooter() {
        return "";
    }

    public String getSummaryHeader() {
        return "";
    }
}
