package net.sf.sahi.report;

/**
 * User: dlewis
 * Date: Dec 6, 2006
 * Time: 3:14:31 PM
 */
public class HtmlReporter extends SahiReporter {
    protected boolean createSuiteLogFolder = false;

    public HtmlReporter() {
        super(new HtmlFormatter());
    }

    public HtmlReporter(boolean createSuiteLogFolder) {
        this();
        this.createSuiteLogFolder = createSuiteLogFolder;
    }

    public HtmlReporter(String logDir) {
        super(logDir, new HtmlFormatter());
    }

    public boolean createSuiteLogFolder() {
        return createSuiteLogFolder;
    }
}
