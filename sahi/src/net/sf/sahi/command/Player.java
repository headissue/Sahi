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

import net.sf.sahi.config.Configuration;
import net.sf.sahi.playback.FileScript;
import net.sf.sahi.playback.SahiScript;
import net.sf.sahi.playback.ScriptFactory;
import net.sf.sahi.report.HtmlReporter;
import net.sf.sahi.report.Report;
import net.sf.sahi.request.HttpRequest;
import net.sf.sahi.response.HttpFileResponse;
import net.sf.sahi.response.HttpResponse;
import net.sf.sahi.response.NoCacheHttpResponse;
import net.sf.sahi.response.SimpleHttpResponse;
import net.sf.sahi.session.Session;
import net.sf.sahi.session.Status;
import net.sf.sahi.test.SahiTestSuite;

import java.util.Properties;

public class Player {
	public void stepWisePlay(HttpRequest request) {
		startPlayback(request.session(), false);
	}

	public void start(HttpRequest request) {
		startPlayback(request.session(), true);
	}

	public void stop(HttpRequest request) {
		Session session = request.session();
		stop(session);
	}

	public void stop(Session session) {
        try{
	    	session.getRecorder().stop();
	        session.getReport().generateTestReport();
	        Status testStatus = session.getReport().getTestSummary().hasFailed() ? Status.FAILURE : Status.SUCCESS;
	        session.setStatus(testStatus);
        }catch(Exception e){
        	e.printStackTrace();
	        session.setStatus(Status.FAILURE);
        }
        SahiTestSuite suite = SahiTestSuite.getSuite(session.id());
        if (suite != null) {
            suite.notifyComplete(session.id());
        }
    }

	public void setScriptFile(HttpRequest request) {
		Session session = request.session();
		String fileName = request.getParameter("file");
		SahiScript script = new ScriptFactory().getScript(fileName);
		session.setScript(script);
		// session.setScript(new ScriptFactory().getScript(
		// Configuration.getScriptFileWithPath(fileName)));
		session
				.setReport(new Report(script.getScriptName(),
						new HtmlReporter()));
		startPlayback(session, true);
	}

	public void setScriptUrl(HttpRequest request) {
		Session session = request.session();
		String url = request.getParameter("url");
		session.setScript(new ScriptFactory().getScript(url));
		session.setReport(new Report(session.getScript().getScriptName(),
				new HtmlReporter()));
		startPlayback(session, true);
	}

	private void startPlayback(Session session, boolean resetConditions) {
		if (resetConditions)
			session.removeVariables(".*");
		session.setStatus(Status.RUNNING);
		session.setVariable("sahi_play", "1");
		session.setVariable("sahi_paused", "1");
	}

	public HttpResponse currentScript(HttpRequest request) {
		Session session = request.session();
		SahiScript script = session.getScript();
		if (script != null) {
			return new Script().view(script.getFilePath());
		} else {
			return new SimpleHttpResponse(
					"No Script has been set for playback.");
		}
	}

	public HttpResponse currentParsedScript(HttpRequest request) {
		Session session = request.session();
		HttpResponse httpResponse;
		if (session.getScript() != null) {
			httpResponse = new SimpleHttpResponse("<pre>"
					+ session.getScript().modifiedScript().replaceAll("\\\\r",
							"").replaceAll("\\\\n", "<br>") + "</pre>");
		} else {
			httpResponse = new SimpleHttpResponse(
					"No Script has been set for playback.");
		}
		return httpResponse;
	}

	public HttpResponse script(HttpRequest request) {
		Session session = request.session();
		String s = (session.getScript() != null) ? session.getScript()
				.modifiedScript() : "";
		return new NoCacheHttpResponse(s);
	}

	public HttpResponse auto(HttpRequest request) {
		Session session = request.session();
		String fileName = request.getParameter("file");

		final String scriptFileWithPath;
		scriptFileWithPath = fileName;
		session.setScript(new FileScript(scriptFileWithPath));
		String startUrl = request.getParameter("startUrl");
		session.setIsWindowOpen(false);

		if (session.getSuite() != null)
			session.setReport(new Report(session.getScript().getScriptName(),
					session.getSuite().getListReporter()));
		else
			session.setReport(new Report(session.getScript().getScriptName(),
					new HtmlReporter()));

		return proxyAutoResponse(startUrl, session.id());
	}

	public void success(HttpRequest request) {
		Session session = request.session();
		SessionState state = new SessionState();
		state.setVar("sahi_retries", "0", session);
		state.setVar("sahi_not_my_win_retries", "0", session);
	}

	private HttpFileResponse proxyAutoResponse(String startUrl, String sessionId) {
		Properties props = new Properties();
		props.setProperty("startUrl", startUrl);
		props.setProperty("sessionId", sessionId);
		return new HttpFileResponse(Configuration.getHtdocsRoot()
				+ "spr/auto.htm", props, false, true);
	}
}
