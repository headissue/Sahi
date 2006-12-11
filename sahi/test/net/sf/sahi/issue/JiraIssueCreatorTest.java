package net.sf.sahi.issue;

import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.jmock.Mock;
import org.jmock.cglib.MockObjectTestCase;
import org.jmock.core.Constraint;

import java.util.HashMap;
import java.util.Map;
import java.net.URL;

/**
 * User: dlewis
 * Date: Dec 11, 2006
 * Time: 12:38:08 PM
 */
public class JiraIssueCreatorTest extends MockObjectTestCase {
    private JiraIssueCreator issueCreator;
    Mock mockXmlRpcClient;


    protected void setUp() throws Exception {
        super.setUp();
        if (mockXmlRpcClient == null) {
            mockXmlRpcClient = mock(XmlRpcClient.class);
            issueCreator = new JiraIssueCreator((XmlRpcClient) mockXmlRpcClient.proxy());
        }
        mockXmlRpcClient.expects(once()).method("execute").with(eq("jira1.login"), ANYTHING).will(returnValue("loginToken"));
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        mockXmlRpcClient.reset();
    }

    public void testLogout() throws Exception {
        mockXmlRpcClient.expects(once()).method("execute").with(eq("jira1.logout"), ANYTHING);
        issueCreator.logout();
    }

    public void testInitializeXmlRpcClient() throws Exception {
        Mock mockConfigImpl = mock(XmlRpcClientConfigImpl.class);
        mockXmlRpcClient.reset();
        mockXmlRpcClient.expects(once()).method("setConfig").with(isA(XmlRpcClientConfigImpl.class));
        mockConfigImpl.expects(once()).method("setServerURL").with(isA(URL.class));
        issueCreator.initializeXmlRpcClient((XmlRpcClient)mockXmlRpcClient.proxy(),(XmlRpcClientConfigImpl)mockConfigImpl.proxy());
    }

    public void testCreateIssue() throws Exception {
        issueCreator.setIssueParams(new HashMap());
        mockXmlRpcClient.expects(once()).method("execute").with(eq("jira1.createIssue"), new Constraint() {
            public boolean eval(Object object) {
                Object[] arr = (Object[]) object;
                Map issueParams = (Map) arr[1];

                return arr[0] instanceof String && issueParams.containsKey("summary") && issueParams.containsKey("description");
            }

            public StringBuffer describeTo(StringBuffer stringBuffer) {
                return null;
            }
        });
        issueCreator.createIssue(new Issue("", ""));
    }

    public void testGetIssueParametersWithParameterNotFound() throws Exception {
        try {
            mockXmlRpcClient.expects(once()).method("execute").with(eq("jira1.getProjects"), ANYTHING).will(returnValue(new Object[0]));
            issueCreator.getIssueParameters();
            fail("Should throw RuntimeException");
        } catch (RuntimeException e) {
            assertTrue(true);
        }
    }

    public void testGetIssueParametersWithParameterFound() throws Exception {
        mockXmlRpcClient.expects(once()).method("execute").with(eq("jira1.getProjects"), ANYTHING).will(returnValue(new Object[]{getParamMap("Sahi Integration")}));
        mockXmlRpcClient.expects(once()).method("execute").with(eq("jira1.getIssueTypes"), ANYTHING).will(returnValue(new Object[]{getParamMap("Sahi Bug")}));
        mockXmlRpcClient.expects(once()).method("execute").with(eq("jira1.getPriorities"), ANYTHING).will(returnValue(new Object[]{getParamMap("Major")}));
        mockXmlRpcClient.expects(once()).method("execute").with(eq("jira1.getComponents"), ANYTHING).will(returnValue(new Object[]{getParamMap("Regression")}));
        mockXmlRpcClient.expects(atLeastOnce()).method("execute").with(eq("jira1.getVersions"), ANYTHING).will(returnValue(new Object[]{getParamMap("0.1")}));
        Map issueParams = issueCreator.getIssueParameters();
        assertTrue(issueParams.containsKey("project"));
        assertTrue(issueParams.containsKey("type"));
        assertTrue(issueParams.containsKey("priority"));
        assertTrue(issueParams.containsKey("assignee"));
        assertTrue(issueParams.containsKey("components"));
    }

    private Map getParamMap(String paramValue) {
        Map map = new HashMap();
        map.put("name", paramValue);
        return map;
    }
}
