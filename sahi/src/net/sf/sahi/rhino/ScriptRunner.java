package net.sf.sahi.rhino;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.sahi.config.Configuration;
import net.sf.sahi.playback.SahiScript;
import net.sf.sahi.report.Report;
import net.sf.sahi.report.ResultType;
import net.sf.sahi.session.Session;
import net.sf.sahi.session.Status;
import net.sf.sahi.test.SahiTestSuite;
import net.sf.sahi.util.Utils;

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


public class ScriptRunner {
	private static final Logger logger = Logger.getLogger("net.sf.sahi.rhino.ScriptRunner");
	protected int counter = 0;
	protected String step;
	protected String js;
	protected String debugInfo;
	protected boolean done;
	protected boolean stopped = false;
	protected Map<String, String> variables = new HashMap<String, String>();
	protected boolean isRunning;
	protected int retries = 0;
	protected int browserRetries;

	private Status status;
	protected SahiTestSuite suite;
	protected int errorCount = 0;
	protected String browserException;
	protected boolean stopOnError = true;
	protected String type;
	Timer stepTimer;
	private boolean inProgress;
	private Session session;
	private String browserJS;
	
	public ScriptRunner() {
		super();
	}

	public int setStep(String step, String debugInfo) {
		return setStep(step, debugInfo, "NORMAL");
	}

	public void execute() {
	}

	public void executeAndWait() {
	}

	public synchronized int setStep(String step, String debugInfo, String type) {
		if (logger.isLoggable(Level.FINER)) {
			logger.finer("step=" + step + "; debugInfo=" + debugInfo + "; type=" + type);
		}
		
		counter++;
		this.retries = 0;
		this.type = type;
		this.step = step;
		this.debugInfo = debugInfo;
		this.done = false;
		this.inProgress = false;
		this.browserRetries = 0;
		this.browserException = null;
		this.status = Status.SUCCESS;
		return counter;
	}

	public boolean doneStep(String lastId) {
		if (logger.isLoggable(Level.FINER)) {
			logger.finer("doneStep with (unused) lastId="+lastId);
		}
		return done;
	}

	public String getStep() {
		return step;
	}

	public String getDebugInfo() {
		return debugInfo;
	}

	public int getCounter() {
		return counter;
	}
	
	public synchronized void markStepDone(String stepId, ResultType type, String failureMessage) {
		if (logger.isLoggable(Level.FINER)) {
			logger.finer("markStepDone: stepId=" + stepId + " type=" + type + " failureMessage=" + failureMessage);
		}
		if (stepId.equals("" + this.counter)) {
			if (this.done) return;
			if (type == ResultType.FAILURE) {
				setStatus(Status.FAILURE);
				setBrowserException(SahiScript.stripSahiFromFunctionNames(step) + "\n" + failureMessage);
			}
			if (type == ResultType.ERROR) {
				setStatus(Status.ERROR);
				if (this.stopOnError){
					setBrowserException(SahiScript.stripSahiFromFunctionNames(step) + "\n" + failureMessage);
				}
			}
			this.done = true;
			cancelStepInProgressTimer(); // make inprogress false only after marking done. 
		} else {
			if (logger.isLoggable(Level.FINER)) {
				logger.finer("Different step received (NOT a problem mostly): " + stepId + "; current:" + this.counter + "; type="+type+"; failureMessage="+failureMessage);
			}
		}

	}

	public void setStopOnError(boolean stop){
		this.stopOnError = stop;
	}

	public void markStepInProgress(String stepId, ResultType type) {
		if (logger.isLoggable(Level.FINER)) {
			logger.finer("markStepInProgress: stepId=" + stepId + " type=" + type);
		}
		
		if (stepTimer != null) stepTimer.cancel();
		inProgress = true;
		stepTimer = new Timer();
		stepTimer.schedule(new StepInProgressMonitor(this, stepId, type), (long) (Configuration.getTimeBetweenStepsOnError()*1.5));
	}

	public void cancelStepInProgressTimer() {
		if (logger.isLoggable(Level.FINER)) {
			logger.finer("cancelStepInProgressTimer");
		}
		try{
			if (stepTimer != null) stepTimer.cancel();
			inProgress = false;
		}catch(Exception e){}
	}

	protected void setBrowserException(String failureMessage) {
		if (logger.isLoggable(Level.FINER)) {
			logger.finer("setBrowserException: failureMessage="+failureMessage);
		}	
		this.browserException = failureMessage;
	}

	public void stop() {
		if (logger.isLoggable(Level.FINER)) {
			logger.finer("stop");
		}		
		if (this.stopped) return;
		this.stopped = true;
	}

	public boolean isStopped() {
		return stopped;
	}

	public boolean isRunning() {
		return isRunning;
	}

	public void setBrowserRetries(int browserRetries) {
		if (logger.isLoggable(Level.FINER)) {
			logger.finer("setBrowserRetries: browserRetries="+browserRetries);
		}			
		this.browserRetries = browserRetries;
		if (browserRetries > 0){
			cancelStepInProgressTimer();
		}
	}

	public int getBrowserRetries() {
		return browserRetries;
	}

	public void setStatus(Status status) {
		if (status == Status.FAILURE) {
			errorCount++;
		}
		this.status = status;
	}

	public Status getStatus() {
		return status;
	}

	public boolean hasErrors() {
		return errorCount > 0;
	}
	
	public int errorCount() {
		return errorCount;
	}

	public String getBrowserException() {
		return browserException;
	}

	String stripPopup(String step) {
		if (step == null)
			return null;
		if (step.startsWith("_sahi._popup"))
			return step.substring(step.indexOf(").") + 2);
		return step;
	}

	protected String getPopupNameFromStep(String step) {
		return getFromStep(step, "_sahi._popup");
	}
	
	protected String getDomainFromStep(String step) {
		return getFromStep(step, "_sahi._domain");
	
	}

	private String getFromStep(String step, String prefix) {
		if (step == null)
			return "";
		String name = "";
		step = step.trim();
		int indexOfPrefix = step.indexOf(prefix);
		if (indexOfPrefix != -1) {
			name = step.substring(indexOfPrefix  + prefix.length(), step.indexOf(").", indexOfPrefix));
			name = name.trim();
			name = name.substring(1).trim();
			if (name.startsWith("\"")) name = name.substring(1, name.length() - 1);
			else if (name.startsWith("'")) name = name.substring(1, name.length() - 1);
		}
		return name;
	}

	public int getMaxCyclesForPageLoad(){
		return Configuration.getMaxCyclesForPageLoad();
	}

	public int getTimeBetweenSteps(){
		return Configuration.getTimeBetweenSteps();
	}
	
	public String getVariable(String key) {
		// System.out.println(variables);
		return (String) variables.get(key);
	}

	public void setVariable(String key, String value) {
		// System.out.println("Setting key="+key+" value="+value);
		variables.put(key, value);
	}
	
	private String encode(String s){
		if (s == null) return s;
		try {
			return URLEncoder.encode(s, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return s;
		}
	}
	
	public synchronized String getStepJSON(String derivedName, String windowName, String windowTitle, String domain, String wasOpened) {
		if (!isRunning) {
			isRunning = true;
		}
		if (stopped)
			return "{'type':'STOP'}";
		if (inProgress || done)
			return "{'type':'WAIT'}";

		String popupNameFromStep = getPopupNameFromStep(step);
		String domainFromStep = getDomainFromStep(step);
		if (((popupNameFromStep.equals("") && derivedName.equals("") && !"1".equals(wasOpened))
				|| (!popupNameFromStep.equals("") &&
						(areSame(windowName, popupNameFromStep) 
								|| areSame(windowTitle, popupNameFromStep))))
			&& ((domainFromStep.equals("") && domain.equals("")) || areSame(domain, domainFromStep))
			) {
			String json = "{'origStep': \"" + Utils.makeString(step) + "\", "
						+ "'step': \"" + Utils.makeString(stripPopup(step))
						+ "\", " + "'type': \"" + type + "\", " + "'debugInfo': \""
						+ Utils.makeString(debugInfo) + "\", " + "'stepId':"
						+ counter + "}";
			return encode(json);
		} else {
			this.retries++;
			if (this.retries > 490) {
				 System.out.println(">>>> step="+step);
				 String message = "Window/Domain not found:  popupNameFromStep="+popupNameFromStep+
				 "; derivedName="+derivedName +
				 "; windowName="+windowName+"; windowTitle="+windowTitle+
				 "; wasOpened="+wasOpened+"; domain="+domain;
				 System.out.println(message);
				 if (this.retries > 500) {
					markStepDone("" + counter, ResultType.ERROR, message);
					return encode("{'origStep': \"" + Utils.makeString(step) + "\", " + 
							"'step': \"" + Utils.makeString(stripPopup(step)) + "\", " + 
							"'type': \"JSERROR\", " + 
							"'debugInfo': \"" + Utils.makeString(debugInfo) + "\", " + 
							"'stepId':" + counter + ",'message':\"" + message + "\"}");
				}
			}
			return "{'type':'WAIT'}";
		}
	}
	
	boolean areSame(String s, String re) {
		if ("".equals(s)) return false; // blank is treated almost as null in this case.
		if (s.equals(re)) return true;
		if (re.startsWith("/") && (re.endsWith("/") || re.endsWith("/i"))) {
			boolean ignoreCase = re.endsWith("/i");
			String re2 = re.replaceAll("^/|/[i]?$", ""); // replace start and end slashes
			Pattern pattern = ignoreCase ? Pattern.compile(re2, Pattern.CASE_INSENSITIVE) : Pattern.compile(re2);
			Matcher matcher2 = pattern.matcher(s);
			return matcher2.find(); 
			
		} 
		return false;
	}

	public Report getReport() {
		return null;
	}

	public String getScriptFilePath() {
		return null;
	}

	public String getScriptName() {
		return null;
	}
	
	public SahiScript getScript() {
		return null;
	}
	
	public String getBrowserJS() {
		return browserJS;
	}
	
	public void setBrowserJS(String browserJS) {
		this.browserJS = browserJS;
	}
	
	public int getThreadNo(){
		return -1;
	}
	
	public void setSession(Session session) {
		this.session = session;
	}
	
	public Session getSession(){
		return this.session;
	}	
    class StepInProgressMonitor extends TimerTask {
        private ScriptRunner runner;
        private String stepId;
        private ResultType type;

        public StepInProgressMonitor(ScriptRunner runner, String stepId, ResultType type) {
            this.runner = runner;
            this.stepId = stepId;
            this.type = type;
	}
	
        public void run() {
            this.runner.markStepDone(stepId, type, "[auto]");
        }
    }
}
