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

import net.sf.sahi.request.HttpRequest;
import net.sf.sahi.response.HttpResponse;
import net.sf.sahi.response.SimpleHttpResponse;
import net.sf.sahi.session.Session;
import net.sf.sahi.util.Utils;

public class Log {

    public HttpResponse highlight(HttpRequest request) {
        int lineNumber = getLineNumber(request);
        String href = request.getParameter("href");
        String content;
        if (href.startsWith("http://") || href.startsWith("https://")){
            content = new String(Utils.readURL(href));
        }else{
//            String fileName = Utils.concatPaths(Configuration.getScriptRoots(), href);
            content = new String(Utils.readFile(href));
        }
        final SimpleHttpResponse response = new SimpleHttpResponse(content);
        if (lineNumber != -1) {
            response
                    .setData(("<html><body><style>b{color:brown}</style><pre>"
                            + Log.highlight(new String(
                                    response.data()), lineNumber) + "</pre></body></html>")
                            .getBytes());
        }
        response.addHeader("Content-type", "text/html");
        response.resetRawHeaders();
        return response;
    }


    public void execute(HttpRequest request) {
        Session session = request.session();
        if (session.getScript() != null) {
            session.logPlayBack(request.getParameter("msg"),
                    request.getParameter("type"),
                    request.getParameter("debugInfo"));
        }
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

    public static String highlight(String s, int lineNumber) {
        s = s.replaceAll("<", "&lt;");
        s = s.replaceAll(">", "&gt;");
        int startIx = 0;
        int endIx = -1;
        int len = s.length();
        StringBuffer sb = new StringBuffer();
        for (int i=0; i<lineNumber; i++) {
            startIx = endIx+1;
            endIx = s.indexOf("\n", startIx);
            if (endIx == -1) break;
        }
        if (endIx==-1) endIx = len;
        sb.append(s.substring(0, startIx));
        sb.append("<b>");
        sb.append(s.substring(startIx, endIx).replace('\r', ' '));
        sb.append("</b>");
        sb.append(s.substring(endIx, len));
        return sb.toString();
    }

}
