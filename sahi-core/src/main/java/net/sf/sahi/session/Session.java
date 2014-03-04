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
package net.sf.sahi.session;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.sahi.command.MockResponder;
import net.sf.sahi.config.Configuration;
import net.sf.sahi.playback.RequestCredentials;
import net.sf.sahi.playback.SahiScript;
import net.sf.sahi.record.Recorder;
import net.sf.sahi.report.Report;
import net.sf.sahi.rhino.ScriptRunner;
import net.sf.sahi.test.BrowserLauncher;
import net.sf.sahi.test.SahiTestSuite;
import net.sf.sahi.util.Utils;

/**
 * User: nraman Date: Jun 21, 2005 Time: 8:03:28 PM
 */
public class Session {
	private static final Logger logger = Logger.getLogger("net.sf.sahi.session.Session");
	private final Hashtable<String,String> ajaxRedirects = new Hashtable<String, String>();
	
	private Status status;

	private static Map<String, Session> sessions = new HashMap<String, Session>();

	private String sessionId;

	private boolean isWindowOpen = false;

	private Recorder recorder;

	private SahiScript script;

	private Map<String, String> variables;

	private String xhrReadyStatesToWaitFor;
	
	private MockResponder mockResponder = new MockResponder();

	private Report report;

	private long timestamp = System.currentTimeMillis();

	private ScriptRunner scriptRunner;
	
	private boolean sendHTMLResponseAfterFileDownload = false;

	private Map<String, RequestCredentials> requestCredentials = new HashMap<String, RequestCredentials>();

	private BrowserLauncher launcher;

	private boolean isPlaying;

	private boolean isRecording;

	private boolean isReadyForDriver;

	private Map<String, Object> objectVariables = new HashMap<String, Object>();
	private boolean is204;
	static double playbackInactiveTimeout = Configuration.getMaxInactiveTimeForScript() * 1.5;
	static double recorderInactiveTimeout = 20 * 60 * 1000; // 20 minutes
	
	static {
		Timer stepTimer = new Timer();
		stepTimer.schedule(new TimerTask(){
			@Override
			public void run() {
				removeInactiveSessions();
			}
		}, 0, 10000);
	}

	public Report getReport() {
		return report;
	}

	public void setReport(final Report report) {
		this.report = report;
	}
	public static void removeInstance(final String sessionId) {
		sessions.remove(sessionId);
	}

	public static synchronized Session getInstance(final String sessionId) {
		if (!sessions.containsKey(sessionId)) {
			sessions.put(sessionId, new Session(sessionId));
		}
		Session session = sessions.get(sessionId);
		session.touch();
		return session;
	}
	
	public static synchronized Session getExistingInstance(final String sessionId) {
		return sessions.get(sessionId);
	}
	
	public Session(final String sessionId) {
		this.sessionId = sessionId;
		this.variables = new HashMap<String, String>();
		this.status = Status.INITIAL;
	}

	public String id() {
		return sessionId;
	}

	public void setIsWindowOpen(final boolean isWindowOpen) {
		this.isWindowOpen = isWindowOpen;
	}

	public boolean isWindowOpen() {
		return isWindowOpen;
	}

	public boolean sendHTMLResponseAfterFileDownload() {
		return this.sendHTMLResponseAfterFileDownload;
	}
	public void setSendHTMLResponseAfterFileDownload(boolean b) {
		this.sendHTMLResponseAfterFileDownload = b;
	}
	
	public Recorder getRecorder() {
		if (this.recorder == null) {
			this.recorder = new Recorder();
		}
		return recorder;
	}

	public String getVariable(final String name) {
		// System.out.println("get name="+name);
		// System.out.println("get value="+(String) (variables.get(name)));
		return (String) (variables.get(name));
	}

	public void removeVariables(final String pattern) {
		for (Iterator<String> iterator = variables.keySet().iterator(); iterator.hasNext();) {
			String s = iterator.next();
			if (s.matches(pattern)) {
				iterator.remove();
			}
		}
	}

	public void setVariable(final String name, final String value) {
		// System.out.println("set name="+name);
		// System.out.println("set value="+value);
		variables.put(name, value);
		if (scriptRunner != null)
			scriptRunner.setVariable(name, value);
	}

	public SahiScript xgetScript() {
		return script;
	}

	public SahiTestSuite getSuite() {
		return SahiTestSuite.getSuite(this.id());
	}

	public MockResponder mockResponder() {
		return mockResponder;
	}

	public boolean isPlaying() {
//		return scriptRunner != null;
		return this.isPlaying;
	}
	
	public void setIsPlaying(boolean isPlaying){
		this.isPlaying = isPlaying;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public void touch() {
		timestamp = System.currentTimeMillis();
	}

	public long lastActiveTime() {
		return timestamp;
	}

	public ScriptRunner getScriptRunner() {
		return scriptRunner;
	}

	public void setScriptRunner(ScriptRunner scriptRunner) {
		this.scriptRunner = scriptRunner;
		scriptRunner.setSession(this);
	}

	public void setLauncher(BrowserLauncher launcher) {
		this.launcher = launcher;
	}

	public BrowserLauncher getLauncher() {
		return launcher;
	}

	public void addRequestCredentials(String realm, String username, String password){
		realm = realm.trim();
		logger.info(">>> Credentials added: " + realm + " " + username);
		requestCredentials.put(realm, new RequestCredentials(realm, username, password));
	}
	
	public void removeRequestCredentials(String realm){
		requestCredentials.remove(realm);
	}
	
	public void removeAllRequestCredentials(){
		requestCredentials.clear();
	}
	
	public RequestCredentials getMatchingCredentials(String realm, String scheme){
		String key = Utils.isBlankOrNull(realm) ? scheme : realm;
		RequestCredentials cred = requestCredentials.get(key.trim());
		if (cred == null || cred.used()) return null;
		return cred;
	}

	public boolean isRecording() {
		return isRecording;
	}

	public void setIsRecording(boolean isRecording) {
		this.isRecording = isRecording;
	}

	public void setIsReadyForDriver(boolean isReadyForDriver) {
		this.isReadyForDriver = isReadyForDriver;
	}

	public boolean isReadyForDriver() {
		return isReadyForDriver;
	}
	
	public boolean isPaused() {
        return "1".equals(getVariable("sahi_paused"));
    }
    
	public String getInfoJSON() {
    	StringBuilder sb = new StringBuilder();
    	sb.append("{");
    	sb.append("'isRecording':"+isRecording());
    	sb.append(",");
    	sb.append("'isPlaying':"+isPlaying());
    	sb.append(",");
    	sb.append("'isPaused':"+isPaused());
    	sb.append(",");
    	sb.append("'sessionId':'"+sessionId+"'");
    	sb.append("}");
    	return sb.toString();	
    }
	
	public double getInactiveTimeout(){
		return isPlaying() ? playbackInactiveTimeout : recorderInactiveTimeout;
	}
	
	public static void removeInactiveSessions(){
		long timeNow = System.currentTimeMillis();
		try{
			for (Iterator<Session> iterator = sessions.values().iterator(); iterator.hasNext();) {
				Session s = (Session) iterator.next();
				if (timeNow - s.lastActiveTime() > s.getInactiveTimeout()){
					iterator.remove();
				}
			}
		}catch(Exception e){e.printStackTrace();}
		System.gc();		
		//System.out.println("sessions.size() = " + sessions.size());
	}

	public void setObject(String key, Object value) {
		this.objectVariables.put(key, value);
	}

	public Object getObject(String key) {
		return this.objectVariables.get(key);
	}

	public void addAjaxRedirect(String redirectedTo) {
		if (logger.isLoggable(Level.FINE)){
			logger.fine("Adding AJAX redirect for: " + redirectedTo);
		}
		ajaxRedirects.put(redirectedTo, redirectedTo);
	}

	public boolean isAjaxRedirect(String url) {
		boolean isRedirect = ajaxRedirects.containsKey(url);
		if (logger.isLoggable(Level.FINE)){
			logger.fine("AJAX redirect for: " + url + ": " + isRedirect);
		}
		if (isRedirect) ajaxRedirects.remove(url);
		return isRedirect;
	}

	public void set204(boolean is204) {
		this.is204 = is204;
		if (logger.isLoggable(Level.FINE)){
			logger.fine("Setting is204 = " + is204);
		}
	}

	public boolean is204() {
		if (logger.isLoggable(Level.FINE)){
			logger.fine("is204 = " + is204);
		}
		return is204;
	}
	
	public String getXHRReadyStatesToWaitFor() {
		return xhrReadyStatesToWaitFor;
	}
	
	public void setXHRReadyStatesToWaitFor(String states) {
		this.xhrReadyStatesToWaitFor = states;
	}

}
