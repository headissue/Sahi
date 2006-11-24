package net.sf.sahi.report;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.sf.sahi.config.Configuration;
import net.sf.sahi.util.Utils;

/**
 * @author dlewis
 * 
 */
public class Report {
	protected List listResult = new ArrayList();

	protected Formatter formatter = null;

	protected String scriptName = null;

	protected String logDir = null;

	public Report(String scriptName, String logDir, Formatter formatter) {
		this.scriptName = scriptName;
		this.formatter = formatter;
		this.logDir = logDir;
	}

	public List getListResult() {
		return listResult;
	}

	public void addResult(List listResult) {
		this.listResult.addAll(listResult);
	}

	public void addResult(String message, String type, String debugInfo) {
		listResult.add(new TestResult(message, ResultType.getType(type),
				debugInfo));
	}

	public void generateReport() {
		File dir = new File(getLogDir());
		Configuration.createLogFolders(dir);
		System.out.println("Log Dir: "+dir.getAbsolutePath());

		File logFile = new File(dir, formatter.getFileName(Utils
				.createLogFileName(scriptName)));
		System.out.println("Log File: "+logFile.getAbsolutePath());
		

		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(logFile));
			writer.write(formatter.getHeader());
			writer.write(formatter.getSummaryData(summarizeResults()));
			writer.write(formatter.getStartScript());
			writer.write(formatter.getResultData(listResult));
			writer.write(formatter.getStopScript());
			writer.write(formatter.getFooter());
			writer.flush();
			writer.close();
			System.out.println("Hopefully wrote something");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String getLogDir() {
		StringBuffer sb = new StringBuffer();
		String dir = null;
		if (!Utils.isBlankOrNull(logDir)) {
			dir = logDir;
			// sb.append(logDir).append(System.getProperty("file.separator"))
			// .append(
			// formatter.getFileName(Utils
			// .createLogFileName(scriptName)));
		} else {
			dir = Configuration.getPlayBackLogsRoot();
			// sb.append(Configuration.appendLogsRoot(formatter.getFileName(Utils
			// .createLogFileName(scriptName))));
		}
		return dir;
	}

	public TestSummary summarizeResults() {
		TestSummary summary = new TestSummary();
		summary.setScriptName(scriptName);
		summary.setSteps(listResult.size());
		for (Iterator iter = listResult.iterator(); iter.hasNext();) {
			TestResult result = (TestResult) iter.next();
			if (ResultType.FAILURE.equals(result.type)) {
				summary.setFailures(summary.getFailures() + 1);
			} else if (ResultType.ERROR.equals(result.type)) {
				summary.setErrors(summary.getErrors() + 1);
			}
		}
		return summary;
	}

	public Formatter getFormatter() {
		return formatter;
	}

	public void setFormatter(Formatter formatter) {
		this.formatter = formatter;
	}
}
