package net.sf.sahi.report;

import net.sf.sahi.util.Utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author dlewis
 */
public class Report {
    protected List listResult = new ArrayList();
    protected String scriptName;
    protected List listReporter;
    protected TestSummary testSummary;

    public Report(String scriptName, List listReporter) {
        this.scriptName = scriptName;
        this.listReporter = listReporter;
    }

    public Report(String scriptName, SahiReporter reporter) {
        this.scriptName = scriptName;
        addReporter(reporter);
    }

    public String getScriptName() {
        return scriptName;
    }

    public List getListReporter() {
        return listReporter;
    }

    public void setListReporter(List listReporter) {
        this.listReporter = listReporter;
    }

    public void addReporter(SahiReporter reporter) {
        if (listReporter == null) {
            listReporter = new ArrayList();
        }
        listReporter.add(reporter);
    }

    public List getListResult() {
        return listResult;
    }

    void addResult(List listResult) {
        this.listResult.addAll(listResult);
    }

    public void addResult(String message, String type, String debugInfo,
                          String failureMsg) {
        listResult.add(new TestResult(message, ResultType.getType(type),
                debugInfo, failureMsg));
    }

    public TestSummary summarizeResults() {
        TestSummary summary = new TestSummary();
        boolean fail = false;
        summary.setScriptName(scriptName);
        summary.setSteps(listResult.size());
        summary.setLogFileName(Utils.createLogFileName(scriptName));
        for (Iterator iter = listResult.iterator(); iter.hasNext();) {
            TestResult result = (TestResult) iter.next();
            if (ResultType.FAILURE.equals(result.type)) {
                summary.incrementFailures();
                fail = true;
            } else if (ResultType.ERROR.equals(result.type)) {
                summary.incrementErrors();
                fail = true;
            }
        }
        summary.setFail(fail);
        return summary;
    }

    public TestSummary getTestSummary() {
        if (testSummary == null) {
            testSummary = summarizeResults();
        }
        return testSummary;
    }

    public void generateTestReport() {
        for (Iterator iterator = listReporter.iterator(); iterator.hasNext();) {
            SahiReporter reporter = (SahiReporter) iterator.next();
            reporter.generateTestReport(this);
        }
    }
}
