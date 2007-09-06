/**
 * Sahi - Web Automation and Test Tool
 *
 * Copyright  2006  V Narayan Raman
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.sf.sahi.session;

import net.sf.sahi.command.MockResponder;
import net.sf.sahi.playback.SahiScript;
import net.sf.sahi.record.Recorder;
import net.sf.sahi.report.Report;
import net.sf.sahi.test.SahiTestSuite;

import java.util.*;

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

    public Report getReport() {
        return report;
    }

    public void setReport(Report report) {
        this.report = report;
    }

    public static Session getInstance(String sessionId) {
        if (!sessions.containsKey(sessionId)) {
            sessions.put(sessionId, new Session(sessionId));
        }
        return (Session) sessions.get(sessionId);
    }

    public Session(String sessionId) {
        this.sessionId = sessionId;
        this.variables = new HashMap();
        this.status = Status.INITIAL;
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
        if (this.recorder == null)
            this.recorder = new Recorder();
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

    public void removeVariables(String pattern) {
        for (Iterator iterator = variables.keySet().iterator(); iterator
                .hasNext();) {
            String s = (String) iterator.next();
            if (s.matches(pattern)) {
                iterator.remove();
            }
        }
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
}
