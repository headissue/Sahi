package net.sf.sahi.report;

/**
 * @author dlewis
 * 
 */
public class TestResult {
	String message = null;

	String debugInfo = null;

	ResultType type = null;

	public TestResult(String message, ResultType type, String debugInfo) {
		this.message = message;
		this.type = type;
		this.debugInfo = debugInfo;
	}

	public String toString() {
		return "Msg:" + message + " | " + "Type:" + type + " | " + "DebugInfo:"
				+ debugInfo;
	}
}
