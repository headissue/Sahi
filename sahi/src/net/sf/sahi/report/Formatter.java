package net.sf.sahi.report;

import java.util.List;

/**
 * @author dlewis
 * 
 */
public interface Formatter {
	String getFileName(String scriptName);

	String getHeader();

	String getResultData(List listResult);

	String getSummaryHeader();

	String getSummaryData(TestSummary summary);

	String getSummaryFooter();

	String getStartScript();

	String getStopScript();

	String getFooter();

    String getSuiteLogFileName();
}
