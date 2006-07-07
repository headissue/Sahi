package net.sf.sahi.command;

import net.sf.sahi.playback.SahiScript;
import net.sf.sahi.playback.ScriptUtil;
import net.sf.sahi.request.HttpRequest;
import net.sf.sahi.response.HttpResponse;
import net.sf.sahi.response.NoCacheHttpResponse;
import net.sf.sahi.session.Session;
import net.sf.sahi.util.Utils;

public class ControllerUI {

    public void opened(HttpRequest request) {
        request.session().setIsWindowOpen(true);
    }

    public void closed(HttpRequest request) {
        request.session().setIsWindowOpen(false);
    }

    public HttpResponse getSahiScript(HttpRequest request) {
        String code = request.getParameter("code");
        return new NoCacheHttpResponse(SahiScript
                .modifyFunctionNames(code));
    }

    public HttpResponse scriptsList(HttpRequest request) {
        return new NoCacheHttpResponse(ScriptUtil
                .getScriptsJs(getScriptPath(request.session())));
    }

    public HttpResponse scriptDirsList(HttpRequest request) {
        return new NoCacheHttpResponse(ScriptUtil
                .getScriptRootsJs(request.session().getRecorder().getDir()));
    }

    private String getScriptPath(Session session) {
        SahiScript script = session.getScript();
        if (script == null)
            return "";
        return Utils.escapeDoubleQuotesAndBackSlashes(script.getFilePath());
    }
}
