package net.sf.sahi.test;

import java.io.IOException;
import java.net.URL;

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


public class ShutDownHook implements Runnable {

    private String sahiHost;
    private String port;
    private String sessionId;

    public ShutDownHook(final String sahiHost, final String port, final String sessionId) {
        this.sahiHost = sahiHost;
        this.port = port;
        this.sessionId = sessionId;
    }

    public void run() {
        try {
            System.out.println("Shutting down ...");
            String urlStr = "http://" + this.sahiHost + ":" + this.port + "/_s_/dyn/Suite_kill/?sahisid=" + sessionId;
            URL url = new URL(urlStr);
            url.getContent();
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }
}
