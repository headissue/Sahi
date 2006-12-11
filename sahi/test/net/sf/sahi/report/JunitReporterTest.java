package net.sf.sahi.report;

import junit.framework.TestCase;

/**
 * User: dlewis
 * Date: Dec 11, 2006
 * Time: 5:47:23 PM
 */
public class JunitReporterTest extends TestCase {
    public void testCreateSuiteLogFolder()  {
        assertEquals(false,new JunitReporter("").createSuiteLogFolder());   
    }
}
