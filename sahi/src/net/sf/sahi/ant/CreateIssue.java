package net.sf.sahi.ant;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

/**
 * User: dlewis
 * Date: Dec 5, 2006
 * Time: 5:09:40 PM
 */
public class CreateIssue extends Task {
    private String tool;
    private String propertiesFile;

    public void setTool(String tool) {
        this.tool = tool;
    }

    public void setPropertiesFile(String propertiesFile) {
        this.propertiesFile = propertiesFile;
    }

    public String getTool() {
        return tool;
    }

    public String getPropertiesFile() {
        return propertiesFile;
    }

    public void execute() throws BuildException {
        if (!"jira".equalsIgnoreCase(tool)) {
            throw new BuildException("tool attribute is mandatory and must be 'jira'");
        }
    }
}
