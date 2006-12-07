package net.sf.sahi.plugin;

/**
 * User: dlewis
 * Date: Dec 4, 2006
 * Time: 4:59:11 PM
 */
public class Issue {
    private String summary;

    private String description;

    public Issue(String summary, String description) {
        this.summary = summary;
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }
}
