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

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;

import net.sf.sahi.RemoteRequestProcessor;
import net.sf.sahi.config.Configuration;
import net.sf.sahi.request.HttpRequest;
import net.sf.sahi.response.HttpFileResponse;
import net.sf.sahi.response.HttpModifiedResponse2;
import net.sf.sahi.response.HttpResponse;
import net.sf.sahi.util.Utils;

public class MockResponder {

    private HashMap<String, String> map = new HashMap<String, String>();

    public void add(final String urlPattern, final String className) {
        map.put(urlPattern, className);
    }

    public void remove(final String urlPattern) {
        map.remove(urlPattern);
    }

    String getCommand(final String url) {
        final Iterator<String> iterator = map.keySet().iterator();
        while (iterator.hasNext()) {
            String pattern = iterator.next();
            if (url.matches(pattern)) {
                return map.get(pattern);
            }
        }
        return null;
    }

    public HttpResponse getResponse(final HttpRequest request) throws Exception {
        String url = request.url();
        final String command = getCommand(url);
        if (command == null) {
            return null;
        }
        System.out.println("url: " + url);
        System.out.println("command: " + command);
        return new CommandExecuter(command, request, false).execute();
    }

    public void remove(final HttpRequest request) {
        request.session().mockResponder().remove(request.getParameter("pattern"));
    }

    public void add(final HttpRequest request) {
        request.session().mockResponder().add(request.getParameter("pattern"), request.getParameter("class"));
    }

    public HttpResponse mockImage(final HttpRequest request) throws IOException {
        return new HttpFileResponse(Utils.concatPaths(Configuration.getHtdocsRoot(), "spr/mock.gif"), null, true, true);
    }

    public HttpResponse simple(final HttpRequest request) throws IOException {
        Properties props = new Properties();
        props.put("url", request.url());
        HttpResponse mockResponse = new HttpFileResponse(Configuration.getHtdocsRoot() + "spr/simpleMock.htm", props, false, false);
        HttpResponse response = new HttpModifiedResponse2(mockResponse, request.isSSL(), request.fileExtension());
//		response.addFilter(new ChunkedFilter());
		return response;
    }

    public HttpResponse fileUpload(final HttpRequest request) {
        return new RemoteRequestProcessor().processHttp(request);
    }
}
