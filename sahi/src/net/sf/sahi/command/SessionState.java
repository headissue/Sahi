/**
 * Copyright V Narayan Raman
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
import net.sf.sahi.request.HttpRequest;
import net.sf.sahi.response.HttpFileResponse;
import net.sf.sahi.response.HttpResponse;
import net.sf.sahi.response.NoCacheHttpResponse;
import net.sf.sahi.session.Session;

public class SessionState {

	public HttpResponse execute(HttpRequest request) {
		Session session = request.session();
		Properties props = new Properties();
		props.setProperty("sessionId", session.id());
		props.setProperty("isRecording", "" + session.isRecording());
		props.setProperty("isWindowOpen", "" + session.isWindowOpen());
		props.setProperty("isSahiPaused", "" + isPaused(session));
		props.setProperty("isSahiPlaying", ""+session.isPlayingBack());
		props.setProperty("isSahiRecording", ""+session.isRecording());
		props.setProperty("hotkey", "" + Configuration.getHotKey());

        props.setProperty("interval", "" + Configuration.getTimeBetweenSteps());
        props.setProperty("onErrorInterval", "" + Configuration.getTimeBetweenStepsOnError());
        props.setProperty("maxRetries", "" + Configuration.getMaxReAttemptsOnError());
        props.setProperty("maxWaitForLoad", "" + Configuration.getMaxCyclesForPageLoad());             

        NoCacheHttpResponse httpResponse = new NoCacheHttpResponse(
				new HttpFileResponse(Configuration.getHtdocsRoot()
						+ "spr/state.js", props, false, true));
		addSahisidCookie(httpResponse, session);
		return httpResponse;
	}



	private boolean isPaused(Session session) {
		return "1".equals(session.getVariable("sahi_paused"));
	}



	public void setVar(HttpRequest request) {
		Session session = request.session();
		String name = request.getParameter("name");
		String value = request.getParameter("value");
		Hits.increment("SessionState_setVar :: "+name);
		setVar(name, value, session);
	}

	public void setVar(String name, String value, Session session) {
		session.setVariable(name, value);
	}

	public HttpResponse getVar(HttpRequest request) {
		Session session = request.session();
		HttpResponse httpResponse;
		String name = request.getParameter("name");
		Hits.increment("SessionState_getVar :: "+name);
		String value = session.getVariable(name);
		httpResponse = new NoCacheHttpResponse(value != null
				? value
				: "null");
		return httpResponse;
	}



	private HttpResponse addSahisidCookie(HttpResponse httpResponse,
			Session session) {
		httpResponse.addHeader("Set-Cookie", "sahisid=" + session.id()
				+ "; path=/; ");
		// P3P: policyref="http://catalog.example.com/P3P/PolicyReferences.xml",
		// CP="NON DSP COR CURa ADMa DEVa CUSa TAIa OUR SAMa IND"
		httpResponse
				.addHeader(
						"P3P",
						"policyref=\"http://www.sahidomain.com/p3p.xml\", CP=\"NON DSP COR CURa ADMa DEVa CUSa TAIa OUR SAMa IND\"");
		httpResponse.resetRawHeaders();
		return httpResponse;
	}
}
