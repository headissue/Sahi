/**
 * User: dlewis
 * Date: Dec 4, 2006
 * Time: 5:11:59 PM
 */
package net.sf.sahi.issue;

import net.sf.sahi.config.Configuration;
import net.sf.sahi.util.FileNotFoundRuntimeException;
import net.sf.sahi.util.Utils;
import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.*;

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

@SuppressWarnings("unchecked")
public class JiraIssueCreator implements IssueCreator {

    private static Properties properties;
    private XmlRpcClient rpcClient;
    private String loginToken;
    private Map issueParams;

    public JiraIssueCreator(final String propFile) {
        loadPropFile(propFile);
        initializeXmlRpcClient(new XmlRpcClient(), new XmlRpcClientConfigImpl());
    }

    void initializeXmlRpcClient(XmlRpcClient rpcClient, XmlRpcClientConfigImpl configImpl) {
        try {
            URL jiraRpcUrl = new URL(properties.getProperty("jira.url") + properties.getProperty("jira.rpc.path"));
            configImpl.setServerURL(jiraRpcUrl);
            this.rpcClient = rpcClient;
            rpcClient.setConfig(configImpl);
        } catch (Exception e) {
            throw new RuntimeException("Error connecting to CreateIssue", e);
        }
    }

    JiraIssueCreator(XmlRpcClient rpcClient) {
        this.rpcClient = rpcClient;
        loadPropFile(null);
    }

    void setIssueParams(Map issueParams) {
        this.issueParams = issueParams;
    }

    private void loadPropFile(String propFile) {
        if (Utils.isBlankOrNull(propFile)) {
            propFile = Configuration.getJiraPropertyPath();
        }
        properties = new Properties();
        try {
            properties.load(new FileInputStream(propFile));
        } catch (IOException e) {
            throw new FileNotFoundRuntimeException(e);
        }
    }

    public static void main(String[] args) {
        JiraIssueCreator issueCreator = new JiraIssueCreator("");
        try {
            issueCreator.createIssue(new Issue("Sahi Test", "blah"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void createIssue(Issue issue) throws Exception {
        if (issueParams == null) {
            issueParams = getIssueParameters();
        }
        issueParams.put("summary", issue.getSummary());
        issueParams.put("description", issue.getDescription());

        rpcClient.execute("jira1.createIssue", new Object[]{getLoginToken(), issueParams});
    }

    Map getIssueParameters() {
        issueParams = new HashMap();
        try {
            String projectKey = new JiraItem("jira1.getProjects", null, properties.getProperty("jira.project")).get("key");
            issueParams.put("project", projectKey);
            issueParams.put("type", new JiraItem("jira1.getIssueTypes", null, properties.getProperty("jira.issueType")).get("id"));
            issueParams.put("priority", new JiraItem("jira1.getPriorities", null, properties.getProperty("jira.priority")).get("id"));
            issueParams.put("assignee", properties.getProperty("jira.assignee"));
            List param = new ArrayList();
            param.add(projectKey);
            issueParams.put("components", new JiraItem("jira1.getComponents", param, properties.getProperty("jira.component")).getMapInList("id"));
            if (!Utils.isBlankOrNull(properties.getProperty("jira.affectsVersions"))) {
                issueParams.put("affectsVersions", new JiraItem("jira1.getVersions", param, properties.getProperty("jira.affectsVersions")).getMapInList("id"));
            }
            if (!Utils.isBlankOrNull(properties.getProperty("jira.fixVersions"))) {
                issueParams.put("fixVersions", new JiraItem("jira1.getVersions", param, properties.getProperty("jira.fixVersions")).getMapInList("id"));
            }
        } catch (XmlRpcException e) {
            throw new RuntimeException(e);
        }
        return issueParams;
    }

    public void logout() throws XmlRpcException {
        rpcClient.execute("jira1.logout", new Object[]{getLoginToken()});
    }

    public String getLoginToken() {
        if (loginToken == null) {
            login();
        }
        return loginToken;
    }

    public void login() {
        List loginParams = new ArrayList();
        loginParams.add(properties.getProperty("jira.username"));
        loginParams.add(properties.getProperty("jira.password"));
        try {
            loginToken = (String) rpcClient.execute("jira1.login", loginParams);
        } catch (Exception e) {
            throw new RuntimeException("Error logging in to Jira", e);
        }
    }

    private class JiraItem {

        private Map attributes;

        public JiraItem(String method, List params, String itemName) throws XmlRpcException {
            boolean found = false;
            List toParams = new ArrayList();
            toParams.add(getLoginToken());
            if (params != null) {
                toParams.addAll(params);
            }
            Object[] listResult = (Object[]) rpcClient.execute(method, toParams);
            for (int i = 0; i < listResult.length; i++) {
                Map map = (Map) listResult[i];
                if (itemName.equals(map.get("name"))) {
                    attributes = map;
                    found = true;
                    break;
                }
            }
            if (!found) {
                throw new RuntimeException(itemName + " not found in results for method " + method);
            }
        }

        public String get(String attribute) {
            return (String) attributes.get(attribute);
        }

        public List getMapInList(String attribute) {
            List list = new ArrayList();
            Map map = new HashMap();
            map.put(attribute, attributes.get(attribute));
            list.add(map);
            return list;
        }
    }
}
