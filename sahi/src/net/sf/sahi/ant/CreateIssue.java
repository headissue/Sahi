package net.sf.sahi.ant;

/**
 * User: dlewis
 * Date: Dec 5, 2006
 * Time: 5:09:40 PM
 */
public class CreateIssue {

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
}
