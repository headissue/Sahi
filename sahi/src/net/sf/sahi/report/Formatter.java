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

	String getSummaryData(TestSummary summary);

	String getStartScript();

	String getStopScript();

	String getFooter();
}
