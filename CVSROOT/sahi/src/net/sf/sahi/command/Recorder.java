package net.sf.sahi.command;

import net.sf.sahi.config.Configuration;
import net.sf.sahi.request.HttpRequest;
import net.sf.sahi.response.HttpResponse;
import net.sf.sahi.response.NoCacheHttpResponse;
import net.sf.sahi.session.Session;

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
