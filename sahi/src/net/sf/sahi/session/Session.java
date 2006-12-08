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

    private List listReport = new ArrayList();

    private Report report;

    public Report getReport() {
        return report;
    }

    public void setReport(Report report) {
        this.report = report;
    }

    public List getListReport() {
        return listReport;
    }

    public void addReport(Report report) {
        this.listReport.add(report);
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

//    public String getPlayBackStatus() {
//        if (getSuite().isRunning()) {
//            return STATE_RUNNING;
//        }
//        return new LogFileConsolidator(getSuiteLogDir()).getStatus();
//    }

    public MockResponder mockResponder() {
        return mockResponder;
    }

    public boolean isPlayingBack() {
        return "1".equals(getVariable("sahi_play"));
        //return Status.RUNNING.equals(status);
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}
