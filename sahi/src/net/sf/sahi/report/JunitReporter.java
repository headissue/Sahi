package net.sf.sahi.report;

import java.util.List;

/**
 * User: dlewis
 * Date: Dec 6, 2006
 * Time: 3:18:18 PM
 */
public class JunitReporter extends SahiReporter {
    public JunitReporter(String logDir) {
        super(logDir, new JUnitFormatter());
    }


    public void generateSuiteReport(List tests) {
    }

    public boolean createSuiteLogFolder() {
        return false;
    }
}
