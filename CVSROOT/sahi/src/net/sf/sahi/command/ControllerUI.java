package com.sahi.command;

import com.sahi.playback.SahiScript;
import com.sahi.playback.ScriptUtil;
import com.sahi.request.HttpRequest;
import com.sahi.response.HttpResponse;
import com.sahi.response.NoCacheHttpResponse;
import com.sahi.session.Session;

public class ControllerUI {

	public void opened(HttpRequest request) {
		request.session().setIsWindowOpen(true);;
	}

	public void closed(HttpRequest request) {
		request.session().setIsWindowOpen(false);;
	}

	public HttpResponse getSahiScript(HttpRequest request) {
		String code = request.getParameter("code");
		return new NoCacheHttpResponse(SahiScript
				.modifyFunctionNames(code));
	}	

	public HttpResponse scriptsList(HttpRequest request) {
		return new NoCacheHttpResponse(ScriptUtil
				.getScriptsJs(getScriptName(request.session())));
	}	

	private String getScriptName(Session session) {
		SahiScript script = session.getScript();
		if (script == null)
			return "";
		return script.getScriptName();
	}	
}
