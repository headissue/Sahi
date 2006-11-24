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
		listResult.add(getFailureResult());
		return listResult;
	}

	public static TestResult getSuccessResult() {
		return new TestResult("_assertNotNull(_textarea(\"t2\"));",
				ResultType.SUCCESS, null);
	}

	public static TestResult getFailureResult() {
		return new TestResult("_call(testAccessors()); Assertion Failed.",
				ResultType.FAILURE, "");
	}

	public static TestResult getInfoResult() {
		return new TestResult("_click(_link(\"Form Test\"));", ResultType.INFO,
				"blah");
	}

	public static TestSummary getTestSummary() {
		TestSummary summary = new TestSummary();
		summary.setScriptName("test");
		summary.setFailures(1);
		summary.setErrors(0);
		summary.setSteps(3);
		return summary;
	}
}
