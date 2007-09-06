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

import net.sf.sahi.report.LogViewer;
import net.sf.sahi.request.HttpRequest;
import net.sf.sahi.response.HttpFileResponse;
import net.sf.sahi.response.HttpResponse;
import net.sf.sahi.response.NoCacheHttpResponse;
import net.sf.sahi.response.SimpleHttpResponse;
import net.sf.sahi.util.URLParser;
import net.sf.sahi.util.Utils;
import net.sf.sahi.config.Configuration;

public class Log {

    public HttpResponse viewLogs(HttpRequest request) {
        String fileName = URLParser.logFileNamefromURI(request.uri());
        if ("".equals(fileName)) {
            return new NoCacheHttpResponse(LogViewer.getLogsList(Configuration.getPlayBackLogsRoot()));
        } else {
            return new HttpFileResponse(fileName, null, false, false);
        }
    }

    public HttpResponse highlight(HttpRequest request) {
        int lineNumber = getLineNumber(request);
        String href = request.getParameter("href");
        String content;
        if (href.startsWith("http://") || href.startsWith("https://")) {
            content = new String(Utils.readURL(href));
        } else {
            content = new String(Utils.readFile(href));
        }
        final SimpleHttpResponse response = new SimpleHttpResponse(content);
        response.setData(LogViewer.highlight(new String(response.data()), lineNumber).getBytes());
        response.resetRawHeaders();
        return response;
    }

    private int getLineNumber(HttpRequest req) {
        String p = req.getParameter("n");
        int i = -1;
        try {
            i = Integer.parseInt(p);
        } catch (Exception e) {
        }
        return i;
    }
}
