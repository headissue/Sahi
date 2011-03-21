package net.sf.sahi.command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import net.sf.sahi.request.HttpRequest;
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


public class StepWiseRecorder {
	
	// This will cause a leak. Fix this by setting property in session itself.
	static HashMap<String,RecordedSteps> recorders = new HashMap<String, RecordedSteps>(); 
	
    public void start(final HttpRequest request) {
    	RecordedSteps recordedSteps = recorders.get(request.session().id());
    	if (recordedSteps != null) return;
    	recordedSteps = new RecordedSteps();
		Session session = request.session();
		recorders.put(session.id(), recordedSteps);
    }
    
    public void record(final HttpRequest request) {
    	RecordedSteps recordedSteps = recorders.get(request.session().id());
    	String step = request.getParameter("step");
    	if (recordedSteps == null || !request.session().isRecording()){
//    		System.out.println("Recording not started, but step received was: " + step);
    		return;
    	}
//    	System.out.println(step);
		recordedSteps.record(step);
    }

    public SimpleHttpResponse getSteps(final HttpRequest request) {
    	RecordedSteps recordedSteps = recorders.get(request.session().id());
    	if (recordedSteps == null) return new SimpleHttpResponse("");
    	return new SimpleHttpResponse(recordedSteps.getNewStepsAsString());
    }
    
    public SimpleHttpResponse getAllSteps(final HttpRequest request) {
    	RecordedSteps recordedSteps = recorders.get(request.session().id());
    	if (recordedSteps == null) return new SimpleHttpResponse("");
    	return new SimpleHttpResponse(recordedSteps.getAllStepsAsString());
    }
    
    public void clear(final HttpRequest request) {
    	RecordedSteps recordedSteps = recorders.get(request.session().id());
    	if (recordedSteps != null) recordedSteps.clear();
    }
    
    public void stop(final HttpRequest request) {
    	Session session = request.session();
//    	session.setVariable("sahi_record", "0");
    	session.setIsRecording(false);
//		recorders.remove(session.id());
    }    
}

class RecordedSteps{
	private Queue<String> newSteps = new ConcurrentLinkedQueue<String>();
	private List<String> allSteps = new ArrayList<String>();

	public String getNewStepsAsString() {
		StringBuilder sb = new StringBuilder();
		while (!newSteps.isEmpty()) {
			String step = newSteps.poll();
			sb.append(step + "__xxSAHIDIVIDERxx__");
		}
		return sb.toString();
	}

	public void clear() {
		allSteps.clear();
	}

	public void record(String step) {
		newSteps.offer(step);
		allSteps.add(step);
	}

	public String getAllStepsAsString() {
		StringBuilder sb = new StringBuilder();
		for (String step : allSteps) {
			sb.append(step + "__xxSAHIDIVIDERxx__");
		}
		return sb.toString();
	}
}