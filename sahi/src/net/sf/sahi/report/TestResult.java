package net.sf.sahi.report;

/**
 * @author dlewis
 * 
 */
public class TestResult {
	String message = null;

	String debugInfo = null;

	String failureMsg = null;

	ResultType type = null;

	public TestResult(String message, ResultType type, String debugInfo,
			String failureMsg) {
		this.message = message;
		this.type = type;
		this.debugInfo = debugInfo;
		this.failureMsg = failureMsg;
	}
}
