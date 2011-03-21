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


public class TM6Formatter implements Formatter {
	 
    public String getFileName(String scriptName) {
        final int ix = scriptName.indexOf("__");
        String fileName = ix == -1 ? scriptName : scriptName.substring(0, ix);
		return fileName + ".xml";
    }

    public String getFooter() {
        return "</report>";
    }

    public String getSuiteLogFileName() {
        return "";
    }

    public String getHeader() {
        return "<report>";
    }

	public String getResultData(List<TestResult> listResult) {
		long lastTime = -1;
		StringBuffer sb = new StringBuffer();
		if (listResult != null && listResult.size() > 0) {
			for (int i = 0; i < listResult.size(); i++) {
				TestResult result = listResult.get(i);
		        long timeDiff = 100;
		        if (lastTime != -1) {
		        	timeDiff = result.time.getTime() - lastTime;
		        }
		        lastTime = result.time.getTime();
		        sb.append(getStringResult(result, timeDiff)).append("\n");
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
//        StringBuffer sb = new StringBuffer();
//        sb.append("\n<testsuite errors=\"").append(summary.getErrors()).append(
//                "\" failures=\"").append(summary.getFailures()).append(
//                "\" name=\"").append(
//                Utils.escapeQuotesForXML(summary.getScriptName())).append(
//                "\" tests=\"").append(summary.getSteps()).append("\">\n");
//
//        return sb.toString();
    	return "";
    }

    public String getStringResult(final TestResult result, long timeDiff) {
        StringBuffer sb = new StringBuffer();
        String fullMsg = result.message;
        String stepName = result.message;
        final int slashNIx = fullMsg.indexOf("\n");
        String extra = "";
		if (slashNIx != -1) {
			stepName = fullMsg.substring(0, slashNIx);
			if (slashNIx+1 < fullMsg.length()) extra = fullMsg.substring(slashNIx + 1);
		}
		if(ResultType.CUSTOM1.equals(result.type)){
			return "";
		}
        sb.append("<step name=\"").append(Utils.escapeForXML(stepName)).append("\"");
        if (ResultType.FAILURE.equals(result.type)) {
        	sb.append(" warn=\"").append(Utils.escapeForXML(result.failureMsg)).append("\"");
        }
        if (ResultType.ERROR.equals(result.type)) {
        	sb.append(" failure=\"").append(Utils.escapeForXML(extra)).append("\"");
        }
        sb.append(" dur=\"").append(timeDiff).append("\"");
        sb.append(" />");
        return sb.toString();
    }

    public String getSummaryFooter() {
        return "";
    }

    public String getSummaryHeader() {
        return "";
    }
}
