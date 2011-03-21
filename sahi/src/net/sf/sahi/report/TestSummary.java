/**
 * @author dlewis
 */
package net.sf.sahi.report;

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


public class TestSummary {

    private String scriptName = null;
    private String logFileName = null;
    private boolean addLink = false;
    private int steps;
    private int failures;
    private int errors;
    private int successes;
    private boolean fail;
	private long timeTaken;
	private String suiteName;

    public boolean addLink() {
        return addLink;
    }

    public void setAddLink(final boolean addLink) {
        this.addLink = addLink;
    }

    public String getLogFileName() {
        return logFileName;
    }

    public void setLogFileName(final String logFileName) {
        this.logFileName = logFileName;
    }

    public int getErrors() {
        return errors;
    }

    public void setErrors(final int errors) {
        this.errors = errors;
    }

    public int getSuccesses() {
        return successes;
    }
    
    public int getFailures() {
        return failures;
    }

    public void setFailures(final int failures) {
        this.failures = failures;
    }

    public String getScriptName() {
        return scriptName;
    }

    public void setScriptName(final String scriptName) {
        this.scriptName = scriptName;
    }

    public int getSteps() {
        return steps;
    }

    public void setSteps(final int steps) {
        this.steps = steps;
    }

    public void incrementFailures() {
        this.failures++;
    }

    public boolean hasFailed() {
        return fail;
    }

    public void incrementErrors() {
        this.errors++;
    }

    public void incrementSuccesses() {
        this.successes++;
    }

    public void setFail(final boolean fail) {
        this.fail = fail;
    }

	public long getTimeTaken() {
		return timeTaken;
	}
	
	public String getSuiteName() {
		return suiteName;
	}
	
	public void setTimeTaken(long timeTaken){
		this.timeTaken = timeTaken;
	}

	public void setSuiteName(String suiteName) {
		this.suiteName = suiteName;
	}
}
