/**
 * 
 */
package net.sf.sahi.report;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.List;

import net.sf.sahi.config.Configuration;
import net.sf.sahi.report.Formatter;
import net.sf.sahi.report.Report;
import net.sf.sahi.session.Session;
import net.sf.sahi.test.TestLauncher;

/**
 * @author dlewis
 * 
 */
public class SuiteReport {
	private Writer writer = null;

	public void generateReport(String suiteLogDir, List tests) {
		TestLauncher test = (TestLauncher) tests.get(0);
		Session session = Session.getInstance(test.getChildSessionId());
		Formatter formatter = session.getReport().getFormatter();
		try {
			createWriter(suiteLogDir, formatter);
			writer.write(formatter.getHeader());
			writer.write(formatter.getSummaryHeader());
			writeTests(tests);
			writer.write(formatter.getSummaryFooter());
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void writeTests(List tests) throws IOException {
		for (Iterator iter = tests.iterator(); iter.hasNext();) {
			TestLauncher test = (TestLauncher) iter.next();
			Session session = Session.getInstance(test.getChildSessionId());
			Report report = session.getReport();
			writer.write(report.getFormatter().getSummaryData(
					report.getTestSummary()));
		}
	}

	private void createWriter(String suiteLogDir, Formatter formatter)
			throws IOException {
		File dir = new File(suiteLogDir);
		Configuration.createLogFolders(dir);
		File logFile = new File(dir, formatter.getFileName("index"));

		this.writer = new BufferedWriter(new FileWriter(logFile));
	}
}
