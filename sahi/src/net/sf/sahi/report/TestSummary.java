package net.sf.sahi.report;

/**
 * @author dlewis
 */
public class TestSummary {
    private String scriptName = null;

    private String logFile = null;

    private boolean addLink = false;

    private int steps;

    private int failures;

    private int errors;

    public boolean addLink() {
        return addLink;
    }

    public void setAddLink(boolean addLink) {
        this.addLink = addLink;
    }

    public String getLogFile() {
        return logFile;
    }

    public void setLogFile(String logFile) {
        this.logFile = logFile;
    }

    public int getErrors() {
        return errors;
    }

    public void setErrors(int errors) {
        this.errors = errors;
    }

    public int getFailures() {
        return failures;
    }

    public void setFailures(int failures) {
        this.failures = failures;
    }

    public String getScriptName() {
        return scriptName;
    }

    public void setScriptName(String scriptName) {
        this.scriptName = scriptName;
    }

    public int getSteps() {
        return steps;
    }

    public void setSteps(int steps) {
        this.steps = steps;
    }
}
