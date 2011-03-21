/**
 * User: dlewis
 * Date: Dec 7, 2006
 * Time: 1:57:00 PM
 */
package net.sf.sahi.issue;

import net.sf.sahi.report.Formatter;
import net.sf.sahi.report.TestResult;
import net.sf.sahi.report.TestSummary;

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

    public String getResultData(List<TestResult> listResult) {
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
