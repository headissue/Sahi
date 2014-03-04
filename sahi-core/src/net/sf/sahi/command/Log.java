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

import java.io.UnsupportedEncodingException;

import net.sf.sahi.config.Configuration;
import net.sf.sahi.playback.SahiScript;
import net.sf.sahi.playback.ScriptFactory;
import net.sf.sahi.report.LogViewer;
import net.sf.sahi.request.HttpRequest;
import net.sf.sahi.response.HttpFileResponse;
import net.sf.sahi.response.HttpResponse;
import net.sf.sahi.response.NoCacheHttpResponse;
import net.sf.sahi.response.SimpleHttpResponse;
import net.sf.sahi.util.FileNotFoundRuntimeException;
import net.sf.sahi.util.URLParser;
import net.sf.sahi.util.Utils;

public class Log {

    public HttpResponse viewLogs(final HttpRequest request) {
        String fileName = URLParser.logFileNamefromURI(request.uri());
        if ("".equals(fileName)) {
//        	long start = System.currentTimeMillis();
            String logsList = LogViewer.getLogsList(Configuration.getPlayBackLogsRoot());
//            System.out.println((System.currentTimeMillis() - start));
			NoCacheHttpResponse response = new NoCacheHttpResponse(logsList);
//            System.out.println((System.currentTimeMillis() - start));
			return response;
        } else {
            return new HttpFileResponse(fileName, null, false, false);
        }
    }

    public HttpResponse getBrowserScript(final HttpRequest request) {
        HttpResponse httpResponse;
        String scriptPath = request.getParameter("href");
        SahiScript script = new ScriptFactory().getScript(scriptPath);
		if (script != null) {
            httpResponse = new SimpleHttpResponse(LogViewer.highlight(script.getBrowserJSWithLineNumbers(), getLineNumber(request)));
        } else {
            httpResponse = new SimpleHttpResponse(
                    "No Script has been set for playback.");
        }
        return httpResponse;
    }

    public HttpResponse highlight(final HttpRequest request) {
        int lineNumber = getLineNumber(request);
        String href = request.getParameter("href");
        String content;
        if (href.startsWith("http://") || href.startsWith("https://")) {
            content = new String(Utils.readURL(href));
        } else {
        	try{
        		content = Utils.readFileAsString(Configuration.getAbsoluteUserPath(href));
        	}catch(FileNotFoundRuntimeException e){
        		content = "File ["+href+"] not found";
        	}
        }
        content = content.replaceAll("<", "&lt;").replaceAll(">", "&gt;");
        final SimpleHttpResponse response = new SimpleHttpResponse("");
        String highlighted = LogViewer.highlight(content, lineNumber);
        highlighted = ("<h4>" + href.replace("\\\\", "\\") + "</h4>").concat(highlighted);
		try {
			response.setData(highlighted.getBytes("UTF8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			response.setData(highlighted.getBytes());
		}
        response.resetRawHeaders();
        return response;
    }

    private int getLineNumber(final HttpRequest req) {
        String p = req.getParameter("n");
        int i = -1;
        try {
            i = Integer.parseInt(p);
        } catch (Exception e) {

        }
        return i;
    }
}
