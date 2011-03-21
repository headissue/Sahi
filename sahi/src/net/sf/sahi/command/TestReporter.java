/**
 * @author dlewis
 */

package net.sf.sahi.command;

import net.sf.sahi.playback.SahiScript;
import net.sf.sahi.report.Report;
import net.sf.sahi.request.HttpRequest;
import net.sf.sahi.rhino.ScriptRunner;
import net.sf.sahi.session.Session;


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


public class TestReporter {
    public void logTestResult(final HttpRequest request) {
        Session session = request.session();
        ScriptRunner scriptRunner = session.getScriptRunner();
        if (scriptRunner == null) return;
		Report report = scriptRunner.getReport();
        if (report != null) {
            report.addResult(SahiScript.stripSahiFromFunctionNames(request.getParameter("msg")), request.getParameter("type"), request.getParameter("debugInfo"), request.getParameter("failureMsg"));
        }
    }
}
