/**
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
