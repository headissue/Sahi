package net.sf.sahi.plugin;

import org.apache.xmlrpc.XmlRpcException;

/**
 * User: dlewis
 * Date: Dec 4, 2006
 * Time: 4:57:21 PM
 */
public interface IssueCreator {
     void createIssue(Issue issue) throws Exception;
}
