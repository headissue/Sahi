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

import net.sf.sahi.RemoteRequestProcessor;
import net.sf.sahi.config.Configuration;
import net.sf.sahi.request.HttpRequest;
import net.sf.sahi.response.HttpFileResponse;
import net.sf.sahi.response.HttpModifiedResponse;
import net.sf.sahi.response.HttpResponse;
import net.sf.sahi.util.Utils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;

public class MockResponder {

    private HashMap map = new HashMap();

    public void add(String urlPattern, String className) {
        map.put(urlPattern, className);
    }

    public void remove(String urlPattern) {
        map.remove(urlPattern);
    }

    String getCommand(String url) {
        final Iterator iterator = map.keySet().iterator();
        while (iterator.hasNext()) {
            String pattern = (String) iterator.next();
            if (url.matches(pattern)) {
                return (String) map.get(pattern);
            }
        }
        return null;
    }

    public HttpResponse getResponse(HttpRequest request) {
        String url = request.url();
        final String command = getCommand(url);
        if (command == null)
            return null;
        System.out.println("url: " + url);
        System.out.println("command: " + command);
        return new CommandExecuter(command, request).execute();
    }

    public void remove(HttpRequest request) {
        request.session().mockResponder().remove(request.getParameter("pattern"));
    }

    public void add(HttpRequest request) {
        request.session().mockResponder().add(request.getParameter("pattern"), request.getParameter("class"));
    }

    public HttpResponse mockImage(HttpRequest request) throws IOException {
        return new HttpFileResponse(Utils.concatPaths(Configuration.getHtdocsRoot(), "spr/mock.gif"), null, true, true);
    }

    public HttpResponse simple(HttpRequest request) throws IOException {
        Properties props = new Properties();
        props.put("url", request.url());
        return new HttpModifiedResponse(new HttpFileResponse(Configuration.getHtdocsRoot() + "spr/simpleMock.htm", props, false, false), request
            .isSSL(), request.fileExtension());
    }

    public HttpResponse fileUpload(HttpRequest request) {
        return new RemoteRequestProcessor().processHttp(request);
    }
}
