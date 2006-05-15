package net.sf.sahi.command;

import net.sf.sahi.playback.SahiScript;
import net.sf.sahi.playback.ScriptUtil;
import net.sf.sahi.request.HttpRequest;
import net.sf.sahi.response.HttpResponse;
import net.sf.sahi.response.NoCacheHttpResponse;
import net.sf.sahi.session.Session;

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
