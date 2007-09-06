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
