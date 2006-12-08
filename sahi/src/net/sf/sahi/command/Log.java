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
