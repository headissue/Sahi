package net.sf.sahi.report;

import java.util.ArrayList;
import java.util.List;

/**
 * @author dlewis
 * 
 */
public class ReportUtil {
	public static List getListResult() {
		List listResult = new ArrayList();
		listResult.add(getInfoResult());
		listResult.add(getSuccessResult());
		listResult.add(getFailureResultWithoutDebugInfo());
		return listResult;
	}

	public static TestResult getSuccessResult() {
		return new TestResult("_assertNotNull(_textarea(\"t2\"));",
				ResultType.SUCCESS, null, null);
	}

	public static TestResult getFailureResultWithoutDebugInfo() {
		return new TestResult("_call(testAccessors());",
				ResultType.FAILURE, "", "Assertion Failed.");
	}

	public static TestResult getFailureResultWithDebugInfo() {
		return new TestResult(
				"_call(testAccessors());",
				ResultType.FAILURE, null, "Assertion Failed. Expected:[2] Actual:[1]");
	}

	public static TestResult getInfoResult() {
		return new TestResult("_click(_link(\"Form Test\"));", ResultType.INFO,
				"blah", null);
	}

	public static TestSummary getTestSummary() {
		TestSummary summary = new TestSummary();
		summary.setScriptName("test");
		summary.setFailures(1);
		summary.setErrors(0);
		summary.setSteps(3);
        summary.setFail(true);
        return summary;
	}

}
