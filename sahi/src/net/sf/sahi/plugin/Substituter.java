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
package net.sf.sahi.plugin;

import net.sf.sahi.RemoteRequestProcessor;
import net.sf.sahi.request.HttpRequest;
import net.sf.sahi.response.HttpResponse;

public class Substituter {

    public HttpResponse replaceHttps(final HttpRequest request) {
        HttpResponse response = new RemoteRequestProcessor().processHttp(request);
        byte[] bytes = response.data();
        String data = new String(bytes);
        data = data.replaceAll("https", "http");
        response.setData(data.getBytes());
        return response;
    }

    public HttpResponse makeHTTPS(final HttpRequest request) {
        request.setSSL(true);
        return new RemoteRequestProcessor().processHttp(request);
    }
}
