/**
 * User: dlewis
 * Date: Dec 6, 2006
 * Time: 3:15:15 PM
 */
package net.sf.sahi.report;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.sf.sahi.config.Configuration;
import net.sf.sahi.rhino.RhinoScriptRunner;
import net.sf.sahi.test.TestLauncher;
import net.sf.sahi.util.Utils;

/**
 * Sahi - Web Automation and Test Tool
 * 
 * Copyright  2006  V Narayan Raman
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


public abstract class SahiReporter {

    protected Formatter formatter;
    protected String logDir;
    protected String suiteName;
    protected Writer suiteWriter;

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

    public void generateSuiteReport(List<TestLauncher> tests) {
        try {
        	createWriter();
            suiteWriter.write(formatter.getHeader());
            suiteWriter.write(formatter.getSummaryHeader());
            writeTestSummary(tests);
            suiteWriter.write(formatter.getSummaryFooter());
            suiteWriter.write(formatter.getFooter());
            suiteWriter.flush();
            suiteWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void writeTestSummary(List<TestLauncher> origtests) throws IOException {
    	List<TestLauncher> tests = new ArrayList<TestLauncher>(origtests);
        for (Iterator<TestLauncher> iter = tests.iterator(); iter.hasNext();) {
            try {
                TestLauncher test = iter.next();
                RhinoScriptRunner scriptRunner = test.getScriptRunner();
                if (scriptRunner == null) {
                    continue;
                }
				Report report = scriptRunner.getReport();
                if (report == null) {
                    continue;
                }
                TestSummary summary = report.getTestSummary();
                if (summary != null) {
                    summary.setAddLink(true);
                    suiteWriter.write(formatter.getSummaryData(summary));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    protected void createWriter() throws IOException {
    	suiteWriter = createWriter(formatter.getSuiteLogFileName());
    }
    
    protected Writer createWriter(String file) throws IOException {
        File dir = new File(getLogDir());
        Configuration.createFolders(dir);
        File logFile = new File(dir, formatter.getFileName(file));
        return new BufferedWriter(new OutputStreamWriter(new FileOutputStream(logFile), "UTF8"));
    }

    public void generateTestReport(Report report) {
        try {
            TestSummary summary = report.getTestSummary();
            summary.setSuiteName(suiteName);
			String logFileName = summary.getLogFileName();
            if (new File(logFileName).exists()) return;
			Writer writer = createWriter(logFileName);
            writer.write(formatter.getHeader());
            writer.write(formatter.getSummaryHeader());
            writer.write(formatter.getSummaryData(summary));
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
                logDir = Configuration.appendLogsRoot(Utils.createLogFileName(suiteName));
            } else {
                logDir = Configuration.getPlayBackLogsRoot();
            }
        }

        return logDir;
    }

    public abstract boolean createSuiteLogFolder();

    public void setLogDir(final String logDir) {
        this.logDir = logDir;
    }

    public void setSuiteName(final String suiteName) {
        this.suiteName = suiteName;
    }
}
