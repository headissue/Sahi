package net.sf.sahi.ant;

/**
 * User: dlewis
 * Date: Dec 5, 2006
 * Time: 6:23:55 PM
 */
public class Report {

    private String logDir;
    private String type;

    public Report() {
        super();
    }

    public Report(String type, String logDir) {
        this.logDir = logDir;
        this.type = type;
    }

    public String getLogDir() {
        return logDir;
    }

    public String getType() {
        return type;
    }

    public void setLogDir(String logDir) {
        this.logDir = logDir;
    }

    public void setType(String type) {
        this.type = type;
    }
}
