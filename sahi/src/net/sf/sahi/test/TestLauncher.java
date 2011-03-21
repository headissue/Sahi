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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.logging.Logger;

import net.sf.sahi.config.Configuration;
import net.sf.sahi.playback.ScriptFactory;
import net.sf.sahi.rhino.RhinoScriptRunner;
import net.sf.sahi.session.Session;
import net.sf.sahi.session.Status;
import net.sf.sahi.util.Utils;

/**
 * @author nraman Launches browser with test and kills it on completion
 */
public class TestLauncher {

	private final String scriptName;

	private final String startURL;

	private String sessionId;

	private String childSessionId;

	private String browser;

	private static Logger logger = Configuration
			.getLogger("net.sf.sahi.test.TestLauncher");

	private String browserOption;

	private int threadNo;

	@SuppressWarnings("unused")
	private boolean isMultiThreaded;

	private RhinoScriptRunner scriptRunner;

	private String browserProcessName;

	private BrowserLauncher browserLauncher;

	private boolean useSystemProxy;

	public TestLauncher(final String scriptName, final String startURL) {
		this.scriptName = scriptName;
		this.startURL = startURL;
	}

	public void setSessionId(final String sessionId) {
		this.sessionId = sessionId;
		this.childSessionId = createChildSessionId();
	}

	public void setBrowser(final String browser) {
		this.browser = browser;
	}

	public void setBrowserOption(final String browserOption) {
		this.browserOption = browserOption;
	}

	private String createChildSessionId() {
		return sessionId + "sahix" + Utils.getUUID() + "x";
	}

	public String getStartURL() {
		return startURL;
	}

	public String getScriptName() {
		return scriptName;
	}

	public void execute(Session session) {
		this.execute(session, true, true);
	}
	
	public void execute(Session session, boolean async, boolean setDefaultReporters) {
		System.out.println("#### Running Script: " + scriptName);
        scriptRunner = new RhinoScriptRunner(new ScriptFactory().getScript(scriptName), session.getSuite(), this, setDefaultReporters);
		session.setScriptRunner(scriptRunner);
        String url = addSessionId(getURL());
        browserOption = (browserOption == null) ? "" : browserOption.replaceAll("[$]threadNo", "" + threadNo)
        		.replaceAll("[$]userDir", Configuration.getAbsoluteUserPath(".").replace('\\', '/'));
		browserLauncher = new BrowserLauncher(browser, browserProcessName, browserOption, useSystemProxy);
		browserLauncher.openURL(url);
		if (async) scriptRunner.execute();
		else scriptRunner.executeAndWait(); 
	}

	private String getURL() {
		String cmd = null;
		try {
			cmd = "http://" + Configuration.getCommonDomain() + "/_s_/dyn/Player_auto?file="
					+ URLEncoder.encode(scriptName, "UTF8") + "&startUrl="
					+ URLEncoder.encode(startURL, "UTF8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return cmd;
	}

	private String addSessionId(String url) {
		return url + "&sahisid=" + childSessionId;
	}

	public void kill() {
		System.out.println("Killing " + scriptName);
		logger.fine("Killing " + scriptName);
		browserLauncher.kill();
	}

	public String getChildSessionId() {
		return childSessionId;
	}

	public void setThreadNo(int threadNo, boolean isMultiThreaded) {
		this.threadNo = threadNo;
		this.isMultiThreaded = isMultiThreaded;
	}

	public int getThreadNo() {
		return threadNo;
	}

	public Status getStatus() {
		return scriptRunner.getScriptStatus();
	}
	
	public RhinoScriptRunner getScriptRunner() {
		return scriptRunner;
	}

	public void setBrowserProcessName(String browserProcessName) {
		this.browserProcessName = browserProcessName;
	}
	public void setUseSystemProxy(boolean useSystemProxy) {
		this.useSystemProxy = useSystemProxy;
	}
}
