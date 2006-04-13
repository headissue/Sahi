package com.sahi.command;

import java.util.Properties;

import com.sahi.config.Configuration;
import com.sahi.request.HttpRequest;
import com.sahi.response.HttpFileResponse;
import com.sahi.response.HttpResponse;
import com.sahi.response.NoCacheHttpResponse;
import com.sahi.session.Session;

public class SessionState {

	public HttpResponse execute(HttpRequest request) {
		Session session = request.session();
		Properties props = new Properties();
		props.setProperty("sessionId", session.id());
		props.setProperty("isRecording", "" + session.isRecording());
		props.setProperty("isWindowOpen", "" + session.isWindowOpen());
		props.setProperty("hotkey", "" + Configuration.getHotKey());
		NoCacheHttpResponse httpResponse = new NoCacheHttpResponse(
				new HttpFileResponse(Configuration.getHtdocsRoot()
						+ "spr/state.js", props));
		addSahisidCookie(httpResponse, session);
		return httpResponse;
	}



	public void setVar(HttpRequest request) {
		Session session = request.session();
		String name = request.getParameter("name");
		String value = request.getParameter("value");
		session.setVariable(name, value);
	}

	public HttpResponse getVar(HttpRequest request) {
		Session session = request.session();
		HttpResponse httpResponse;
		String name = request.getParameter("name");
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
