package net.sf.sahi.test;

import net.sf.sahi.config.Configuration;
import net.sf.sahi.util.BrowserType;
import net.sf.sahi.util.Utils;
import org.apache.log4j.Logger;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

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


public class BrowserLauncher {

  private String browserProcessName;
  private String browser;
  private String browserOption;
  private ProcessHelper process;
  private final boolean useProxy;
  private int maxTimeToWaitForPIDs = Configuration.getMaxTimeForPIDGather();
  private Logger logger = Logger.getLogger(BrowserLauncher.class);

  public BrowserLauncher(String browser, String browserProcessName,
                         String browserOption, boolean useProxy) {
    this.browser = browser;
    this.browserProcessName = browserProcessName;
    this.browserOption = browserOption;
    this.useProxy = useProxy;
  }

  public BrowserLauncher(BrowserType browserType) {
    this(browserType.path(), browserType.processName(), browserType.options(), browserType.useSystemProxy());
  }

  public ProcessHelper openURL(final String url) throws Exception {
    String cmd = buildCommand(url);
    logger.info("start browser: " + cmd);
    cmd = cmd.replaceAll("%20", " ").replaceAll("[&]", "__SahiAmpersandSahi__");
    cmd = cmd.replaceAll("[$]userDir", Configuration.getUserDataDir().replace('\\', '/'));
    cmd = cmd.replaceAll("[$]threadNo", "0"); // if this has not been substituted, change it to 0.
    cmd = Utils.expandSystemProperties(cmd);
    process = new ProcessHelper(cmd, browserProcessName, maxTimeToWaitForPIDs);
    process.execute();
    addShutDownHook();
    return process;
  }

  private void addShutDownHook() {
    ProcessExitDetector processExitDetector = new ProcessExitDetector(process.getActiveProcess());
    processExitDetector.start();
  }

  private String buildCommand(final String url) {
    if (Utils.isWindows()) {
      return buildCommandForWindows(url);
    } else {
      return buildCommandForNonWindows(url);
    }
  }

  String buildCommandForWindows(final String url) {
    String result;
    result = "\"" + browser + "\" ";
    if (!Utils.isBlankOrNull(browserOption)) {
      result += browserOption;
    }
    result += " \"" + url + "\"";
    return result;
  }

  String buildCommandForNonWindows(final String url) {
    String result;
    result = browser.replaceAll("[ ]+", "\\ ");
    if (!Utils.isBlankOrNull(browserOption)) {
      result += " " + browserOption.replaceAll("[ ]+", "\\ ");
    }
    result += " " + url;
    return result;
  }

  public void kill() {
    try {
      if (process != null) {
        process.kill();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  String getPlayerAutoURL(String childSessionId, String startURL, boolean isSingleSession) {
    String cmd = null;
    try {
      cmd = "http://" + Configuration.getCommonDomain() + "/_s_/dyn/Player_auto"
        + "?startUrl=" + URLEncoder.encode(startURL, "UTF8")
        + "&sahisid=" + childSessionId
        + "&isSingleSession=" + isSingleSession;
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }
    return cmd;
  }

  public void setMaxTimeToWaitForPIDs(int maxTimeToWaitForPIDs) {
    this.maxTimeToWaitForPIDs = maxTimeToWaitForPIDs;
  }

  public void waitTillAlive() {
    process.waitTillAlive();
  }

}
