package net.sf.sahi.issue;

/**
 * User: dlewis
 * Date: Dec 4, 2006
 * Time: 4:57:21 PM
 */
public interface IssueCreator {
    void login() throws Exception;

    void createIssue(Issue issue) throws Exception;

    void logout() throws Exception;
}
