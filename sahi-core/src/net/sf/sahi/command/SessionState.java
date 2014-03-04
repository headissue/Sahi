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
package net.sf.sahi.command;

import java.util.Properties;

import net.sf.sahi.config.Configuration;
import net.sf.sahi.request.HttpRequest;
import net.sf.sahi.response.HttpFileResponse;
import net.sf.sahi.response.HttpResponse;
import net.sf.sahi.response.NoCacheHttpResponse;
import net.sf.sahi.response.SimpleHttpResponse;
import net.sf.sahi.rhino.ScriptRunner;
import net.sf.sahi.session.Session;
import net.sf.sahi.test.SahiTestSuite;
import net.sf.sahi.util.Utils;

public class SessionState {

    public HttpResponse ping(final HttpRequest request) {
    	return new SimpleHttpResponse(request.session().getInfoJSON());
    }
    
    public HttpResponse isPlaying(final HttpRequest request){
    	Session session = request.session();
        return new SimpleHttpResponse(session.isPlaying() ? "1" : "0");    	
    }   
    
    public HttpResponse domainfix(final HttpRequest request){
    	String domainFixInfo = Configuration.getDomainFixInfo();
		Properties props = new Properties();
		props.setProperty("domainInfo", domainFixInfo);
		return new HttpFileResponse(Configuration.getHtdocsRoot() + "spr/domainfix.js", props, false, true);
    }
    
    public HttpResponse isRecording(final HttpRequest request){
    	Session session = request.session();
        return new SimpleHttpResponse(session.isRecording() ? "1" : "0");    	
    }
    
	private String getXHRReadyStatesToWaitFor(Session session) {
		String states = session.getXHRReadyStatesToWaitFor();
		if (states == null) states = Configuration.xhrReadyStatesToWaitFor();
		return states;
	}
	
	public void setXHRReadyStatesToWaitFor(final HttpRequest request) {
		final String states = request.getParameter("states");
		System.out.println(states);
		request.session().setXHRReadyStatesToWaitFor(states);
	}
    
    public HttpResponse execute(final HttpRequest request) {
        Session session = request.session();
        Properties props = new Properties();
        props.setProperty("sessionId", session.id());
        props.setProperty("isWindowOpen", "" + session.isWindowOpen());
        props.setProperty("isSahiPaused", "" + session.isPaused());
        props.setProperty("isSahiPlaying", "" + session.isPlaying());
        props.setProperty("isSahiRecording", "" + session.isRecording());
        props.setProperty("hotkey", "" + Configuration.getHotKey());

        props.setProperty("interval", "" + Configuration.getTimeBetweenSteps());
        props.setProperty("onErrorInterval", "" + Configuration.getTimeBetweenStepsOnError());
        props.setProperty("maxRetries", "" + Configuration.getMaxReAttemptsOnError());
        props.setProperty("maxWaitForLoad", "" + Configuration.getMaxCyclesForPageLoad());
        props.setProperty("strictVisibilityCheck", "" + Configuration.isStrictVisibilityCheckEnabled());
        props.setProperty("waitReadyStates", getXHRReadyStatesToWaitFor(session));
        props.setProperty("controllerMode", "" + Configuration.getControllerMode());
        props.setProperty("escapeUnicode", "" + Configuration.getEscapeUnicode());
        props.setProperty("commonDomain", "" + Configuration.getCommonDomain());
        props.setProperty("chromeExplicitCheckboxRadioToggle", "" + Configuration.getChromeExplicitCheckboxRadioToggle());
        props.setProperty("ignorableIdsPattern", Configuration.getIgnorableIdsPattern());
        props.setProperty("strictVisibilityCheck", "" + session.getVariable("strictVisibilityCheck"));
        props.setProperty("isSingleSession",  "" + session.getVariable("isSingleSession"));

        String waitCondition = session.getVariable("waitCondition");
        if (Utils.isBlankOrNull(waitCondition)) {
            waitCondition = "";
        }
        props.setProperty("waitCondition", "" + Utils.escapeDoubleQuotesAndBackSlashes(waitCondition));
        String waitTime = session.getVariable("waitConditionTime");
        if (Utils.isBlankOrNull(waitTime)) {
            waitTime = "-1";
        }
        props.setProperty("waitConditionTime", "" + waitTime);
        props.setProperty("stabilityIndex", "" + Configuration.getStabilityIndex());
        ScriptRunner scriptRunner = session.getScriptRunner();
        if (scriptRunner != null && scriptRunner.getScript() != null){
			props.setProperty("scriptPath", Utils.escapeDoubleQuotesAndBackSlashes(Utils.escapeDoubleQuotesAndBackSlashes(scriptRunner.getScript().getFilePath())));
	        props.setProperty("scriptName", scriptRunner.getScriptName());
        }else{
			props.setProperty("scriptPath", "");
	        props.setProperty("scriptName", "");
        }

        NoCacheHttpResponse httpResponse = new NoCacheHttpResponse(
                new HttpFileResponse(Configuration.getHtdocsRoot() + "spr/state.js", props, false, true));
        addSahisidCookie(httpResponse, session);
        return httpResponse;
    }

    public void setVar(final HttpRequest request) {
        Session session = request.session();
        String name = request.getParameter("name");
        String value = request.getParameter("value");
        boolean append = "1".equals(request.getParameter("append"));
        Hits.increment("SessionState_setVar :: " + name);
        if (setVarOnSuite(request)){
        	SahiTestSuite suite = request.session().getSuite();
        	if (append) {
        		String val = suite.getVariable(name);
        		if (val != null) value = val + " + " + value;
        	}
			suite.setVariable(name, value);
        } else {
        	if (append) {
        		String val = session.getVariable(name);
        		if (val != null) value = val + " + " + value;
        	}
        	setVar(name, value, session);
    }
    }

	private boolean setVarOnSuite(final HttpRequest request) {
        boolean isGlobal = "1".equals(request.getParameter("isglobal"));
		return Configuration.spanVariablesAcrossSuite() && isGlobal && request.session().getSuite() != null;
	}

    public void setVar(final String name, final String value, final Session session) {
        session.setVariable(name, value);
    }

    public HttpResponse getVar(final HttpRequest request) {
        Session session = request.session();
        HttpResponse httpResponse;
        String name = request.getParameter("name");
        Hits.increment("SessionState_getVar :: " + name);
        boolean isDelete = "1".equals(request.getParameter("isdelete"));
        String value = null;
        if (setVarOnSuite(request)){
        	SahiTestSuite suite = request.session().getSuite();
			value = suite.getVariable(name);
        	if (isDelete) suite.setVariable(name, null);  
        } else{
        	value = session.getVariable(name);
        	if (isDelete) session.setVariable(name, null);
        }
        httpResponse = new NoCacheHttpResponse(value != null
                ? Utils.encode(value)
                : "null");
        return httpResponse;
    }

    private HttpResponse addSahisidCookie(final HttpResponse httpResponse,
            Session session) {
        httpResponse.addHeader("Set-Cookie", "sahisid=" + session.id() + "; path=/; ");
        // P3P: policyref="http://catalog.example.com/P3P/PolicyReferences.xml",
        // CP="NON DSP COR CURa ADMa DEVa CUSa TAIa OUR SAMa IND"
        httpResponse.addHeader(
                "P3P",
                "policyref=\"http://" + Configuration.getCommonDomain() + "/p3p.xml\", CP=\"NON DSP COR CURa ADMa DEVa CUSa TAIa OUR SAMa IND\"");
        httpResponse.resetRawHeaders();
        return httpResponse;
    }
    
    public void setCredentials(HttpRequest request){
        Session session = request.session();
        session.addRequestCredentials(request.getParameter("authRealm"), 
        		request.getParameter("authUser"), request.getParameter("authPassword"));
    }    
    
    public void removeAllCredentials(HttpRequest request){
        Session session = request.session();
        session.removeAllRequestCredentials();
    }
    
//    public HttpResponse getSessionInfo(final HttpRequest request) {
//    	Session session = request.session();
//    	StringBuffer sb = new StringBuffer();
//    	sb.append("{");
//    	ScriptRunner scriptRunner = session.getScriptRunner();
//    	if (scriptRunner != null){
//    		sb.append("script:{");
//    		sb.append("name: " + Utils.makeString(scriptRunner.getScriptName()));
//    		sb.append("path: " + Utils.makeString(scriptRunner.getScript().getFilePath()));
//    		sb.append("}");
//    		SahiTestSuite suite = session.getSuite();
//    		if (suite != null){
//    			sb.append(", suite:{");
//    			
//        		sb.append("}");
//    		}
//    	}
//    	sb.append("}");
//    	return new SimpleHttpResponse(sb.toString());
//    }
}
