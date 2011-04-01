package net.sf.sahi.client;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import net.sf.sahi.config.Configuration;
import net.sf.sahi.test.ProcessHelper;
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
/**
 * Browser is the main driver class for Sahi.<br/> 
 * It performs various actions on the browser and is also a factory for ElementStubs which are a representation
 * of various elements on the browser DOM.<br/>
 * A Browser instance is associated with a specific session on the Sahi proxy.
 * 
 * <pre>
Eg.

String sahiBasePath = "D:\\path\\to\\sahi_dir";
Sting userDataDirectory = "D:\\path\\to\\userdata_dir"; // userdata_dir is in sahiBasePath/userdata by default

net.sf.sahi.config.Configuration.initJava(sahiBasePath, userDataDirectory);

String browserName = "ie"; // default values are "ie", "firefox", "safari", "chrome", "opera" - specified in userdata/config/browser_types.xml 
Browser browser = new Browser(browserName);  		
browser.open();
browser.navigateTo("http://www.google.com");
browser.textbox("q").setValue("sahi forums");
browser.submit("Google Search").click();
browser.link("Sahi - Web Automation and Test Tool").click();		
browser.link("Login").click();
assertTrue(browser.textbox("req_username").exists());

browser.close();
 * 
 * 
 * </pre>
 * 
 */
public class Browser extends BrowserElements {

	private String sessionId = null;
	private boolean opened = false;
	private String popupName;
	private String host = "localhost";
	private int port = 9999;
	private String domainName;
	private String browserName;
	private String browserPath;
	private String browserOption;
	private String browserProcessName;

	/**
	 * Constructs a Browser object and associates it with a session on Sahi Proxy
	 * 
	 * @param browserName - Name of the browser as it is in browser_types.xml
	 *  
	 */
	public Browser(String browserName) {
		this(browserName, "localhost", Configuration.getPort());
	}
	
	/**
	 * Constructs a Browser object and associates it with a session on Sahi Proxy
	 * 
	 * @param browserPath The browser executable path
	 * @param browserProcessName The process name to look for to run a kill command
	 * @param browserOption Any browser options. Leave blank if not required.
	 *  
	 */
	public Browser(String browserPath, String browserProcessName, String browserOption) {
		this(browserPath, browserProcessName, browserOption, "localhost", Configuration.getPort());
	}
	
	/**
	 * Constructs a Browser object and associates it with a session on Sahi Proxy
	 * 
	 * @param browserPath The browser executable path
	 * @param browserProcessName The process name to look for to run a kill command
	 * @param browserOption Any browser options. Leave blank if not required.
	 *  
	 */
	public Browser(String browserPath, String browserProcessName, String browserOption, String host, int port) {
		this.host = host;
		this.port = port;
		this.browserPath = browserPath;
		this.browserOption = browserOption;
		this.browserProcessName = browserProcessName;
		sessionId = Utils.generateId();
		super.browser = this;		
	}
	
	public Browser(String browserName, String host, int port) {
		this.browserName = browserName;
		this.host = host;
		this.port = port;
		sessionId = Utils.generateId();
		super.browser = this;
	}
	
	public Browser(){
		super.browser = this;
	}
	
	private String getProxyURL(String command, QueryStringBuilder qs) {
		if (qs == null)
			qs = new QueryStringBuilder();
		qs.add("sahisid", sessionId);
		return "http://" + host + ":" + port + "/_s_/dyn/Driver_" + command + qs.toString();
	}
	

	/**
	 * Re-initializes proxy for playback if proxy is restarted.<br/>
	 * Should be invoked if proxy is started as a different process and 
	 * needs a restart. <br/>
	 * For NTLM/Windows authentication, java stores the first supplied valid <br/>
	 * credentials. To logout of such a system, the proxy needs to be restarted <br/>
	 * and then browser.restartPlayback() needs to be called. <br/>
	 *
	 */	
	public void restartPlayback() {
        String url = getProxyURL("restart", new QueryStringBuilder());
        Utils.readURL(url);
    }	
	
	private String execCommand(String command) {
		return execCommand(command, null);
	}

	private String execCommand(String command, QueryStringBuilder qs) {
		return new String(Utils.readURL(getProxyURL(command, qs)));
	}

	/**
	 * Navigates to the given URL
	 * 
	 * @param url
	 * @throws ExecutionException
	 */
	public void navigateTo(String url) throws ExecutionException {
		navigateTo(url, false);
	}

	/**
	 * Navigates to the given URL 
	 * 
	 * @param url
	 * @param forceReload boolean forces reload
	 * @throws ExecutionException
	 */
	public void navigateTo(String url, boolean forceReload) throws ExecutionException {
		executeStep("_sahi._navigateTo(\"" + url + "\", "+ forceReload +")");
		fetch("_sahi.loaded");
	}

	/**
	 * Executes any javascript on the browser.
	 * 
	 * @param step
	 * @throws ExecutionException
	 */
	public void execute(String step) throws ExecutionException {
		String prefix = "";
		if (isDomain()) prefix = "_sahi._domain(\""+ domainName.replaceAll("\"", "\\\"") + "\").";
		if (isPopup()) prefix += "_sahi._popup(\""+ popupName.replaceAll("\"", "\\\"") + "\").";
		step = prefix + step;
		executeStep(step);
	}

	private boolean isPopup() {
		return popupName != null;
	}

	private boolean isDomain() {
		return domainName != null;
	}

	public void executeStep(String step) throws ExecutionException {
		QueryStringBuilder qs = new QueryStringBuilder();
		// System.out.println("step=" + step);
		qs.add("step", step);
		execCommand("setStep", qs);
		int i = 0;
		while (i < 4000) {
			try {
				Thread.sleep(Configuration.getTimeBetweenSteps());
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
			i++;
			String checkDone = execCommand("doneStep");
			boolean done = "true".equals(checkDone);
//			System.out.println(checkDone);
			boolean error = checkDone.startsWith("error:");
			if (done)
				return;
			if (error){
//				System.out.println(checkDone);
				throw new ExecutionException(checkDone);
			}
		}
	}

//	public void reset() {
//		try {
//			navigateTo(getResetURL());
//		} catch (ExecutionException e) {
//			e.printStackTrace();
//		}
//	}
	
	/**
	 * Opens a browser instance and associates with a session on Sahi proxy.
	 */
	public void open() {
		if (opened) return;
		openURL();
		int i = 0;
		while (i < 500) {
			i++;
			String isReady = execCommand("isReady");
//			System.out.println(isReady);
			if ("true".equals(isReady)){
				opened = true;
				ProcessHelper.setProcessStarted();
				return;
			}
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		opened = true;
	}
	
	private void openURL(){
		QueryStringBuilder qs = new QueryStringBuilder();
		if(this.browserName != null){
			qs.add("browserType", this.browserName);
			qs.add("startUrl", "http://" + Configuration.getCommonDomain() + "/_s_/dyn/Driver_initialized");
			execCommand("launchPreconfiguredBrowser", qs);
		}
		else{
			qs.add("browser", this.browserPath);
			qs.add("browserOption", this.browserOption);
			qs.add("browserProcessName", this.browserProcessName);
			qs.add("startUrl", "http://" + Configuration.getCommonDomain() + "/_s_/dyn/Driver_initialized");
			execCommand("launchAndPlayback", qs);
		}
	}
	
	/*
	private String getInitialURL(){
		QueryStringBuilder qs = new QueryStringBuilder();
		qs.add("sahisid", this.sessionId);
		String url = "http://" + Configuration.getCommonDomain() + "/_s_/dyn/Driver_initialized" + qs.toString();
		return url;
	}*/

	/*private String getResetURL() {
		QueryStringBuilder qs = new QueryStringBuilder().add("startUrl", "http://" + Configuration.getCommonDomain() + "/_s_/dyn/Driver_initialized");
		qs.add("sahisid", this.sessionId);
		String url = "http://" + Configuration.getCommonDomain() + "/_s_/dyn/Driver_start" + qs.toString();
		return url;
	}*/

	void sleep(int millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Closes the browser instance.
	 * If called on a popup it works 
	 */
	public void close() {
		if (isPopup()){
			execute("_sahi._closeWindow()");
		}
		else kill();
	}

	/**
	 * Kills the browser instance
	 */
	public void kill() {
		execCommand("kill");
	}
	/**
	 * Sets the value in a form element
	 * 
	 * @param textbox
	 * @param value
	 * @throws ExecutionException
	 */
	public void setValue(ElementStub textbox, String value) throws ExecutionException {
		execute("_sahi._setValue(" + textbox + ", " + quoted(value) + ")");
	}

	/**
	 * Sets the file to be posted to the server via a file input field.
	 * This method instructs the proxy to inject the file contents directly in the request.
	 * The browser's input field will not be populated, though data will be submitted. 
	 * 
	 * @param elementStub
	 * @param value
	 * @throws ExecutionException
	 */
	public void setFile(ElementStub textbox, String value) throws ExecutionException {
		execute("_sahi._setFile(" + textbox + ", " + quoted(value) + ")");
	}
	
	/**
	 * Clicks the given element
	 * 
	 * @param element
	 * @throws ExecutionException
	 */
	public void click(ElementStub element) throws ExecutionException {
		execute("_sahi._click(" + element + ")");		
	}
	
	
	/**
	 * Double clicks the given element
	 * 
	 * @param element
	 * @throws ExecutionException
	 */
	public void doubleClick(ElementStub element) throws ExecutionException {
		execute("_sahi._doubleClick(" + element + ")");		
	}
	
	
	/**
	 * Right clicks the given element
	 * 
	 * @param element
	 * @throws ExecutionException
	 */
	public void rightClick(ElementStub element) throws ExecutionException {
		execute("_sahi._rightClick(" + element + ")");		
	}
	
	/**
	 * Checks the given checkbox or radio only if it is unchecked. 
	 * 
	 * @param element
	 * @throws ExecutionException
	 */
	public void check(ElementStub element) throws ExecutionException {
		execute("_sahi._check(" + element + ")");		
	}	
	
	/**
	 * Unchecks the given checkbox only if it is checked. 
	 * 
	 * @param element
	 * @throws ExecutionException
	 */
	public void uncheck(ElementStub element) throws ExecutionException {
		execute("_sahi._uncheck(" + element + ")");		
	}	
	
	/**
	 * Brings focus on the element. 
	 * 
	 * @param element
	 * @throws ExecutionException
	 */
	public void focus(ElementStub element) throws ExecutionException {
		execute("_sahi._focus(" + element + ")");		
	}	
	
	/**
	 * Removes focus from the element. 
	 * 
	 * @param element
	 * @throws ExecutionException
	 */
	public void removeFocus(ElementStub element) throws ExecutionException {
		execute("_sahi._removeFocus(" + element + ")");		
	}

	/**
	 * Simulates a mouse over on the given element
	 * 
	 * @param element
	 * @throws ExecutionException
	 */
	public void mouseOver(ElementStub element) throws ExecutionException {
		execute("_sahi._mouseOver(" + element + ")");		
	}

	/**
	 * Simulates a mouse down on the given element
	 * 
	 * @param element
	 * @throws ExecutionException
	 */
	public void mouseDown(ElementStub element) throws ExecutionException {
		execute("_sahi._mouseDown(" + element + ")");		
	}
	
	/**
	 * Simulates a mouse up on the given element
	 * 
	 * @param element
	 * @throws ExecutionException
	 */
	public void mouseUp(ElementStub element) throws ExecutionException {
		execute("_sahi._mouseUp(" + element + ")");		
	}


	/**
	 * Simulates a drag and drop event
	 * 
	 * @param dragElement Element to drag
	 * @param dropElement Element to drop on
	 * @throws ExecutionException
	 */
	public void dragDrop(ElementStub dragElement, ElementStub dropElement) throws ExecutionException {
		execute("_sahi._dragDrop(" + dragElement + ", " + dropElement + ")");	
	}
		
	
	/**
	 * Fetches the value of any DOM property
	 * 
	 * @param expression
	 * @return
	 * @throws ExecutionException
	 */
	public String fetch(String expression) throws ExecutionException {
		Date d = new Date();
		String key = "___lastValue___" + d.toString(); 
		execute("_sahi.setServerVarPlain('"+key+"', " + expression + ")");
		return execCommand("getVariable", new QueryStringBuilder().add("key", key));		
	}

	/**
	 * Fetches the string value of an element stub by performing an eval on the browser
	 * 
	 * @param element
	 * @return
	 * @throws ExecutionException
	 */
	public String fetch(ElementStub el) throws ExecutionException {
		return fetch(el.toString());
	}

	/**
	 * Returns the inner text of given element from the browser
	 * 
	 * @param el
	 * @return
	 * @throws ExecutionException
	 */
	public String getText(ElementStub el) throws ExecutionException {
		return fetch("_sahi._getText(" + el + ")");
	}

	/**
	 * Returns the value of given form element from the browser
	 * 
	 * @param el
	 * @return
	 * @throws ExecutionException
	 */	
	public String getValue(ElementStub el) throws ExecutionException {
		return fetch(el + ".value");
	}

	/**
	 * Returns true if the element exists on the browser
	 * Retries a few times if the return value is false. This can be controlled with script.max_reattempts_on_error in sahi.properties. 
	 * Use exists(el, true) to return in a single try.
	 * 
	 * @param elementStub
	 * @return
	 */
	public boolean exists(ElementStub el) {
		return exists(el, false);
	}
	/**
	 * Returns true if the element exists on the browser<br/>
	 * Retries a few times if optimistic is false. Retry count can be controlled with script.max_reattempts_on_error in sahi.properties. <br/>
	 * Use exists(el, true) to return in a single try.
	 *
	 * @param elementStub
	 * @param optimistic boolean. If true returns in a single try. If false, retries a few times. 
	 * @return
	 */
	public boolean exists(ElementStub el, boolean optimistic) {
		if (optimistic)
			return exists1(el);
		else {
			for (int i=0; i<Configuration.getMaxReAttemptsOnError(); i++){
				if (exists1(el)) return true;
			}
			return false;
		}
	}

	private boolean exists1(ElementStub el) {
		try {
			String fetched = fetch("_sahi._exists("+el+")");
			return ("true".equals(fetched));
		} catch (ExecutionException e) {
			return false;
		}
	}
	
	/**
	 * Returns true if the element is visible on the browser
	 * 
	 * @param elementStub
	 * @return
	 */
	public boolean isVisible(ElementStub el) throws ExecutionException {
		return isVisible(el, false);
	}
	
	private boolean isVisible1(ElementStub el) throws ExecutionException {
		return "true".equals(fetch("_sahi._isVisible(" + el + ")"));
	}
	
	/**
	 * Returns true if the element is visible on the browser<br/>
	 * Retries a few times if optimistic is false. Retry count can be controlled with script.max_reattempts_on_error in sahi.properties.<br/> 
	 * Use exists(el, true) to return in a single try.
	 *
	 * @param elementStub
	 * @param optimistic boolean. If true returns in a single try. If false, retries a few times. 
	 * @return
	 */
	public boolean isVisible(ElementStub el, boolean optimistic) throws ExecutionException {
		if (optimistic)
			return isVisible1(el);
		else {
			for (int i=0; i<Configuration.getMaxReAttemptsOnError(); i++){
				if (isVisible1(el)) return true;
			}
			return false;
		}
	}

	
	
	/**
	 * Returns the selected text visible in a select box (&lt;select&gt; tag)
	 * 
	 * @param selectElement
	 * @return
	 */
	public String getSelectedText(ElementStub el) throws ExecutionException {
		return fetch("_sahi._getSelectedText(" + el + ")");
	}

	/**
	 * Represents a popup window.
	 * The name is either the window name or the title of the window.
	 * 
	 * @param popupName
	 * @return
	 */
	public Browser popup(String popupName) {
		return copy(popupName, this.domainName);
	}

	private Browser copy(String popupName, String domainName) {
		Browser newWin = new Browser();
		newWin.host = host;
		newWin.port = port;
		newWin.sessionId = sessionId;
		newWin.popupName = popupName;
		newWin.domainName = domainName;
		return newWin;
	}

	/**
	 * Represents a portion of the page which is from a different domain.
	 * 
	 * @param domainName
	 * @return
	 */
	public Browser domain(String domainName) {
		return copy(this.popupName, domainName);
	}
	
	/**
	 * Waits till timeout milliseconds
	 * 
	 * @param timeout in milliseconds
	 */
	public void waitFor(long timeout){
		try {
			Thread.sleep(timeout);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Waits till the condition is satisfied or till timeout
	 * 
	 * @param condition BrowserCondition 
	 * @param timeout in milliseconds
	 * 
	 * <blockquote>
	   BrowserCondition condition = new BrowserCondition(browser){@Override
			public boolean test() throws ExecutionException {
				return "populated".equals(browser.textbox("t1").value());
			}};
		browser.waitFor(condition, 5000);
		</blockquote>
	 * 
	 * The above code will make the browser wait till the textbox's value becomes "populated".
	 * If it does not become "populated", the browser will wait for max 5000 ms 
	 * before moving to the next step 
	 * 
	 */
	public void waitFor(BrowserCondition condition, int timeout) {
		int total = 0;
		int interval = 500;
		while (total < timeout){
			waitFor(interval);
			total += interval;
			try {
				if (condition.test()) return;
			} catch (ExecutionException e) {
			} 
		}
		return;
	}

	/**
	 * Chooses the given option in a select box (&lt;select&gt; tag).
	 * 
	 * @param selectElement
	 * @param value
	 * @param append: if true, option is selected without unselecting previous option in multi-select box
	 * @throws ExecutionException
	 */
	public void choose(ElementStub elementStub, String value, boolean append) throws ExecutionException {
		execute("_sahi._setSelected(" + elementStub + ", " + quoted(value) + ", " + append + ")");
	}
	

	/**
	 * Chooses the given options in a multi select box (&lt;select&gt; tag).
	 * 
	 * @param selectElement
	 * @param values
	 * @param append: if true, options are selected without unselecting previous options in multi-select box
	 * @throws ExecutionException
	 */
	public void choose(ElementStub elementStub, String[] values, boolean append) throws ExecutionException {
		execute("_sahi._setSelected(" + elementStub + ", " + Utils.toJSON(values) + ", " + append + ")");
	}
	
	/**
	 * Starts recording.
	 * It tells the browser to record events and post the steps
	 * which will be available via getRecordedSteps()
	 */
	public void startRecording(){
		execCommand("startRecording");	
	}

	/**
	 * Stops recording.
	 * Tells the browser to stop monitoring events. 
	 */
	public void stopRecording(){
		execCommand("stopRecording");			
	}

	/**
	 * Gets the recorded steps from the last time getRecordedSteps() was called.
	 * @return String array of recorded steps
	 */
	public String[] getRecordedSteps(){
		return execCommand("getRecordedSteps").split("__xxSAHIDIVIDERxx__");			
	}

	/**
	 * Returns the last alert message from browser<br/>
	 * Alert messages are generated via window.alert(message) in javascript.
	 * 
	 * @return String the last alerted message
	 * @throws ExecutionException
	 */
	
	public String lastAlert() throws ExecutionException {
		return new ElementStub("lastAlert", this).fetch();
	}
	
	/**
	 * Returns the last confirm message from browser<br/>
	 * Confirm messages are generated via window.confirm(message) in javascript.
	 * 
	 * @return String the last confirm message
	 * @throws ExecutionException
	 */
	
	public String lastConfirm() throws ExecutionException {
		return new ElementStub("lastConfirm", this).fetch();
	}		
	
	/**
	 * Sets the input value of a prompt dialog with given message.<br/>
	 * This needs to be set before a prompt is expected.<br/>
	 * Prompts are generated in javascript via window.prompt(message)
	 * 
	 * @param message String visible message prompted by the browser
	 * @param input String input to enter in the prompt dialog
	 * @throws ExecutionException
	 */
	
	public void expectPrompt(String message, String input) throws ExecutionException {
		execute("_sahi._expectPrompt(" + quoted(message) + ", " + quoted(input) + ")");
	}		
	
	/**
	 * Sets the input value of a confirm dialog with given message.<br/>
	 * This needs to be set before a confirm is expected.<br/>
	 * Prompts are generated in javascript via window.confirm(message)
	 * 
	 * @param message String visible message prompted by the browser
	 * @param input boolean true to click on 'OK', false to click on 'Cancel'
	 * @throws ExecutionException
	 */
	
	public void expectConfirm(String message, boolean input) throws ExecutionException {
		execute("_sahi._expectConfirm(" + quoted(message) + ", " + input + ")");
	}		

	/**
	 * Returns the last prompt message from browser<br/>
	 * Alert messages are generated via window.prompt(message) in javascript.
	 * Use expectPrompt to set value to prompt.
	 * 
	 * @return String the last prompt message
	 * @throws ExecutionException
	 */	
	
	public String lastPrompt() throws ExecutionException {
		return new ElementStub("lastPrompt", this).fetch();
	}
	
	/**
	 * Clears the lastAlert message
	 * 
	 * 
	 * @throws ExecutionException
	 */
	public void clearLastAlert() throws ExecutionException {
		execute("_sahi._clearLastAlert()");
	}
	
	/**
	 * Clears the lastPrompt message
	 * 
	 * 
	 * @throws ExecutionException
	 */
	public void clearLastPrompt() throws ExecutionException {
		execute("_sahi._clearLastPrompt()");
	}
	
	/**
	 * Clears the lastConfirm message
	 * 
	 * 
	 * @throws ExecutionException
	 */
	public void clearLastConfirm() throws ExecutionException {
		execute("_sahi._clearLastConfirm()");
	}
	
	public String title() throws ExecutionException {
		return fetch("_sahi._title()");
	}
	
	private String quoted(String s){
		return "\"" + Utils.escapeDoubleQuotesAndBackSlashes(s).replaceAll("\n", "\\\\n").replaceAll("\r", "\\\\r") + "\"";
	}

	/**
	 * Returns true if the element is checked. Is meaningful only for radio buttons and checkboxes<br/>
	 * 
	 * @param el
	 * @return
	 * @throws ExecutionException
	 */	
	public boolean checked(ElementStub el) throws ExecutionException {
		return "true".equals(fetch(el + ".checked"));
	}
	
	/**
	 * Force Sahi to return a canned response for specific URL patterns
	 * The response is found in sahi/htdocs/spr/simpleMock.htm and can be modified
	 * 
	 * @param urlPattern a javascript regular expression as a string
	 */
	public void addURLMock(String urlPattern) {
		execute("_sahi._addMock(" + quoted(urlPattern) + ")");
	}
	
	/**
	 * Force Sahi to return a dynamic response returned by responseClass.methodName 
	 * 
	 * @param urlPattern a javascript regular expression as a string
	 * @param responseClass_method The class which will respond to matching requests
	 */
	public void addURLMock(String urlPattern, String responseClass_method) {
		execute("_sahi._addMock(" + quoted(urlPattern) + ", " + quoted(responseClass_method) + ")");
	}
	
	/**
	 * Removes any mocks associated with given pattern
	 * 
	 * @param urlPattern
	 */
	public void removeURLMock(String urlPattern) {
		execute("_sahi._removeMock(" + quoted(urlPattern) + ")");
	}

	/**
	 * Sahi automatically downloads files into sahi/userdata/temp/download directory.<br/>
	 * If a file was downloaded as a result of a click, its fileName will be accessible for assertion 
	 * 
	 * @return fileName of last downloaded file
	 */
	public String lastDownloadedFileName(){
		return new ElementStub("lastDownloadedFileName", this).fetch();
	}
	
	/**
	 * Resets the lastDownloadedFileName to null
	 */
	public void clearLastDownloadedFileName(){
		execute("_sahi._clearLastDownloadedFileName()");
	}
	
	/**
	 * Saves the last downloaded file to required location.<br/>
	 * Can be used to save file to some location and then verify contents by reading it.  
	 * 
	 * @param newFilePath
	 */
	public void saveDownloadedAs(String newFilePath){
		execute("_sahi._saveDownloadedAs(" + quoted(newFilePath) + ")");
	}
	
	/**
	 * Sets the speed of playback.<br/>
	 * Some applications do not trigger AJAX requests on response to events, but use a small delay before execution.<br/>
	 * It is useful to tweak this parameter (eg. increase to 200 milliseconds)
	 * The default is picked from "script.time_between_steps" in userdata.properties (or sahi.properties)
	 * 
	 * @param interval time in milliseconds
	 */
	public void setSpeed(int interval) {
		execCommand("setSpeed", new QueryStringBuilder().add("speed", ""+interval));
	}
	
	/**
	 * Sets strict visibility check.<br/>
	 * If true, Sahi APIs will ignore hidden elements.<br/>
	 * Useful when similar widgets are generated but only one widget is displayed at any time <br/>
	 * 
	 * Can set to true globally from sahi.properties/userdata.properties by setting <br/>
	 * element.visibility_check.strict=true<br/>
	 * 
	 * @param boolean
	 */
	public void setStrictVisibilityCheck(boolean check) {
		execute("_sahi._setStrictVisibilityCheck(" + check + ")");
	}
	/**
	 * Returns true if the element contains the input text
	 * 
	 * @param el
	 * @param text
	 * @return true if the element contains the input text
	 */
	public boolean containsText(ElementStub el, String text) {
		return "true".equals(fetch("_sahi._containsText(" + el + ", " + (quoted(text)) + ")"));
	}

//	private String quoteIfString(String text) {
//		return isRegExp(text) ? text : quoted(text);
//	}	
//	
//	private boolean isRegExp(String text) {
//		return text.charAt(0) == '/' && text.charAt(text.length()-1) == '/';
//	}

	/**
	 * Returns true if the element's innerHTML contains the input html
	 * 
	 * @param el
	 * @param html
	 * @return true if the element's innerHTML contains the input html
	 */
	public boolean containsHTML(ElementStub el, String html) {
		return "true".equals(fetch("_sahi._containsHTML(" + el + ", " + quoted(html) + ")"));
	}

	/**
	 * Returns the computed css style <br/>
	 * 
	 * @param el
	 * @param attribute
	 * @return the computed css style 
	 */
	public String style(ElementStub el, String attribute) {
		return fetch("_sahi._style(" + el + ", " + quoted(attribute) + ")");
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public String sessionId() {
		return sessionId;
	}

	/**
	 * Sets the value in a Rich Text Editor (RTE)
	 * 
	 * @param rte
	 * @param value
	 * @throws ExecutionException
	 */
	public void rteWrite(ElementStub rte, String value) {
		execute("_sahi._rteWrite(" + rte + ", " + quoted(value) + ")");		
	}	
	
	/**
	 * Allows given javascript to be injected into the browser.<br/>
	 * Custom functions or stubbed functions can be passed through this <br/>
	 * The given javascript will be available to every web page in this session <br/> 
	 * Equivalent to adding &lt;browser&gt; blocks in Sahi Script <br/>
	 * <br/>
	 * Eg.<br/>
	 * <code>
	 * browser.setBrowserJS("function myFinderFn(i){return document.links[i];}");<br/>
	 * // The above code defines a function myFinderFn, which will be available on each page of the application.<br/>
	 * 
	 * browser.setBrowserJS("function checkFileFieldIsPopulated(){return true}");<br/>
	 * // The above code will redefine checkFileFieldIsPopulated function so that it will always return true<br/>
	 * </code>
	 * @param browserJS
	 */
	public void setBrowserJS(String browserJS){
		QueryStringBuilder qs = new QueryStringBuilder();
		qs.add("browserJS", browserJS);
		execCommand("setBrowserJS", qs);	
	}

	/**
	 * Checks for Google Chrome browser
	 * @return
	 */
	public boolean isChrome() {
		return "true".equals(fetch("_sahi._isChrome()"));
	}
	
	/**
	 * Checks for Firefox browser
	 * @return
	 */
	public boolean isFirefox() {
		return "true".equals(fetch("_sahi._isFF()"));
	}
	
	/**
	 * Checks for Firefox browser
	 * @return
	 */
	public boolean isFF() {
		return "true".equals(fetch("_sahi._isFF()"));
	}
	
	/**
	 * Checks for Internet Explorer browser
	 * @return
	 */
	public boolean isIE() {
		return "true".equals(fetch("_sahi._isIE()"));
	}
	
	/**
	 * Checks for Safari browser
	 * @return
	 */
	public boolean isSafari() {
		return "true".equals(fetch("_sahi._isSafari()"));
	}
	
	/**
	 * Checks for Opera browser
	 * @return
	 */
	public boolean isOpera() {
		return "true".equals(fetch("_sahi._isOpera()"));
	}

	/**
	 * Returns a count of all matching elements <br/><br/>
	 * 
	 * Eg.<br/>
	 * 
	 * <pre>
	 * browser.count("div", "css-class-name")
	 * </pre>
	 * 
	 * @param args
	 * @return count of elements matching criteria
	 */
	
	public int count(Object... args) {
		final String countStr = new ElementStub("count", this, args).fetch();
		return Integer.parseInt(countStr);
	}

	public void keyDown(ElementStub element, int keyCode, int charCode) {
		execute("_sahi._keyDown(" + element + ", ["+ keyCode + ", " + charCode + "])");	
	}

	public void keyUp(ElementStub element, int keyCode, int charCode) {
		execute("_sahi._keyUp(" + element + ", ["+ keyCode + ", " + charCode + "])");	
	}
	
	
}

class QueryStringBuilder {
	Map<String, String> map = new LinkedHashMap<String, String>();

	public QueryStringBuilder add(String key, String value) {
		map.put(key, value);
		return this;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("?");
		boolean start = true;
		Set<String> keySet = map.keySet();
		for (Iterator<String> iterator = keySet.iterator(); iterator.hasNext();) {
			if (!start)
				sb.append("&");
			start = false;
			String key = iterator.next();
			String value = map.get(key);
			sb.append(key);
			sb.append("=");
			try {
				value = URLEncoder.encode(value, "utf-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			sb.append(value);
		}
		return sb.toString();
	}
}