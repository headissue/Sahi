package net.sf.sahi.nashorn;

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
import org.apache.log4j.Logger;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.ScriptContext;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;
import java.util.logging.Level;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Sahi - Web Automation and Test Tool
 * <p>
 * Copyright  2006  V Narayan Raman
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


public class NashornScriptRunner implements Runnable {

  private static final Logger logger = Logger.getLogger(NashornScriptRunner.class);
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
  protected SahiTestSuite suite;
  protected int errorCount = 0;
  protected String browserException;
  protected boolean stopOnError = true;
  protected String type;
  protected Session session;
  protected String logFileNameBase;
  protected String stackTrace = "";
  Timer stepTimer;
  Report report;
  TestLauncher launcher;
  ScriptEngine nashornEngine;
  private Status status;
  private boolean inProgress;
  private String browserJS;
  private SahiScript script;
  private String recoveryScript;
  private Status scriptStatus = Status.INITIAL;

  NashornScriptRunner(String js) {
    setJS(js);
  }

  public NashornScriptRunner(SahiScript script) {
    this(script, null, null, true);
  }

  public NashornScriptRunner(SahiScript script, SahiTestSuite suite, TestLauncher launcher, boolean setDefaultReporters) {
    this.script = script;
    this.suite = suite;
    this.launcher = launcher;
    setReporter(setDefaultReporters);
    String jsString = script.jsString();
    setJS(jsString);
  }

  public NashornScriptRunner() {

  }

  public boolean isRunning() {
    return isRunning;
  }

  public boolean isStopped() {
    return stopped;
  }

  public Status getStatus() {
    return status;
  }

  public void setStatus(Status status) {
    if (status == Status.FAILURE) {
      errorCount++;
    }
    this.status = status;
  }

  public String getBrowserException() {
    return browserException;
  }

  protected void setBrowserException(String failureMessage) {
    logger.debug("setBrowserException: failureMessage=" + failureMessage);
    this.browserException = failureMessage;
  }

  public boolean doneStep(String lastId) {
    logger.debug("doneStep with (unused) lastId=" + lastId);
    return done;
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

  public Session getSession() {
    return this.session;
  }

  public void setSession(Session session) {
    this.session = session;
    this.logFileNameBase = Utils.createLogFileName(script.getScriptName()); //Utils.getFormattedDateForFile();
  }

  private String getFromStep(String step, String prefix) {
    if (step == null)
      return "";
    String name = "";
    step = step.trim();
    int indexOfPrefix = step.indexOf(prefix);
    if (indexOfPrefix != -1) {
      name = step.substring(indexOfPrefix + prefix.length(), step.indexOf(").", indexOfPrefix));
      name = name.trim();
      name = name.substring(1).trim();
      if (name.startsWith("\"")) name = name.substring(1, name.length() - 1);
      else if (name.startsWith("'")) name = name.substring(1, name.length() - 1);
    }
    return name;
  }

  protected String getPopupNameFromStep(String step) {
    return getFromStep(step, "_sahi._popup");
  }

  protected String getDomainFromStep(String step) {
    return getFromStep(step, "_sahi._domain");

  }

  String stripPopup(String step) {
    if (step == null)
      return null;
    if (step.startsWith("_sahi._popup"))
      return step.substring(step.indexOf(").") + 2);
    return step;
  }

  private String encode(String s) {
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
        logger.debug("step=" + step);
        String message = "Window/Domain not found:  popupNameFromStep=" + popupNameFromStep +
            "; derivedName=" + derivedName +
            "; windowName=" + windowName + "; windowTitle=" + windowTitle +
            "; wasOpened=" + wasOpened + "; domain=" + domain;
        logger.debug(message);
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

  public int setStep(String step, String debugInfo) {
    return setStep(step, debugInfo, "NORMAL");
  }

  public int getMaxCyclesForPageLoad() {
    return Configuration.getMaxCyclesForPageLoad();
  }

  public void setRecoveryScript(String recoveryScript) {
    this.recoveryScript = recoveryScript;
  }

  public void markStepInProgress(String stepId, ResultType type) {
    logger.debug("markStepInProgress: stepId=" + stepId + " type=" + type);

    if (stepTimer != null) stepTimer.cancel();
    inProgress = true;
    stepTimer = new Timer();
    stepTimer.schedule(new StepInProgressMonitor(this, stepId, type), (long) (Configuration.getTimeBetweenStepsOnError() * 1.5));
  }

  public int getTimeBetweenSteps() {
    return Configuration.getTimeBetweenSteps();
  }

  public synchronized int setStep(String step, String debugInfo, String type) {
    logger.debug("setStep: step=" + step + "; debugInfo=" + debugInfo + "; type=" + type);

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

  public void run() {

    initializeEngine();
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
      logger.warn(Utils.getStackTraceString(e, false));
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

  protected void initializeEngine() {
    ScriptEngineManager scriptManager = new ScriptEngineManager();
    nashornEngine = scriptManager.getEngineByName("nashorn");
    nashornEngine.getBindings(ScriptContext.ENGINE_SCOPE).put("NashornScriptRunner", this);
  }

  private void addBrowserExceptionToReport() {
    report.addResult(browserException + "\n" + stackTrace, ResultType.ERROR, debugInfo, null);
  }

  private void runScript() throws ScriptException {
    if (script != null) {
      nashornEngine.put(ScriptEngine.FILENAME, script.getScriptName());
    }
    nashornEngine.eval(js);
  }

  protected void loadSahiLibary() {
    String lib = Configuration.getSahiJavascriptLib();
    nashornEngine.put(ScriptEngine.FILENAME, Utils.concatPaths(Configuration.getHtdocsRoot(),
        "spr/lib.js"));
    try {
      nashornEngine.eval(lib);
    } catch (ScriptException e) {
      logger.error("could not eval lib.js", e);
    }
  }

  private void assertIsSahiPresent() {
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
    logger.debug("markStepDone: stepId=" + stepId + " type=" + type + " failureMessage=" + failureMessage);
    if (stepId.equals("" + this.counter)) {
      if (this.done) return;
      if (type == ResultType.FAILURE) {
        setStatus(Status.FAILURE);
        setBrowserException(SahiScript.stripSahiFromFunctionNames(step) + "\n" + failureMessage);
      }
      if (type == ResultType.ERROR) {
        setStatus(Status.ERROR);
        if (this.stopOnError) {
          setBrowserException(SahiScript.stripSahiFromFunctionNames(step) + "\n" + failureMessage);
        }
      }
      this.done = true;
      cancelStepInProgressTimer(); // make inprogress false only after marking done.
    } else {
        logger.debug("Different step received (NOT a problem mostly): " + stepId + "; current:" + this.counter + "; type=" + type + "; failureMessage=" + failureMessage);
    }
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

  public int errorCount() {
    return errorCount;
  }

  public void cancelStepInProgressTimer() {
    logger.debug("cancelStepInProgressTimer");
    try {
      if (stepTimer != null) stepTimer.cancel();
      inProgress = false;
    } catch (Exception e) {
    }
  }

  public void setVariable(String key, String value) {
    logger.debug("Setting key="+key+" value="+value);
    variables.put(key, value);
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

  public String getVariable(String key) {
    return (String) variables.get(key);
  }

  public boolean hasErrors() {
    return errorCount > 0;
  }

  public void setStopOnError(boolean stop) {
    this.stopOnError = stop;
  }

  public int getBrowserRetries() {
    return browserRetries;
  }

  public void setBrowserRetries(int browserRetries) {
    logger.debug("setBrowserRetries: browserRetries=" + browserRetries);
    this.browserRetries = browserRetries;
    if (browserRetries > 0) {
      cancelStepInProgressTimer();
    }
  }

  public String getBrowserJS() {
    return browserJS;
  }

  public void setBrowserJS(String browserJS) {
    this.browserJS = browserJS;
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

  public Status getScriptStatus() {
    return this.scriptStatus;
  }

  public void setScriptStatus(Status scriptStatus) {
    this.scriptStatus = scriptStatus;
  }

  public boolean isPartOfSuite() {
    return suite != null;
  }

  public void stop() {
    if (this.stopped) return;
    logger.debug("stop");
    this.stopped = true;
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

  public String getLogFileNameBase() {
    return logFileNameBase;
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
    logger.debug("Result for "+ js + ": " + result.toString());
    return result.toString();
  }

  public ScriptEngine getEngine() {
    return nashornEngine;
  }

  class StepInProgressMonitor extends TimerTask {
    private NashornScriptRunner runner;
    private String stepId;
    private ResultType type;

    public StepInProgressMonitor(NashornScriptRunner runner, String stepId, ResultType type) {
      this.runner = runner;
      this.stepId = stepId;
      this.type = type;
    }

    public void run() {
      this.runner.markStepDone(stepId, type, "[auto]");
    }
  }
}
