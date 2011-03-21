package net.sf.sahi.command;

import net.sf.sahi.playback.SahiScript;
import net.sf.sahi.request.HttpRequest;
import net.sf.sahi.response.HttpResponse;
import net.sf.sahi.response.SimpleHttpResponse;
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

public class Messages {
	public void setMessageForController(HttpRequest request){
		Session session = request.session();
		session.setVariable("CONTROLLER_MessageForController", request.getParameter("message"));
	}
	public HttpResponse getMessageForController(HttpRequest request){
		Session session = request.session();
		return new SimpleHttpResponse(session.getVariable("CONTROLLER_MessageForController"));
	}
	
	public void setMessageForBrowser(HttpRequest request){
		Session session = request.session();
		String windowName = request.getParameter("windowName");
		String message = request.getParameter("message");
		message = SahiScript.modifyFunctionNames(message);
		session.setVariable("CONTROLLER_MessageForBrowser" + "_" + windowName, message);
	}
	public HttpResponse getMessageForBrowser(HttpRequest request){
		Session session = request.session();
		String windowName = request.getParameter("windowName");
		String key = "CONTROLLER_MessageForBrowser" + "_" + windowName;
		String message = session.getVariable(key);
		session.setVariable(key, null);
		return new SimpleHttpResponse("" + message);
	}
}
