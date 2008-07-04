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

import java.util.Properties;

import net.sf.sahi.config.Configuration;
import net.sf.sahi.request.HttpRequest;
import net.sf.sahi.response.HttpFileResponse;
import net.sf.sahi.response.HttpResponse;

public class ModalMocker {

    public HttpResponse alert(final HttpRequest requestFromBrowser) {
        return proxyAlertResponse(requestFromBrowser.getParameter("msg"));
    }

    public HttpResponse confirm(final HttpRequest requestFromBrowser) {
        return proxyConfirmResponse(requestFromBrowser.getParameter("msg"));
    }

    private HttpFileResponse proxyAlertResponse(final String msg) {
        Properties props = new Properties();
        props.setProperty("msg", msg);
        return new HttpFileResponse(Configuration.getHtdocsRoot() + "spr/alert.htm", props, false, true);
    }

    private HttpFileResponse proxyConfirmResponse(final String msg) {
        Properties props = new Properties();
        props.setProperty("msg", msg);
        return new HttpFileResponse(Configuration.getHtdocsRoot() + "spr/confirm.htm", props, false, true);
    }
}
