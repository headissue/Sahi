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
import java.util.Iterator;
import java.util.Map;
import net.sf.sahi.command.MockResponder;
import net.sf.sahi.playback.SahiScript;
import net.sf.sahi.record.Recorder;
import net.sf.sahi.report.Report;
import net.sf.sahi.test.SahiTestSuite;

/**
 * User: nraman Date: Jun 21, 2005 Time: 8:03:28 PM
 */
public class Session {

    private Status status;
    private static Map sessions = new HashMap();
    private String sessionId;
    private boolean isWindowOpen = false;
    private Recorder recorder;
    private SahiScript script;
    private Map variables;
    private MockResponder mockResponder = new MockResponder();
    private Report report;
    private long timestamp = System.currentTimeMillis();

    public Report getReport() {
        return report;
    }

    public void setReport(final Report report) {
        this.report = report;
    }

    public static void removeInstance(final String sessionId) {
        sessions.remove(sessionId);
    }

    public static Session getInstance(final String sessionId) {
        if (!sessions.containsKey(sessionId)) {
            sessions.put(sessionId, new Session(sessionId));
        }
        return (Session) sessions.get(sessionId);
    }

    public Session(final String sessionId) {
        this.sessionId = sessionId;
        this.variables = new HashMap();
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

    public Recorder getRecorder() {
        if (this.recorder == null) {
            this.recorder = new Recorder();
        }
        return recorder;
    }

    public boolean isRecording() {
        return getRecorder().isRecording();
    }

    public void setScript(final SahiScript script) {
        this.script = script;
    }

    public String getVariable(final String name) {
//    	System.out.println("get name="+name);
//    	System.out.println("get value="+(String) (variables.get(name)));
        return (String) (variables.get(name));
    }

    public void removeVariables(final String pattern) {
        for (Iterator iterator = variables.keySet().iterator(); iterator.hasNext();) {
            String s = (String) iterator.next();
            if (s.matches(pattern)) {
                iterator.remove();
            }
        }
    }

    public void setVariable(final String name, final String value) {
//    	System.out.println("set name="+name);
//    	System.out.println("set value="+value);
        variables.put(name, value);
    }

    public SahiScript getScript() {
        return script;
    }

    public SahiTestSuite getSuite() {
        return SahiTestSuite.getSuite(this.id());
    }

    public MockResponder mockResponder() {
        return mockResponder;
    }

    public boolean isPlayingBack() {
        return "1".equals(getVariable("sahi_play"));
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
}
