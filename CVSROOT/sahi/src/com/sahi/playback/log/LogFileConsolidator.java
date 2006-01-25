package com.sahi.playback.log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.StringTokenizer;
import com.sahi.config.Configuration;
import com.sahi.playback.log.PlayBackLogLevel;
import com.sahi.util.Utils;

public class LogFileConsolidator {
	private final String logFileName;
	private ArrayList testResults = new ArrayList();

	public LogFileConsolidator(String logFileName) {
		this.logFileName = logFileName;
	}

	private void consolidate() {
		String withPath = Configuration.getPlayBackLogsRoot() + logFileName;
		File logFile = new File(withPath);
		if (logFile.exists()) {
			if (logFile.isDirectory()) {
				File[] files = logFile.listFiles();
				for (int i = 0; i < files.length; i++) {
					addTestResult(files[i]);
				}
			} else {
				addTestResult(logFile);
			}
		}
	}

	public String getStatus() {
		consolidate();
		Iterator iterator = testResults.iterator();
		while (iterator.hasNext()) {
			TestResult testResult = (TestResult) iterator.next();
			if (testResult.failures.size() > 0 || testResult.errors.size() > 0) {
				return "FAILURE";
			}
		}
		return "SUCCESS";
	}

	private void addTestResult(File file) {
		String logFileFullPath;
		try {
			logFileFullPath = file.getCanonicalPath();
			if (logFileFullPath.endsWith(".htm")) {
				TestResult testResult = new TestResult(logFileFullPath);
				testResults.add(testResult);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String getHTML() {
		StringBuffer sb = new StringBuffer();
		sb.append("<style>\r\n")
		.append(new String(Utils.readFile(Configuration.getConsolidatedLogCSSFileName(true))))
		.append("</style>\r\n")
		.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"/_s_"+Configuration.getConsolidatedLogCSSFileName(false)+"\">\r\n");
		sb.append("<table>");
		sb.append("<tr><td>Test</td><td>Total Steps</td><td>Failures</td>" + "<td>Errors</td><td>Success Rate</td></tr>\r\n");
		Iterator iterator = testResults.iterator();
		while (iterator.hasNext()) {
			TestResult testResult = (TestResult) iterator.next();
			int failureCount = testResult.failures.size();
			int errorCount = testResult.errors.size();
			boolean isFailed = (failureCount != 0 || errorCount != 0);
			sb.append("<tr class=\"" + (isFailed?"FAILURE":"SUCCESS") + "\" ><td>");
			sb.append(getTestLink(testResult, isFailed));
			sb.append("</td><td>");
			sb.append(testResult.total);
			sb.append("</td><td>");
			sb.append(failureCount);
			sb.append("</td><td>");
			sb.append(errorCount);
			sb.append("</td><td>");
			int success = 0;
			if (testResult.total != 0) {
				success = ((100 - (testResult.errors.size() + testResult.failures.size()) * 100 / testResult.total));
			}
			sb.append(success);
			sb.append(" % </td></tr>\r\n");
		}
		sb.append("</table>");
		return sb.toString();
	}

	private String getTestLink(TestResult testResult, boolean isFailed) {
		return "<a "+ (isFailed?"style='color:white'":"") +" href='" + testResult.logFile + "'>" + getTestFileName(testResult.logFile) + "</a>";
	}

	private String getTestFileName(String logFile) {
		String changed = logFile.replaceAll("\\\\", "/");
		int ixUnderScores = changed.indexOf("__");
		if (ixUnderScores == -1)
			return logFile;
		int ixSlash = changed.lastIndexOf("/", ixUnderScores);
		return logFile.substring(ixSlash + 1, ixUnderScores) + ".sah";
	}

	public static String getLogsList() {
		File[] fileList = new File(Configuration.getPlayBackLogsRoot()).listFiles();
		Comparator comparator = new LastModifiedComparator();
		java.util.Arrays.sort(fileList, comparator);
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < fileList.length; i++) {
			String fileName = fileList[i].getName();
			if (fileName.endsWith(".htm") || fileList[i].isDirectory()) {
				sb.append("<a href='/_s_/logs/");
				sb.append(getSummaryFile(fileList[i]));
				sb.append("'>");
				sb.append(fileName);
				sb.append("</a><br>");
			}
		}
		sb.append("\n\n\n");
		return sb.toString();
	}

	private static Object getSummaryFile(File file) {
		if (file.isDirectory()) {
			return file.getName() + "/index.htm";
		}
		return file.getName();
	}

	public void summarize() throws IOException {
		consolidate();
		String withPath = Configuration.getPlayBackLogsRoot() + logFileName;
		File logFile = new File(withPath);
		File summaryFile = null;
		if (logFile.exists()) {
			if (logFile.isDirectory()) {
				summaryFile = new File(logFile, "index.htm");
			} else {
				summaryFile = new File(stripExtension(withPath) + "_summary.htm");
			}
			summaryFile.createNewFile();
			FileOutputStream out = new FileOutputStream(summaryFile);
			out.write(getHTML().getBytes());
			out.flush();
			out.close();
		}
	}

	private String stripExtension(String withPath) {
		String name = withPath;
		int ix = withPath.lastIndexOf(".");
		if (ix != -1) {
			name = withPath.substring(0, ix);
		}
		return name;
	}

	class TestResult {
		private final String logFile;
		private final String logFileFullPath;
		private int total = 0;
		private ArrayList errors = new ArrayList();
		private ArrayList failures = new ArrayList();
		final PlayBackLogFormatter playBackLogFormatter = new PlayBackLogFormatter();

		TestResult(String logFileFullPath) {
			this.logFileFullPath = logFileFullPath;
			this.logFile = new File(logFileFullPath).getName();
			init();
		}

		private void init() {
			String contents = new String(Utils.readFile(logFileFullPath));
			StringTokenizer tokenizer = new StringTokenizer(contents, "\n");
			while (tokenizer.hasMoreTokens()) {
				String line = tokenizer.nextToken().trim();
				if (line.startsWith(playBackLogFormatter.getErrorIndicator())) {
					errors.add(line);
					total++;
				} else if (line.startsWith(playBackLogFormatter.getFailureIndicator())) {
					failures.add(line);
					total++;
				} else if (line.startsWith(playBackLogFormatter.getSuccessIndicator())) {
					total++;
				}
			}
		}
	}
}

class LastModifiedComparator implements Comparator {
	public int compare(Object file1, Object file2) {
		long diff = (((File) file1).lastModified() - ((File) file2).lastModified());
		return diff == 0 ? 0 : diff < 0 ? 1 : -1;
	}
}
