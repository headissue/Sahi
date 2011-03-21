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

import java.util.logging.Logger;

import net.sf.sahi.playback.SahiScript;
import net.sf.sahi.request.HttpRequest;
import net.sf.sahi.response.HttpResponse;
import net.sf.sahi.response.NoCacheHttpResponse;
import net.sf.sahi.rhino.RhinoScriptRunner;
import net.sf.sahi.rhino.ScriptRunner;
import net.sf.sahi.session.Session;

public class RhinoRuntime {
	private static final Logger logger = Logger.getLogger("net.sf.sahi.command.RhinoRuntime");
	
    public HttpResponse eval(final HttpRequest request){
        Session session = request.session();
        String toEval = request.getParameter("toEval");
        ScriptRunner scriptRunner = session.getScriptRunner();
        String result = "null";
        if (scriptRunner instanceof RhinoScriptRunner){
	        RhinoScriptRunner rsr = (RhinoScriptRunner) session.getScriptRunner();
	        toEval = SahiScript.modifyFunctionNames(toEval);
			result = rsr.eval(toEval);
        } else {
        	logger.warning("Should not have come here: RhinoRuntime.eval: " + toEval);
        }
        return new NoCacheHttpResponse(result);
    }
}
