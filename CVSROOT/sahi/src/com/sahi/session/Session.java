package com.sahi.session;

import java.util.HashMap;
import java.util.Map;

import com.sahi.config.Configuration;
import com.sahi.playback.SahiScript;
import com.sahi.playback.log.LogFileConsolidator;
import com.sahi.playback.log.PlayBackLogger;
import com.sahi.record.Recorder;
import com.sahi.test.SahiTestSuite;

/**
 * User: nraman Date: Jun 21, 2005 Time: 8:03:28 PM
 */
public class Session {
	private static Map sessions = new HashMap();
	private String sessionId;
	private boolean isWindowOpen;
	private Recorder recorder;
	private SahiScript script;
	private Map variables;
	private PlayBackLogger playBackLogger;
	private String scriptLogFile;

	public static Session getInstance(String sessionId) {
		if (!sessions.containsKey(sessionId)) {
			sessions.put(sessionId, new Session(sessionId));
		}
		return (Session) sessions.get(sessionId);
	}

	public Session(String sessionId) {
		this.sessionId = sessionId;
		this.variables = new HashMap();
	}

	public String id() {
		return sessionId;
	}

	public void setIsWindowOpen(boolean isWindowOpen) {
		this.isWindowOpen = isWindowOpen;
	}

	public boolean isWindowOpen() {
		return isWindowOpen;
	}

	public Recorder getRecorder() {
		if (this.recorder == null) this.recorder = new Recorder(Configuration.getScriptFormat());
		return recorder;
	}

	public boolean isRecording() {
		return getRecorder().isRecording();
	}

	public void setScript(SahiScript script) {
		this.script = script;
	}

	public String getVariable(String name) {
		return (String) (variables.get(name));
	}

	public void setVariable(String name, String value) {
		variables.put(name, value);
	}

	public SahiScript getScript() {
		return script;
	}

	public SahiTestSuite getSuite() {
		return SahiTestSuite.getSuite(this.id());
	}

	public void logPlayBack(String msg, String type, String debugInfo) {
		if (playBackLogger == null) {
			createPlayBackLogger();
		}
		playBackLogger.log(SahiScript.stripSahiFromFunctionNames(msg), type, debugInfo);
	}

	public void stopPlayBack() {
		playBackLogger.log("Stopping script", "stop", script.getScriptName());
		playBackLogger.stop();
		playBackLogger = null;
	}

	public void startPlayBack() {
		createPlayBackLogger();
		playBackLogger.log("Starting script", "start", script.getScriptName());
	}

	private void createPlayBackLogger() {
		playBackLogger = new PlayBackLogger(script.getScriptName(), getSuiteLogDir());
		scriptLogFile = playBackLogger.getScriptLogFile();
	}

	public String getSuiteLogDir() {
		if (getSuite() == null) return null; 
		return getSuite().getSuiteLogDir();
	}

	public String getPlayBackStatus() {
		if (getSuite().isRunning()) {
			return "RUNNING";
		}
		return new LogFileConsolidator(getSuiteLogDir()).getStatus();
	}

	public String getScriptLogFile() {
		return scriptLogFile;
	}
}
