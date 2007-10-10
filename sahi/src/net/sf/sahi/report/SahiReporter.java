package net.sf.sahi.report;

import net.sf.sahi.config.Configuration;
import net.sf.sahi.session.Session;
import net.sf.sahi.test.TestLauncher;
import net.sf.sahi.util.Utils;

import java.io.*;
import java.util.Iterator;
import java.util.List;

/**
 * User: dlewis
 * Date: Dec 6, 2006
 * Time: 3:15:15 PM
 */
public abstract class SahiReporter {
    protected Formatter formatter;
    protected String logDir;
    protected Writer writer;
    protected String suiteName;

    public SahiReporter(String logDir, Formatter formatter) {
        this.logDir = logDir;
        this.formatter = formatter;
    }

    public SahiReporter(Formatter formatter) {
        this.formatter = formatter;
    }

    public Formatter getFormatter() {
        return formatter;
    }

    public void setFormatter(Formatter formatter) {
        this.formatter = formatter;
    }

    public void generateSuiteReport(List tests) {
        try {
            createWriter(formatter.getSuiteLogFileName());
            writer.write(formatter.getHeader());
            writer.write(formatter.getSummaryHeader());
            writeTestSummary(tests);
            writer.write(formatter.getSummaryFooter());
            writer.write(formatter.getFooter());
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void writeTestSummary(List tests) throws IOException {
        for (Iterator iter = tests.iterator(); iter.hasNext();) {
            try{
                TestLauncher test = (TestLauncher) iter.next();
                Session session = Session.getInstance(test.getChildSessionId());
                Report report = session.getReport();
                if (report == null) continue;
                TestSummary summary = report.getTestSummary();
                if (summary != null) {
                    summary.setAddLink(true);
                    writer.write(formatter.getSummaryData(summary));
                }
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    protected void createWriter(String file) throws IOException {
        File dir = new File(getLogDir());
        Configuration.createFolders(dir);
        File logFile = new File(dir, formatter.getFileName(file));
        this.writer = new BufferedWriter(new FileWriter(logFile));
    }

    public void generateTestReport(Report report) {
        try {
            createWriter(report.getTestSummary().getLogFileName());
            writer.write(formatter.getHeader());
            writer.write(formatter.getSummaryHeader());
            writer.write(formatter.getSummaryData(report.getTestSummary()));
            writer.write(formatter.getSummaryFooter());
            writer.write(formatter.getStartScript());
            writer.write(formatter.getResultData(report.getListResult()));
            writer.write(formatter.getStopScript());
            writer.write(formatter.getFooter());
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getLogDir() {
        if (Utils.isBlankOrNull(logDir)) {
            if (createSuiteLogFolder()) {
                logDir = Configuration.appendLogsRoot(Utils
                        .createLogFileName(suiteName));
            } else {
                logDir = Configuration.getPlayBackLogsRoot();
            }
        }

        return logDir;
    }

    public abstract boolean createSuiteLogFolder();

    public void setLogDir(String logDir) {
        this.logDir = logDir;
    }

    public void setSuiteName(String suiteName) {
        this.suiteName = suiteName;
    }
}
