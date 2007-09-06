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

package net.sf.sahi.command;

import java.util.Properties;

import net.sf.sahi.config.Configuration;
import net.sf.sahi.request.HttpRequest;
import net.sf.sahi.response.HttpFileResponse;
import net.sf.sahi.response.HttpResponse;
import net.sf.sahi.response.NoCacheHttpResponse;
import net.sf.sahi.session.Session;
import net.sf.sahi.util.Utils;

public class SessionState {

    public HttpResponse execute(HttpRequest request) {
        Session session = request.session();
        Properties props = new Properties();
        props.setProperty("sessionId", session.id());
        props.setProperty("isRecording", "" + session.isRecording());
        props.setProperty("isWindowOpen", "" + session.isWindowOpen());
        props.setProperty("isSahiPaused", "" + isPaused(session));
        props.setProperty("isSahiPlaying", ""+session.isPlayingBack());
        props.setProperty("isSahiRecording", ""+session.isRecording());
        props.setProperty("hotkey", "" + Configuration.getHotKey());

        props.setProperty("interval", "" + Configuration.getTimeBetweenSteps());
        props.setProperty("onErrorInterval", "" + Configuration.getTimeBetweenStepsOnError());
        props.setProperty("maxRetries", "" + Configuration.getMaxReAttemptsOnError());
        props.setProperty("maxWaitForLoad", "" + Configuration.getMaxCyclesForPageLoad());

        String waitCondition = session.getVariable("waitCondition");
        if (Utils.isBlankOrNull(waitCondition)) waitCondition = "";
        props.setProperty("waitCondition", "" + Utils.escapeDoubleQuotesAndBackSlashes(Utils.escapeDoubleQuotesAndBackSlashes(waitCondition)));
        String waitTime = session.getVariable("waitConditionTime");
        if (Utils.isBlankOrNull(waitTime)) waitTime = "-1";
        props.setProperty("waitConditionTime", "" + waitTime);

        NoCacheHttpResponse httpResponse = new NoCacheHttpResponse(
                new HttpFileResponse(Configuration.getHtdocsRoot()
                        + "spr/state.js", props, false, true));
        addSahisidCookie(httpResponse, session);
        return httpResponse;
    }



    private boolean isPaused(Session session) {
        return "1".equals(session.getVariable("sahi_paused"));
    }



    public void setVar(HttpRequest request) {
        Session session = request.session();
        String name = request.getParameter("name");
        String value = request.getParameter("value");
        Hits.increment("SessionState_setVar :: "+name);
        setVar(name, value, session);
    }

    public void setVar(String name, String value, Session session) {
        session.setVariable(name, value);
    }

    public HttpResponse getVar(HttpRequest request) {
        Session session = request.session();
        HttpResponse httpResponse;
        String name = request.getParameter("name");
        Hits.increment("SessionState_getVar :: "+name);
        String value = session.getVariable(name);
        httpResponse = new NoCacheHttpResponse(value != null
                ? value
                : "null");
        return httpResponse;
    }



    private HttpResponse addSahisidCookie(HttpResponse httpResponse,
            Session session) {
        httpResponse.addHeader("Set-Cookie", "sahisid=" + session.id()
                + "; path=/; ");
        // P3P: policyref="http://catalog.example.com/P3P/PolicyReferences.xml",
        // CP="NON DSP COR CURa ADMa DEVa CUSa TAIa OUR SAMa IND"
        httpResponse
                .addHeader(
                        "P3P",
                        "policyref=\"http://localproxy.sahi.co.in/p3p.xml\", CP=\"NON DSP COR CURa ADMa DEVa CUSa TAIa OUR SAMa IND\"");
        httpResponse.resetRawHeaders();
        return httpResponse;
    }
}
