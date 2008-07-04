/**
 * User: dlewis
 * Date: Dec 6, 2006
 * Time: 3:14:31 PM
 */
package net.sf.sahi.report;

public class HtmlReporter extends SahiReporter {

    protected boolean createSuiteLogFolder = false;

    public HtmlReporter() {
        super(new HtmlFormatter());
    }

    public HtmlReporter(boolean createSuiteLogFolder) {
        this();
        this.createSuiteLogFolder = createSuiteLogFolder;
    }

    public HtmlReporter(final String logDir) {
        super(logDir, new HtmlFormatter());
        createSuiteLogFolder = true;
    }

    public boolean createSuiteLogFolder() {
        return createSuiteLogFolder;
    }
}
