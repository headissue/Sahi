package net.sf.sahi.report;

/**
 * User: dlewis
 * Date: Dec 6, 2006
 * Time: 3:14:31 PM
 */
public class HtmlReporter extends SahiReporter {
    public HtmlReporter(String logDir) {
        super(logDir, new HtmlFormatter());
    }
}
