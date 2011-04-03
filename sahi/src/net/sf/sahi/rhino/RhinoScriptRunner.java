package net.sf.sahi.rhino;

import java.util.logging.Logger;

import net.sf.sahi.config.Configuration;
import net.sf.sahi.playback.SahiScript;
import net.sf.sahi.report.HtmlReporter;
import net.sf.sahi.report.Report;
import net.sf.sahi.report.ResultType;
import net.sf.sahi.session.Status;
import net.sf.sahi.test.SahiTestSuite;
import net.sf.sahi.test.TestLauncher;
import net.sf.sahi.util.Utils;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.RhinoException;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

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


public class RhinoScriptRunner extends ScriptRunner implements Runnable {

	private SahiScript script;

	Report report;

	TestLauncher launcher;

	private String recoveryScript;
	
	private static final Logger logger = Logger.getLogger("net.sf.sahi.rhino.ScriptRunner");

	private Scriptable scope;

	private Status scriptStatus = Status.INITIAL;

	protected String stackTrace = "";

	RhinoScriptRunner(String js) {
		setJS(js);	}

	public RhinoScriptRunner(SahiScript script) {
		this(script, null, null, true);
	}
	public RhinoScriptRunner(SahiScript script, SahiTestSuite suite, TestLauncher launcher, boolean setDefaultReporters) {
		this.script = script;
		this.suite = suite;
		this.launcher = launcher;
		setReporter(setDefaultReporters);
		String jsString = script.jsString();
		setJS(jsString);
	}

	private void setJS(String jsString) {
		this.js = "_sahi.start();" + jsString;
	}

	public void execute() {
		new Thread(this).start();
	}
	
	public void executeAndWait() {
		final Thread thread = new Thread(this);
		thread.start();
		try {
			thread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public void setRecoveryScript(String recoveryScript){
		this.recoveryScript = recoveryScript;
	}

	public void run() {
//		long initialFreeMemory = Runtime.getRuntime().freeMemory();
		report.startTimer();
		String lib = Configuration.getRhinoLibJS();
		Context cx = Context.enter();
		cx.setOptimizationLevel(Configuration.getRhinoOptimizationLevel());
			
		scope = cx.initStandardObjects();
		try {
			Object wrappedOut = Context.javaToJS(this, scope);
			ScriptableObject.putProperty(scope, "ScriptRunner", wrappedOut);

			cx.evaluateString(scope, lib, "RhinoScriptRunner.run", 1, null);
			cx.evaluateString(scope, js, "RhinoScriptRunner.run", 1, null);

			// System.out.println("End of script: " + script.getScriptName());
		} catch (RhinoException ee) {
			if (browserException != null){
				this.scriptStatus = Status.FAILURE;
				report.addResult(browserException + "\n" + stackTrace, ResultType.ERROR, debugInfo, null);
				browserException= null;
			}else{
				this.scriptStatus = Status.FAILURE;
				report.addResult("ERROR " + "\n" + stackTrace, ResultType.ERROR, script.getLineDebugInfo(ee.lineNumber()-1), ee.details());
			}
			setHasError();

			if(recoveryScript != null){
				try{
					report.addResult("--- Recovery Start ---", ResultType.INFO, null, null);
					cx.evaluateString(scope, recoveryScript, "RhinoScriptRunner.run", 1, null);
				} catch (RhinoException ee2) {
					if (browserException != null){
						this.scriptStatus = Status.FAILURE;
						report.addResult(browserException, ResultType.ERROR, debugInfo, null);
					}else{
						this.scriptStatus = Status.FAILURE;
						report.addResult("ERROR ", ResultType.ERROR, script.getDebugInfo(ee2.lineNumber()-1), ee2.details());
					}
				} finally {
					report.addResult("--- Recovery End ---", ResultType.INFO, null, null);
				}
			}
		} catch (Exception e) {
			logger.warning(Utils.getStackTraceString(e, false));
			this.scriptStatus = Status.FAILURE;
			report.addResult("ERROR ", ResultType.ERROR, e.getMessage(), e.getMessage());
			setHasError();
		} finally {
			// Exit from the context.
//			report.addResult("Total Memory in JVM is: " + Runtime.getRuntime().totalMemory()/(1024*1024) + " MB;<br/>" +
//					"Free Memory in JVM is: " + Runtime.getRuntime().freeMemory()/(1024*1024) + " MB;<br/>" +
//							"Memory used during this test is: " + ((initialFreeMemory - Runtime.getRuntime().freeMemory())/(1024*1024)) + " MB", 
//							ResultType.CUSTOM2, "", null);
			try{
				cx.evaluateString(scope, "_sahi.callOnScriptEnd();", "RhinoScriptRunner.run", 1, null);
			}catch(Exception e2){
				report.addResult("ERROR ", ResultType.ERROR, e2.getMessage(), e2.getMessage());
			}
			Context.exit();
			if (this.scriptStatus == Status.INITIAL) this.scriptStatus = Status.SUCCESS;
			stop();
			cx = null;
		}
	}
	
	public void markStepDoneFromLib(String stepId, String typeName, String failureMessage) {
		markStepDone(stepId, ResultType.getType(typeName), failureMessage);
	}

	public void setStackTrace(String stackTrace) {
		this.stackTrace = stackTrace;
	}
	
	public boolean needsStackTrace() {
		return (this.browserRetries > 2 && "".equals(this.stackTrace));
	}	
	
	public void markStepDone(String stepId, ResultType type, String failureMessage) {
		super.markStepDone(stepId, type, failureMessage);
		if (stepId.equals("" + this.counter)) {
			if (type == ResultType.ERROR) {
				this.scriptStatus = Status.FAILURE;
				if (!this.stopOnError){
					report.addResult(SahiScript.stripSahiFromFunctionNames(step), type, debugInfo, failureMessage + "\n" + stackTrace);
				}
			} else {
				if (type == ResultType.FAILURE){
					this.scriptStatus = Status.FAILURE;
					failureMessage += "\n" + stackTrace;
				}
				report.addResult(SahiScript.stripSahiFromFunctionNames(step), type, debugInfo, failureMessage);
			} 
		}
	}	

	public String getScriptFilePath() {
		return getScript().getFilePath();
	}	

	private void setReporter(boolean setDefaultReporters) {
		final String reportFileNameBase = getScriptName();
		if (setDefaultReporters){
			if (suite != null) {
				report = new Report(reportFileNameBase, suite.getListReporter());
			} else {
				report = new Report(reportFileNameBase, new HtmlReporter());
			}
		}else{
			report = new Report(reportFileNameBase);
		}
		
	}

	public String getScriptName() {
		return script.getScriptName();
	}

	public SahiScript getScript() {
		return script;
	}

	public int getThreadNo(){
		if (launcher == null) return 0; 
		return launcher.getThreadNo();
	}
	
	public Report getReport() {
		return report;
	}

	public void log(String message, String debugInfo, String resultType){
		report.addResult(message, ResultType.getType(resultType), debugInfo, "");
	}

	public void logException(String message, String debugInfo, boolean isError){
		if (isError) setHasError();
		report.addResult("Logging exception: ", isError ? ResultType.ERROR : ResultType.CUSTOM, debugInfo, message);
	}

	public void setHasError(){
		setStatus(Status.FAILURE);
	}	
	
	public void logExceptionWithLineNumber(String message, int lineNumber, boolean isFailure){
		logException(message, script.getDebugInfo(lineNumber), isFailure);
	}
	
	public Status getScriptStatus(){
		return this.scriptStatus;
	}
	
	public boolean isPartOfSuite(){
		return suite != null;
	}
	
	public void stop() {
		super.stop();
		try{
			report.stopTimer();
			report.generateTestReport();
		}catch(Exception e){
			e.printStackTrace();
		}
		if (suite != null){
			suite.notifyComplete(launcher);
		}
	}

	public String eval(String js) {
		Context cx = Context.enter();
		Object result;
		try{
			result = cx.evaluateString(scope, "_sahi.toJSON(" + js + ")", "RhinoScriptRunner.eval", 1, null);
		}catch(Exception e){
			result = e.getLocalizedMessage();
		}
//		System.out.println("<<< >>> " + result.toString());
		Context.exit();
		return result.toString();
	}

}
