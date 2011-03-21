package net.sf.sahi.command;

import java.util.Properties;
import java.util.logging.Logger;

import net.sf.sahi.config.Configuration;
import net.sf.sahi.request.HttpRequest;
import net.sf.sahi.response.HttpFileResponse;
import net.sf.sahi.response.HttpModifiedResponse2;
import net.sf.sahi.response.HttpResponse;
import net.sf.sahi.response.SimpleHttpResponse;
import net.sf.sahi.rhino.ScriptRunner;
import net.sf.sahi.session.Session;
import net.sf.sahi.session.Status;
import net.sf.sahi.test.BrowserLauncher;
import net.sf.sahi.util.BrowserType;
import net.sf.sahi.util.BrowserTypesLoader;
import net.sf.sahi.util.ProxySwitcher;
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


public class Driver {
    private static Logger logger = Configuration.getLogger("net.sf.sahi.command.Driver");
    private Boolean useSystemProxy = false;
    
    
    public void setControllerMode(final HttpRequest request) {
    	String mode = request.getParameter("mode");
    	Configuration.setControllerMode(mode);
    }
    
    public void launchAndRecord(final HttpRequest request) {
    	launchBrowser(request);
    	record(request);
    }
    
    public void launchAndPlayback(final HttpRequest request) {
    	launchBrowser(request);
    }

	private void launchBrowser(final HttpRequest request) {
        String browser = request.getParameter("browser");
        String browserOption = request.getParameter("browserOption");
        String browserProcessName = request.getParameter("browserProcessName");
        launch(browser, browserProcessName, browserOption, "true".equals(request.getParameter("useSystemProxy")), request);
	}
	
	public void launchPreconfiguredBrowser(final HttpRequest request){
    	BrowserTypesLoader browserLoader = new BrowserTypesLoader();
    	BrowserType browserType = browserLoader.getBrowserType(request);
        
        // launches browser with pre configured browser settings
        if(browserType != null){
	        launch(browserType.path(), browserType.processName(), 
	        		browserType.options(), browserType.useSystemProxy(), request);
        }
	}
	
	private void launch(String browser, String browserProcessName, String browserOption, boolean useProxy, HttpRequest request){
		
    	Session session = request.session();
        
		String startUrl = request.getParameter("startUrl");
		if (startUrl == null) startUrl = "";
		
//		if (useProxy) {
//			this.useSystemProxy = true;
//			enableIEProxy(request);
//		}
//		
        final BrowserLauncher launcher = new BrowserLauncher(browser, browserProcessName, browserOption, useProxy);
		String url = "http://" + Configuration.getCommonDomain() + "/_s_/dyn/Driver_start?sahisid="
			+ session.id()
			+ "&startUrl="
			+ Utils.encode("http://" + Configuration.getCommonDomain() + "/_s_/dyn/Driver_initialized?startUrl="+Utils.encode(startUrl));

    	launcher.openURL(url);        
        session.setLauncher(launcher);
	}
	
    public void kill(final HttpRequest request) {
    	Session session = request.session();
    	BrowserLauncher launcher = session.getLauncher();
    	if (launcher != null) {
    		launcher.kill();
//    		if (useSystemProxy){
//    			disableIEProxy(request);
//    		}
    	}
    }    
    
    public HttpResponse start(final HttpRequest request){
		Session session = request.session();
        session.setScriptRunner(new ScriptRunner());    	
    	return new Player().autoJava(request);
    }    
    
    public void restart(final HttpRequest request) {
    	Session session = request.session();
        session.setScriptRunner(new ScriptRunner());    	
    	session.setIsPlaying(true);
		session.setIsReadyForDriver(true);
    }
    
    public HttpResponse initialized(final HttpRequest request) {
    	Session session = request.session();
    	session.setIsPlaying(true);
		session.setIsReadyForDriver(true);
		String startUrl = request.getParameter("startUrl");
		Properties properties = new Properties();
		if (startUrl == null) startUrl = "";
		properties.setProperty("startUrl", Utils.replaceLocalhostWithMachineName(startUrl));
    	HttpFileResponse httpFileResponse = new HttpFileResponse(Configuration.getHtdocsRoot() + "spr/initialized.htm", properties, false, true);
		HttpModifiedResponse2 response = new HttpModifiedResponse2(httpFileResponse, false, "htm");
//        response.addFilter(new ChunkedFilter());
		return response;
    }       

    public HttpResponse isReady(final HttpRequest request){
//		ScriptRunner scriptRunner = request.session().getScriptRunner();
//		boolean isReady = scriptRunner != null && scriptRunner.isRunning();
//		return new SimpleHttpResponse("" + isReady);		
		return new SimpleHttpResponse("" + request.session().isReadyForDriver());		
    }    
    
    public void setStep(final HttpRequest request){
    	String step = request.getParameter("step");
		setStep(request, step);
    }
    
	public void setBrowserJS(final HttpRequest request) {
		Session session = request.session();
		String browserJS = request.getParameter("browserJS");
		session.getScriptRunner().setBrowserJS(browserJS);
	}    
	

    
    public HttpResponse getVariable(final HttpRequest request){
    	String key = request.getParameter("key");
//    	System.out.println("key="+key);
		Session session = request.session();
		String val = session.getVariable(key);
//    	System.out.println("val="+val);
		return new SimpleHttpResponse(val != null ? val : "");
    }
    
    public HttpResponse doneStep(final HttpRequest request){
		Session session = request.session();
		ScriptRunner scriptRunner = session.getScriptRunner();
		if (scriptRunner == null){
			return new SimpleHttpResponse("error:Playback session not started. Verify that proxy is set on the browser.");
		}
		boolean done = scriptRunner.doneStep("")  || scriptRunner.isStopped();
		if (done){
			Status status = scriptRunner.getStatus();
			String browserException = scriptRunner.getBrowserException();
			if (browserException == null) browserException = "";
			if (status == Status.ERROR){
				return new SimpleHttpResponse("error:" +  browserException);
			} else if (status == Status.FAILURE) {
				return new SimpleHttpResponse("failure:" + browserException);				
			}
		}
		return new SimpleHttpResponse(""+done);
    }
    
    public SimpleHttpResponse getRecordedSteps(final HttpRequest request) {
    	return new StepWiseRecorder().getSteps(request);
    }
    
    public void setLastIdentifiedElement(final HttpRequest request) {
    	Session session = request.session();
		session.setVariable("__sahi__lastIdentifiedElement", request.getParameter("element"));
    }
    
    public SimpleHttpResponse getLastIdentifiedElement(final HttpRequest request) {
    	Session session = request.session();
		String val = session.getVariable("__sahi__lastIdentifiedElement");
		session.setVariable("__sahi__lastIdentifiedElement", null);
		return new SimpleHttpResponse(val == null ? "" : val);
	}
    
    public SimpleHttpResponse getAllRecordedSteps(final HttpRequest request) {
    	return new StepWiseRecorder().getAllSteps(request);
    }

    public void startRecording(final HttpRequest request) {
    	record(request);
		request.session().setIsWindowOpen(true);
		if (!"true".equals(request.getParameter("fromBrowser"))) 
			setStep(request, "_sahi.openController()");
    }

	private void record(final HttpRequest request) {
		Session session = request.session();
		session.setIsRecording(true);
    	new StepWiseRecorder().start(request);
		session.setIsPlaying(false);
	}

    public void stopRecording(final HttpRequest request) {
    	new StepWiseRecorder().stop(request);
    	Session session = request.session();
    	session.setIsWindowOpen(false);
		if (!"true".equals(request.getParameter("fromBrowser"))) 
			setStep(request, "_sahi.closeController()");
		session.setIsRecording(false);
		session.setIsPlaying(true);    	
    }
    
    public SimpleHttpResponse isRecording(final HttpRequest request) {
    	return new SimpleHttpResponse("" + request.session().isRecording());
    }
    
	private void setStep(final HttpRequest request, String step) {
		Session session = request.session();
		session.getScriptRunner().setStep(step, "");
	}    
	
	public void setSpeed(final HttpRequest request) {
		try { 
			String speed = request.getParameter("speed");
			logger.fine("Setting speed to " + speed);
			Configuration.setTimeBetweenSteps(Integer.parseInt(speed));
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public void toggleIEProxy(final boolean enable){
		String cmd = Configuration.getAbsolutePath("tools/toggle_IE_proxy.exe " + (enable ? "enable" : "disable"));
		String[] tokens = Utils.getCommandTokens(cmd.replaceAll("%20", " "));
		try {
			Utils.executeAndGetProcess(tokens);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}	
	
	public void enableIEProxy(final HttpRequest request){
		ProxySwitcher.setSahiAsProxy();
//		toggleIEProxy(true);
	}
	public void disableIEProxy(final HttpRequest request){
		ProxySwitcher.revertSystemProxy();
//		toggleIEProxy(false);
	}
}
