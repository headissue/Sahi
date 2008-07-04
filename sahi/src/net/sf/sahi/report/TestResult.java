/**
 * @author dlewis
 * 
 */
package net.sf.sahi.report;

public class TestResult {

    String message = null;
    String debugInfo = null;
    String failureMsg = null;
    ResultType type = null;

    public TestResult(final String message, final ResultType type, final String debugInfo,
            final String failureMsg) {
        this.message = message;
        this.type = type;
        this.debugInfo = debugInfo;
        this.failureMsg = failureMsg;
    }
}
