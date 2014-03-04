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
package net.sf.sahi.command;

import java.util.Properties;

import net.sf.sahi.config.Configuration;
import net.sf.sahi.playback.FileScript;
import net.sf.sahi.playback.SahiScript;
import net.sf.sahi.playback.ScriptFactory;
import net.sf.sahi.report.LogViewer;
import net.sf.sahi.report.ResultType;
import net.sf.sahi.request.HttpRequest;
import net.sf.sahi.response.HttpFileResponse;
import net.sf.sahi.response.HttpResponse;
import net.sf.sahi.response.NoCacheHttpResponse;
import net.sf.sahi.response.SimpleHttpResponse;
import net.sf.sahi.rhino.RhinoScriptRunner;
import net.sf.sahi.rhino.ScriptRunner;
import net.sf.sahi.session.Session;
import net.sf.sahi.session.Status;
import net.sf.sahi.test.ProcessHelper;
import net.sf.sahi.util.Utils;

public class Player {

    public void stepWisePlay(final HttpRequest request) {
        startPlayback(request.session(), false, "1");
    }

    public void start(final HttpRequest request) {
        startPlayback(request.session(), true, "1");
    }

    public void stop(final HttpRequest request) {
        Session session = request.session();
        if (session.getRecorder() != null) session.getRecorder().stop();
        if (session.getScriptRunner() != null) session.getScriptRunner().stop();
    }

//    public void stop(final Session session) {
//        try {
//            if (session.getRecorder() != null) session.getRecorder().stop();
//            if (session.getScriptRunner() != null) session.getScriptRunner().stop();
////            if (session.getReport() != null) {
////            	session.getReport().generateTestReport();
////	            Status testStatus = session.getReport().getTestSummary().hasFailed() ? Status.FAILURE : Status.SUCCESS;
////	            session.setStatus(testStatus);
////            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            session.getScriptRunner().setStatus(Status.FAILURE);
//        }
//        SahiTestSuite suite = SahiTestSuite.getSuite(session.id());
//        if (suite != null) {
//        	//System.out.println("Player.stop: "+session.id());
//            suite.notifyComplete(session.id());
//        }
//    }

    public void setScriptFile(final HttpRequest request) {
        Session session = request.session();
        String dir = request.getParameter("dir");
        String fileName = request.getParameter("file");
        session.setIsWindowOpen("1".equals(request.getParameter("manual")));
        String filePath = Utils.concatPaths(dir, fileName);
        setScript(session, filePath);
    }

    public void setScriptUrl(final HttpRequest request) {
        Session session = request.session();
        String url = request.getParameter("url");
        session.setIsWindowOpen("1".equals(request.getParameter("manual")));
        setScript(session, url);
    }

    public void resetScript(final HttpRequest request) {
        Session session = request.session();
    	String scriptPath = session.getVariable("sahi_scriptPath");
    	stop(request);
    	setScript(session, scriptPath);
    }
    private void setScript(Session session, String scriptPath){
        SahiScript script = new ScriptFactory().getScript(scriptPath);
        RhinoScriptRunner scriptRunner = new RhinoScriptRunner(script);
		session.setScriptRunner(scriptRunner);
        startPlayback(session, true, "1");
    }

    private void startPlayback(final Session session, final boolean resetConditions, String paused) {
        if (resetConditions) {
            session.removeVariables(".*");
        }
        ScriptRunner scriptRunner = session.getScriptRunner();
		scriptRunner.setStatus(Status.RUNNING);
		session.setIsPlaying(true);
//        session.setVariable("sahi_play", "1");
        session.setVariable("sahi_paused", paused);
        session.setVariable("sahi_controller_tab", "playback");
    	session.setVariable("sahi_scriptPath", scriptRunner.getScriptFilePath());
        scriptRunner.execute();
    }

    public HttpResponse isPlaying(final HttpRequest request){
        Session session = request.session();
        return new SimpleHttpResponse(session.isPlaying() ? "1" : "0");
//        ScriptRunner scriptRunner = session.getScriptRunner();
//    	return new SimpleHttpResponse(scriptRunner != null && scriptRunner.getStatus() == Status.RUNNING ? "1" : "0");
    }
    public HttpResponse getCurrentStep(final HttpRequest request){
        Session session = request.session();
        ScriptRunner scriptRunner = session.getScriptRunner();
        if (scriptRunner == null) return new SimpleHttpResponse("{'type':'WAIT'}");
        String derivedName = request.getParameter("derivedName");
        String windowName = request.getParameter("windowName");
        String windowTitle = request.getParameter("windowTitle");
        String domain = request.getParameter("domain");
        String wasOpened = request.getParameter("wasOpened");
        //System.out.println("scriptRunner="+scriptRunner);
        return new SimpleHttpResponse(scriptRunner.getStepJSON(derivedName, windowName, windowTitle, domain, wasOpened));
    }

    public void markStepDone(final HttpRequest request){
        Session session = request.session();
        ScriptRunner scriptRunner = session.getScriptRunner();
        String failureMessage = request.getParameter("failureMsg");
        String type = request.getParameter("type");
        scriptRunner.markStepDone(request.getParameter("stepId"), ResultType.getType(type), failureMessage);
        session.set204(false);
//        try{
//        	new TestReporter().logTestResult(request);
//        }catch(Exception e){
//            e.printStackTrace();
//        }
    }

    public HttpResponse check204(final HttpRequest request) {
        Session session = request.session();
        return new SimpleHttpResponse(""+session.is204());
    }
    
    public void markStepInProgress(final HttpRequest request){
        Session session = request.session();
        ScriptRunner scriptRunner = session.getScriptRunner();
        String type = request.getParameter("type");
        scriptRunner.markStepInProgress(request.getParameter("stepId"), ResultType.getType(type));
    }

    public HttpResponse currentScript(final HttpRequest request) {
        Session session = request.session();
        SahiScript script = getScript(session);
        if (script != null) {
            return new Script().view(script.getFilePath());
        } else {
            return new SimpleHttpResponse(
                    "No Script has been set for playback.");
        }
    }

	private SahiScript getScript(Session session) {
		RhinoScriptRunner scriptRunner = (RhinoScriptRunner) session.getScriptRunner();
		return scriptRunner.getScript();
	}

    public HttpResponse currentBrowserScript(final HttpRequest request) {
        Session session = request.session();
        HttpResponse httpResponse;
        if (session.getScriptRunner() != null && getScript(session) != null) {
            httpResponse = new SimpleHttpResponse(LogViewer.highlight(getScript(session).getBrowserJS(), -1));
        } else {
            httpResponse = new SimpleHttpResponse(
                    "No Script has been set for playback.");
        }
        return httpResponse;
    }

    public HttpResponse currentParsedScript(final HttpRequest request) {
        Session session = request.session();
        HttpResponse httpResponse;
        if (getScript(session) != null) {
            httpResponse = new SimpleHttpResponse("<pre>" + getScript(session).modifiedScript().replaceAll("\\\\r",
                    "").replaceAll("\\\\n", "<br>") + "</pre>");
        } else {
            httpResponse = new SimpleHttpResponse(
                    "No Script has been set for playback.");
        }
        return httpResponse;
    }

    public HttpResponse script(final HttpRequest request) {
        Session session = request.session();
        ScriptRunner scriptRunner = session.getScriptRunner();
		String s = null;
		if (scriptRunner != null) {
			if (scriptRunner.getScript() != null) {
				s = scriptRunner.getScript().getBrowserJS(); // Sahi Script
			} else {
				s = scriptRunner.getBrowserJS(); // Other drivers
			}
		} 
		if (s == null) s = "";
        return new NoCacheHttpResponse(s);
    }

    public HttpResponse auto2(final HttpRequest request) {
    	ProcessHelper.setProcessStarted();
        Session session = request.session();
        String fileName = request.getParameter("file");

        final String scriptFileWithPath;
        scriptFileWithPath = fileName;
        RhinoScriptRunner scriptRunner = new RhinoScriptRunner(new FileScript(scriptFileWithPath));
		session.setScriptRunner(scriptRunner);
		session.setIsPlaying(true);
        String startUrl = request.getParameter("startUrl");
        session.setIsWindowOpen(false);
        startPlayback(session, true, "0");
        return proxyAutoResponse(startUrl, session.id());
    }

    public HttpResponse auto(final HttpRequest request) {
    	ProcessHelper.setProcessStarted();
        Session session = request.session();
        String startUrl = request.getParameter("startUrl");
        session.setIsWindowOpen(false);
        session.setIsPlaying(true);
        session.setVariable("isSingleSession", request.getParameter("isSingleSession"));
        return proxyAutoResponse(startUrl, session.id());
    }

    public HttpResponse autoJava(final HttpRequest request) {
    	ProcessHelper.setProcessStarted();
        Session session = request.session();
        String startUrl = request.getParameter("startUrl");
        session.setIsWindowOpen(false);
        session.removeVariables(".*");
        session.setIsReadyForDriver(false); // will be toggled in Driver_initialized
        return proxyAutoResponse(startUrl, session.id());
    }

    public void setRetries(final HttpRequest request) {
    	ScriptRunner scriptRunner = request.session().getScriptRunner();
    	if (scriptRunner != null)
    	scriptRunner.setBrowserRetries(Integer.parseInt(request.getParameter("retries")));
    }

    public HttpResponse getRetries(final HttpRequest request) {
    	ScriptRunner scriptRunner = request.session().getScriptRunner();
    	return new SimpleHttpResponse(scriptRunner != null ? "" + scriptRunner.getBrowserRetries() : "-1");
    }

    public HttpResponse hasErrors(final HttpRequest request) {
    	ScriptRunner scriptRunner = request.session().getScriptRunner();
    	return new SimpleHttpResponse("" + scriptRunner.hasErrors());
    }
    
    public void xsuccess(final HttpRequest request) {
        Session session = request.session();
        session.touch();
        SessionState state = new SessionState();
        state.setVar("sahi_retries", "0", session);
        state.setVar("sahi_not_my_win_retries", "0", session);
    }

    private HttpResponse proxyAutoResponse(final String startUrl, final String sessionId) {
        Properties props = new Properties();
        props.setProperty("startUrl", startUrl);
        props.setProperty("sessionId", sessionId);
        return new NoCacheHttpResponse(new HttpFileResponse(Configuration.getHtdocsRoot() + "spr/auto.htm", props, false, true));
    }
}
