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
import net.sf.sahi.playback.SahiScript;
import net.sf.sahi.playback.ScriptUtil;
import net.sf.sahi.request.HttpRequest;
import net.sf.sahi.response.HttpResponse;
import net.sf.sahi.response.NoCacheHttpResponse;
import net.sf.sahi.response.SimpleHttpResponse;
import net.sf.sahi.rhino.RhinoScriptRunner;
import net.sf.sahi.session.Session;
import net.sf.sahi.util.Diagnostics;
import net.sf.sahi.util.Utils;

public class ControllerUI {
//    private static final Logger logger = Logger.getLogger("net.sf.sahi.command.ControllerUI");
    public void opened(final HttpRequest request) {
        request.session().setIsWindowOpen(true);
    }

    public void closed(final HttpRequest request) {
        request.session().setIsWindowOpen(false);
    }

    public HttpResponse getSahiScript(final HttpRequest request) {
        String code = request.getParameter("code");
        return new NoCacheHttpResponse(Utils.encode(SahiScript.modifyFunctionNames(code)));
    }

    public HttpResponse scriptsList(final HttpRequest request) {
        return new NoCacheHttpResponse(ScriptUtil.getScriptsJs(getScriptPath(request.session())));
    }

    public HttpResponse scriptDirsList(final HttpRequest request) {
        return new NoCacheHttpResponse(ScriptUtil.getScriptRootsJs(request.session().getRecorder().getDir()));
    }
    
	public HttpResponse scriptDirsListJSON(final HttpRequest request) {
		String[] fileList = Configuration.getScriptRoots();
		return new NoCacheHttpResponse(Utils.toJSON(fileList));
	}

	public HttpResponse scriptsListJSON(final HttpRequest request) {
		String dir = request.getParameter("dir");
		String[] fileList = ScriptUtil.getScriptFiles(dir);
		return new NoCacheHttpResponse(Utils.toJSON(fileList));
	}

    private String getScriptPath(final Session session) {
        RhinoScriptRunner scriptRunner = (RhinoScriptRunner) session.getScriptRunner();
        if (scriptRunner == null) return "";
        SahiScript script = scriptRunner.getScript();
		if (script == null) {
            return "";
        }
        return Utils.escapeDoubleQuotesAndBackSlashes(script.getFilePath());
    }
    
    public HttpResponse getOSInfo(final HttpRequest request){
    	StringBuffer sb = new StringBuffer();
    	sb.append("osname_$sahi$_:"+System.getProperty("os.name")+"_$sahi$_;");
    	sb.append("osversion_$sahi$_:"+System.getProperty("os.version")+"_$sahi$_;");
    	sb.append("osarch_$sahi$_:"+System.getProperty("os.arch")+"_$sahi$_;");
    	sb.append("istasklistavailable_$sahi$_:"+Diagnostics.TASKLIST_STATUS);
    	return new SimpleHttpResponse(sb.toString());
    }
    
    public HttpResponse getJavaInfo(final HttpRequest request){
    	StringBuffer sb = new StringBuffer();
    	sb.append("javadir_$sahi$_:"+System.getProperty("java.home")+"_$sahi$_;");
    	sb.append("javaversion_$sahi$_:"+System.getProperty("java.version")+"_$sahi$_;");
    	sb.append("iskeytoolavailable_$sahi$_:"+Configuration.isKeytoolFound());
    	return new SimpleHttpResponse(sb.toString());
    }
    
    public HttpResponse getChangeLog(final HttpRequest request){
    	String dataStr = new String(Utils.readFileAsString(Configuration.getChangeLogFilePath()));
    	dataStr = dataStr.replace("\r\n", "\n").replace("\r", "\n").replace("\n", "<br/>");
    	dataStr = dataStr.replace("\t", "&nbsp;&nbsp;&nbsp;&nbsp;");
		return new NoCacheHttpResponse(dataStr);
    }
    
    public HttpResponse getSahiVersion(final HttpRequest request){
    	return new SimpleHttpResponse(Configuration.getVersion());
    }
}
