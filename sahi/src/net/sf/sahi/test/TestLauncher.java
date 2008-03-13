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

package net.sf.sahi.test;

import net.sf.sahi.config.Configuration;
import net.sf.sahi.util.Utils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.logging.Logger;

/**
 * @author nraman
 *         Launches browser with test and kills it on completion
 */
public class TestLauncher {
    private final String scriptName;
    private final String startURL;
    private String sessionId;
    private String childSessionId;
    private Process process;
    private String browser;
    private static Logger logger = Configuration.getLogger("net.sf.sahi.test.TestLauncher");
    private String browserOption;
    private int threadNo;

    public TestLauncher(String scriptName, String startURL) {
        this.scriptName = scriptName;
        this.startURL = startURL;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
        this.childSessionId = createChildSessionId();
    }

    public void setBrowser(String browser) {
        this.browser = browser;
    }

    public void setBrowserOption(String browserOption) {
        this.browserOption = browserOption;
    }

    private String createChildSessionId() {
        return sessionId + "sahix" + getRandomInt() + "x";
    }

    public String getStartURL() {
        return startURL;
    }

    public String getScriptName() {
        return scriptName;
    }

    public void execute() {
        System.out.println("#### Running Script: " + scriptName);
        String url = addSessionId(getURL());
        process = openURL(url);
    }

    private String getURL() {
        String cmd = null;
        try {
            cmd = "http://sahi.example.com/_s_/dyn/Player_auto?file="
                    + URLEncoder.encode(scriptName, "UTF8") + "&startUrl="
                    + URLEncoder.encode(startURL, "UTF8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return cmd;
    }

    private int getRandomInt() {
        return (int) (10000 * Math.random());
    }

    private String addSessionId(String url) {
        return url + "&sahisid=" + childSessionId;
    }

    private Process openURL(String url) {
        String cmd = buildCommand(url);
        logger.fine("cmd=" + cmd);
        Process process = null;
        try {
            process = Runtime.getRuntime().exec(cmd.replaceAll("%20", " "));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return process;
    }

    private String buildCommand(String url) {
        if (Utils.isWindows()) {
            return buildCommandForWindows(url);
        } else {
            return buildCommandForNonWindows(url);
        }
    }

    String buildCommandForWindows(String url) {
        String result;
        result = "\"" + browser + "\" ";
        if (!Utils.isBlankOrNull(browserOption))
            result += browserOption.replaceAll("[$]threadNo", ""+threadNo);
        result += " \"" + url + "\"";
        return result;
    }

    String buildCommandForNonWindows(String url) {
        String result;
        result = browser.replaceAll("[ ]+", "\\ ");
        if (!Utils.isBlankOrNull(browserOption)) {
            result += browserOption.replaceAll("[ ]+", "\\ ").replaceAll("[$]threadNo", ""+threadNo);
        }
        result += " " + url;
        return result;
    }

    public void stop() {
        System.out.println("Killing " + scriptName);
        logger.fine("Killing " + scriptName);
        try{
            if (process != null) {
                // if (isFirefox() && Utils.isWindows()) {
                // try {
                // Runtime.getRuntime().exec("taskkill /PID "+process.toString());
                // } catch (IOException e) {
                // e.printStackTrace();
                // }
                // }
                process.destroy();
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public String getChildSessionId() {
        return childSessionId;
    }

//	private boolean isFirefox() {
    // return browser.toLowerCase().indexOf("firefox") != -1;
    //	}

    public void setThreadNo(int threadNo) {
        this.threadNo = threadNo;
    }

    public int getThreadNo() {
        return threadNo;
    }
}
