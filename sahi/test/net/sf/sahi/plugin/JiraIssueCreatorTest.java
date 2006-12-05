package net.sf.sahi.plugin;

import junit.framework.TestCase;
import net.sf.sahi.util.FileNotFoundRuntimeException;


/**
 * User: dlewis
 * Date: Dec 4, 2006
 * Time: 5:13:42 PM
 */
public class JiraIssueCreatorTest extends TestCase {
    private JiraIssueCreator issueCreator;


    protected void setUp() throws Exception {
        super.setUp();
        issueCreator = new JiraIssueCreator(null);
    }

    public void testLoadingInvalidPropertiesFile() {
        try {
            issueCreator = new JiraIssueCreator("temp.prop");
            fail("Should throw FileNotFoundRuntimeException");
        } catch (FileNotFoundRuntimeException e) {
            assertTrue(true);
        }
    }

    public void testGetLoginToken() {
        assertNotNull(issueCreator.getLoginToken());
    }

    public void testGetIssueParameters() {
        assertNotNull(issueCreator.getIssueParameters());
    }

    public void testCreateIssue() throws Exception {
        issueCreator.createIssue(new Issue("Sahi Test", "blah"));
    }
}
