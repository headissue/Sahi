/**
 * Sahi - Web Automation and Test Tool
 *
 * Copyright  2006  V Narayan Raman
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
