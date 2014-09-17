package net.sf.sahi.rhino;

import net.sf.sahi.config.Configuration;
import net.sf.sahi.playback.SahiScript;
import net.sf.sahi.report.HtmlReporter;
import net.sf.sahi.report.Report;
import net.sf.sahi.report.ResultType;
import net.sf.sahi.session.Session;
import net.sf.sahi.session.Status;
import net.sf.sahi.test.SahiTestSuite;
import net.sf.sahi.test.TestLauncher;
import net.sf.sahi.util.Utils;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.ScriptContext;
import java.util.logging.Logger;

/**
 * Sahi - Web Automation and Test Tool
 * <p/>
 * Copyright  2006  V Narayan Raman
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
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

  private Status scriptStatus = Status.INITIAL;

  protected String stackTrace = "";

  ScriptEngine nashornEngine;

  RhinoScriptRunner(String js) {
    setJS(js);
  }

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

  public void setSession(Session session) {
    super.setSession(session);
    this.logFileNameBase = Utils.createLogFileName(script.getScriptName()); //Utils.getFormattedDateForFile();
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

  public void setRecoveryScript(String recoveryScript) {
    this.recoveryScript = recoveryScript;
  }

  private void initializeScope(ScriptEngine nashornEngine) {
    // make ScriptRunner available to JS
    //scope = new SimpleBindings();
  }

  public void run() {

    ScriptEngineManager scriptManager = new ScriptEngineManager();
    nashornEngine = scriptManager.getEngineByName("nashorn");
    nashornEngine.getBindings(ScriptContext.ENGINE_SCOPE).put("ScriptRunner", this);
    report.startTimer();
    try {
      loadSahiLibary();
      assertIsSahiPresent();
      runScript();
    } catch (ScriptException ee) {
      setScriptStatus(Status.FAILURE);
      incrementErrors();
      if (browserException != null) {
        report.addResult(browserException + "\n" + stackTrace, ResultType.ERROR, debugInfo, null);
        browserException = null;
      } else {
        report.addResult("ERROR " + "\n" + stackTrace, ResultType.ERROR, script.getLineDebugInfo(ee.getLineNumber() - 1), ee.getMessage());
      }

      if (recoveryScript != null) {
        try {
          report.addResult("--- Recovery Start ---", ResultType.INFO, null, null);
          nashornEngine.eval(recoveryScript);
        } catch (ScriptException ee2) {
          setScriptStatus(Status.FAILURE);
          if (browserException != null) {
            report.addResult(browserException, ResultType.ERROR, debugInfo, null);
          } else {
            report.addResult("ERROR ", ResultType.ERROR, script.getDebugInfo(ee2.getLineNumber() - 1), ee2.getMessage());
          }
        } finally {
          report.addResult("--- Recovery End ---", ResultType.INFO, null, null);
        }
      }
    } catch (Exception e) {
      logger.warning(Utils.getStackTraceString(e, false));
     setScriptStatus(Status.FAILURE);
      incrementErrors();
      report.addResult("ERROR ", ResultType.ERROR, e.getMessage(), e.getMessage());
    } finally {
      // Exit from the context.
//			report.addResult("Total Memory in JVM is: " + Runtime.getRuntime().totalMemory()/(1024*1024) + " MB;<br/>" +
//					"Free Memory in JVM is: " + Runtime.getRuntime().freeMemory()/(1024*1024) + " MB;<br/>" +
//							"Memory used during this test is: " + ((initialFreeMemory - Runtime.getRuntime().freeMemory())/(1024*1024)) + " MB", 
//							ResultType.CUSTOM2, "", null);
      try {
        assertIsSahiPresent();
        nashornEngine.eval("_sahi.callOnScriptEnd();");
      } catch (Exception e2) {
        report.addResult("ERROR ", ResultType.ERROR, e2.getMessage(), e2.getMessage());
        e2.printStackTrace();
      }
      if (this.scriptStatus == Status.INITIAL) this.scriptStatus = Status.SUCCESS;
      stop();
    }
  }

  private void addBrowserExceptionToReport() {
    report.addResult(browserException + "\n" + stackTrace, ResultType.ERROR, debugInfo, null);
  }

  private void runScript() throws ScriptException {
    nashornEngine.eval(js);
  }

  private void loadSahiLibary() throws ScriptException {
    String lib = Configuration.getRhinoLibJS();
    nashornEngine.eval(lib);
  }

  private void assertIsSahiPresent() {
    //System.out.println(new Date() + " " + Thread.currentThread().getName() + " " + debug);
    assert nashornEngine.get("_sahi") != null;
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
        setScriptStatus(Status.FAILURE);
        if (!this.stopOnError) {
          report.addResult(SahiScript.stripSahiFromFunctionNames(step), type, debugInfo, failureMessage + "\n" + stackTrace);
        }
      } else {
        if (type == ResultType.FAILURE) {
          setScriptStatus(Status.FAILURE);
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
    if (setDefaultReporters) {
      if (suite != null) {
        report = new Report(reportFileNameBase, suite.getListReporter());
      } else {
        report = new Report(reportFileNameBase, new HtmlReporter());
      }
    } else {
      report = new Report(reportFileNameBase);
    }

  }

  public String getScriptName() {
    return script.getScriptName();
  }

  public SahiScript getScript() {
    return script;
  }

  public int getThreadNo() {
    if (launcher == null) return 0;
    return launcher.getThreadNo();
  }

  public Report getReport() {
    return report;
  }

  public void log(String message, String debugInfo, String resultType) {
    report.addResult(message, ResultType.getType(resultType), debugInfo, "");
  }

  public void logException(String message, String debugInfo, boolean isError) {
    if (isError) incrementErrors();
    report.addResult("Logging exception: ", isError ? ResultType.ERROR : ResultType.CUSTOM, debugInfo, message);
  }

  public void incrementErrors() {
    errorCount++;
  }

  public void logExceptionWithLineNumber(String message, int lineNumber, boolean isFailure) {
    logException(message, script.getDebugInfo(lineNumber), isFailure);
  }

  public void setScriptStatus(Status scriptStatus) {
    this.scriptStatus = scriptStatus;
  }

  public Status getScriptStatus() {
    return this.scriptStatus;
  }

  public boolean isPartOfSuite() {
    return suite != null;
  }

  public void stop() {
    if (this.stopped) return;
    super.stop();
    try {
      report.stopTimer();
      report.generateTestReport(getLogFileNameBase());
    } catch (Exception e) {
      e.printStackTrace();
    }
    if (suite != null) {
      suite.notifyComplete(launcher);
    }
  }

  public String eval(String js) {
    Object result;
    assertIsSahiPresent();
    try {
      result = nashornEngine.eval("_sahi.toJSON(" + js + ")");
    } catch (Exception e) {
      result = e.getLocalizedMessage();
    }
    assertIsSahiPresent();
    //System.out.println("<<< >>> " + result.toString());
    return result.toString();
  }

}
