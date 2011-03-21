/**
 * @author dlewis
 */
package net.sf.sahi.report;

import net.sf.sahi.util.Utils;

import java.util.ArrayList;
import java.util.Iterator;
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


public class Report {

    protected List<TestResult> listResult = new ArrayList<TestResult>();
    protected String scriptName;
    protected List<SahiReporter> listReporter;
    protected TestSummary testSummary;
    private long startTime;
	private long endTime;

    public Report(String scriptName, List<SahiReporter> listReporter) {
        this.scriptName = scriptName;
        this.listReporter = listReporter;
    }

    public Report(String scriptName) {
        this.scriptName = scriptName;
        listReporter = new ArrayList<SahiReporter>();
    }
    
    public Report(String scriptName, final SahiReporter reporter) {
        this.scriptName = scriptName;
        addReporter(reporter);
    }

    public String getScriptName() {
        return scriptName;
    }

    public void setScriptName(String scriptName) {
        this.scriptName = scriptName;
    }

    public List<SahiReporter> getListReporter() {
        return listReporter;
    }

    public void setListReporter(List<SahiReporter> listReporter) {
        this.listReporter = listReporter;
    }

    public void addReporter(SahiReporter reporter) {
        if (listReporter == null) {
            listReporter = new ArrayList<SahiReporter>();
        }
        listReporter.add(reporter);
    }

    public List<TestResult> getListResult() {
        return listResult;
    }

    void addResult(List<TestResult> listResult) {
        this.listResult.addAll(listResult);
    }

    public void addResult(final String message, final String type, final String debugInfo,
            final String failureMsg) {
        listResult.add(new TestResult(message, ResultType.getType(type),
                debugInfo, failureMsg));
    }

    public void addResult(final String message, final ResultType type, final String debugInfo,
            final String failureMsg) {
        listResult.add(new TestResult(message, type, debugInfo, failureMsg));
    }

    public TestSummary summarizeResults() {
        TestSummary summary = new TestSummary();
        boolean fail = false;
        summary.setScriptName(scriptName);
        summary.setSteps(listResult.size());
        summary.setLogFileName(Utils.createLogFileName(scriptName));
        if (listResult.size() == 0) fail = true;
        else{
	        for (Iterator<TestResult> iter = listResult.iterator(); iter.hasNext();) {
	            TestResult result = (TestResult) iter.next();
	            if (ResultType.FAILURE.equals(result.type)) {
	                summary.incrementFailures();
	                fail = true;
	            } else if (ResultType.ERROR.equals(result.type)) {
	                summary.incrementErrors();
	                fail = true;
	            } else if (ResultType.SUCCESS.equals(result.type)) {
	                summary.incrementSuccesses();
	            } 
	        }
        }
        summary.setTimeTaken(getTimeTaken());
        summary.setFail(fail);
        return summary;
    }

    public TestSummary getTestSummary() {
        if (testSummary == null) {
            testSummary = summarizeResults();
        }
        return testSummary;
    }

    void setTestSummary(TestSummary testSummary) {
        this.testSummary = testSummary;
    }

    public void generateTestReport() {
        for (Iterator<SahiReporter> iterator = listReporter.iterator(); iterator.hasNext();) {
            SahiReporter reporter = iterator.next();
            reporter.generateTestReport(this);
        }
    }

	public void stopTimer() {
		this.endTime = System.currentTimeMillis();
	}

	public void startTimer() {
		this.startTime = System.currentTimeMillis();
	}
	
	public long getTimeTaken() {
		return this.endTime - this.startTime;
	}	
}
