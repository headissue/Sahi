package com.sahi.command;

import com.sahi.config.Configuration;
import com.sahi.request.HttpRequest;
import com.sahi.response.HttpResponse;
import com.sahi.response.NoCacheHttpResponse;
import com.sahi.session.Session;

public class Recorder {
	public void start(HttpRequest request) {
		startRecorder(request);
	}
	
	public void record(HttpRequest request) {
		request.session().getRecorder().record(request.getParameter("cmd"));
	}
	
	public void stop(HttpRequest request) {
		request.session().getRecorder().stop();
	}
	

	private void startRecorder(HttpRequest request) {
		Session session = request.session();
		String fileName = request.getParameter("file");
		session.getRecorder().start(Configuration.getScriptFileWithPath(fileName));
		session.setVariable("sahi_record", "1");
		// System.out.println("$$$$$$$$$$$ "+session.id());
	}	
}
