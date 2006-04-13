package com.sahi.command;

import com.sahi.processor.SuiteProcessor;
import com.sahi.request.HttpRequest;
import com.sahi.response.HttpResponse;
import com.sahi.response.NoCacheHttpResponse;
import com.sahi.session.Session;
import com.sahi.test.SahiTestSuite;

public class Suite {
	
	public void start(HttpRequest request) {
		SuiteProcessor suiteProcessor = new SuiteProcessor();
		suiteProcessor.startSuite(request, request.session());
	}

	public HttpResponse status(HttpRequest request) {
		Session session = request.session();
		SahiTestSuite suite = SahiTestSuite.getSuite(session.id());
		String status = "NONE";
		if (suite != null) {
			status = session.getPlayBackStatus();
		}
		return new NoCacheHttpResponse(status);
	}
	
}
