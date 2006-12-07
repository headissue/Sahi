package net.sf.sahi.ant;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.BuildFileTest;

/**
 * User: dlewis
 * Date: Dec 6, 2006
 * Time: 11:12:39 AM
 */
public class RunSahiTaskTest extends BuildFileTest {
    public RunSahiTaskTest(String s) {
        super(s);
    }


    protected void setUp() throws Exception {
        configureProject("../antTest.xml");
    }

    public void testSahiWithoutNested() {
        executeTarget("testSahiWithoutNested");
    }

    public void testSahiWithNestedReport() {
        executeTarget("testSahiWithNestedReport");
    }

    public void testReportWithInvalidType() {
        try {
            executeTarget("testReportWithInvalidType");
            fail("Should throw BuildException for invalid type attribute");
        } catch (BuildException e) {
            assertTrue(true);
        }
    }

    public void testSahiWithNestedCreateIssue() {
        executeTarget("testSahiWithNestedCreateIssue");
    }

    public void testCreateIssueWithInvalidTool() {
        try {
            executeTarget("testCreateIssueWithInvalidTool");
            fail("Should throw BuildException for invalid tool attribute");
        } catch (BuildException e) {
            assertTrue(true);
        }
    }

    public void testSahiWithNestedCreateIssueAndReport() {
        executeTarget("testSahiWithNestedCreateIssueAndReport");
    }
}
