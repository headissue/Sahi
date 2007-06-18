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

package net.sf.sahi;

import net.sf.sahi.command.CommandExecuter;
import net.sf.sahi.command.Hits;
import net.sf.sahi.config.Configuration;
import net.sf.sahi.request.HttpRequest;
import net.sf.sahi.response.HttpFileResponse;
import net.sf.sahi.response.HttpResponse;
import net.sf.sahi.response.NoCacheHttpResponse;
import net.sf.sahi.util.URLParser;
import net.sf.sahi.report.LogViewer;

import java.io.IOException;

public class LocalRequestProcessor {
    public HttpResponse getLocalResponse(String uri, HttpRequest requestFromBrowser) throws IOException {
        HttpResponse httpResponse = new NoCacheHttpResponse("");
        if (uri.indexOf("/dyn/") != -1) {
            String command = URLParser.getCommandFromUri(uri);
            Hits.increment(command);
            if (uri.indexOf("/stopserver") != -1) {
                System.exit(1);
            } else if (command != null) {
                httpResponse = new CommandExecuter(command, requestFromBrowser).execute();
            }

        } else if (uri.indexOf("/scripts/") != -1) {
            String fileName = URLParser.scriptFileNamefromURI(
                    requestFromBrowser.uri(), "/scripts/");
            httpResponse = new HttpFileResponse(fileName, null, false, false);
        } else if (uri.indexOf("/spr/") != -1) {
            String fileName = URLParser.fileNamefromURI(requestFromBrowser.uri());
            httpResponse = new HttpFileResponse(fileName, null, true, true);
        }  else if (uri.indexOf("/logs") != -1) {
            httpResponse = new NoCacheHttpResponse(LogViewer.getLogsList(Configuration.getPlayBackLogsRoot()));
        } else {
            httpResponse = new HttpFileResponse(Configuration.getHtdocsRoot() + "/spr/launch.htm");
        }
        return httpResponse;
    }
}
