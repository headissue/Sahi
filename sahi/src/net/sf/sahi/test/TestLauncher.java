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

package net.sf.sahi.test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.logging.Logger;

import net.sf.sahi.config.Configuration;

/**
 * @author nraman
 * Launches browser with test and kills it on completion 
 */
class TestLauncher {
	private final String scriptName;
	private final String startURL;
	private int randomInt;
	private final String sessionId;
	Process process;
	private final String browser;
	private static Logger logger = Configuration.getLogger("net.sf.sahi.test.TestLauncher");
    private String browserOption;

    TestLauncher(String scriptName, String startURL, String browser, String sessionId, String browserOption) {
		this.scriptName = scriptName;
		this.startURL = startURL;
		this.browser = browser;
		this.sessionId = sessionId;
        this.browserOption=browserOption;
    }

	public void execute() {
		randomInt = getRandomInt();
		String url = addSessionId(getURL());
		process = openURL(url);
	}

	String getURL() {
		String cmd = null;
		try {
			cmd = "http://www.sahidomain.com/_s_/dyn/Player_auto?file="
					+ URLEncoder.encode(scriptName, "UTF8") + "&startUrl="
					+ URLEncoder.encode(startURL, "UTF8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		logger.fine("cmd=" + cmd);
		return cmd;
	}

	private int getRandomInt() {
		return (int) (10000 * Math.random());
	}

	private String addSessionId(String url) {
		return url + "&sahisid=" + sessionId + "sahix" + randomInt + "x";
	}

	private Process openURL(String url) {
		String cmd = escapeCommandStringForPlatform(url);
		logger.fine("cmd=" + cmd);
		Process process = null;
		try {
			process = Runtime.getRuntime().exec(cmd);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return process;
	}

	private String escapeCommandStringForPlatform(String url) {
		String result=null;
        if (isWindows())
        {
            result = "\"" + browser  + "\" ";
			if(!browserOption.equals(""))
				result += browserOption;
			result+=" \"" + url + "\"";
             System.out.println(result);
			return result;

        }
        else
        {
            result= browser.replaceAll("[ ]+", "\\ ");
             if(!browserOption.equals(""))
                result+=browserOption.replaceAll("[ ]+", "\\ ");
          result+=  " " + url.replaceAll("&", "\\&");
            return result;
        }   
    }

	private boolean isWindows() {
		return System.getProperty("os.name").toLowerCase().startsWith("windows");
	}

	public void stop() {
		logger.fine("Killing " + scriptName);
		if (process != null) {
			// if (isFirefox() && isWindows()) {
			// try {
			// Runtime.getRuntime().exec("taskkill /PID "+process.toString());
			// } catch (IOException e) {
			// e.printStackTrace();
			// }
			// }
			process.destroy();
		}
	}

//	private boolean isFirefox() {
//		return browser.toLowerCase().indexOf("firefox") != -1;
//	}
}
