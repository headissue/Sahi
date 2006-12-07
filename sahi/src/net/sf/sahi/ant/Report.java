package net.sf.sahi.ant;

import org.apache.tools.ant.Task;
import org.apache.tools.ant.BuildException;
import net.sf.sahi.report.Formatter;
import net.sf.sahi.report.JUnitFormatter;

import java.util.List;

/**
 * User: dlewis
 * Date: Dec 5, 2006
 * Time: 6:23:55 PM
 */
public class Report extends Task {
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

    public void execute() {
        if(!("junit".equalsIgnoreCase(type) || "html".equalsIgnoreCase(type))) {
           throw new BuildException("Valid valued for attribute 'type' of tag 'reporter' are html or junit");
        }
    }
}
