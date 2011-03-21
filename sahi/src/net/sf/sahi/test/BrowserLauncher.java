package net.sf.sahi.test;

import net.sf.sahi.config.Configuration;
import net.sf.sahi.util.ProxySwitcher;
import net.sf.sahi.util.Utils;

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


public class BrowserLauncher {

	private String browserProcessName;
	private String browser;
	private String browserOption;
	private ProcessHelper process;
	private final boolean useProxy;

	public BrowserLauncher(String browser, String browserProcessName,
			String browserOption, boolean useProxy) {
		this.browser = browser;
		this.browserProcessName = browserProcessName;
		this.browserOption = browserOption;
		this.useProxy = useProxy;
	}

	public ProcessHelper openURL(final String url) {
		if (useProxy) 
			ProxySwitcher.setSahiAsProxy();
		String cmd = buildCommand(url);
		//System.out.println(">>>> " + cmd);
		cmd = cmd.replaceAll("%20", " ").replaceAll("[&]", "__SahiAmpersandSahi__");
		cmd = cmd.replaceAll("[$]userDir", Configuration.getUserDataDir().replace('\\', '/'));
		cmd = cmd.replaceAll("[$]threadNo", "0"); // if this has not been substituted, change it to 0.
		cmd = Utils.expandSystemProperties(cmd);
		process = new ProcessHelper(cmd, browserProcessName);
		process.execute();
		return process;
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
				if (useProxy) 
					ProxySwitcher.revertSystemProxy();
				process.kill();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
