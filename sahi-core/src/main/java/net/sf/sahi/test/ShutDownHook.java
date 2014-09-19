package net.sf.sahi.test;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.URL;

/**
 * Sahi - Web Automation and Test Tool
 * <p/>
 * Copyright  2006  V Narayan Raman
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
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
  private Logger logger = Logger.getLogger(ShutDownHook.class);

  public ShutDownHook(final String sahiHost, final String port, final String sessionId) {
    this.sahiHost = sahiHost;
    this.port = port;
    this.sessionId = sessionId;
  }

  public void run() {
    try {
      logger.info("Shutting down ...");
      String urlStr = "http://" + this.sahiHost + ":" + this.port + "/_s_/dyn/Suite_kill/?sahisid=" + sessionId;
      URL url = new URL(urlStr);
      url.getContent();
    } catch (IOException e) {
      System.err.println(e.getMessage());
    }
  }
}
