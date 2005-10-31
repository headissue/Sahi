package com.sahi.processor;

import java.io.IOException;
import com.sahi.request.HttpRequest;
import com.sahi.session.Session;
import com.sahi.test.SahiTestSuite;

public class SuiteProcessor {
	public void startSuite(HttpRequest requestFromBrowser, Session session) throws IOException {
		String suiteName = requestFromBrowser.getParameter("suite");
		String browser = requestFromBrowser.getParameter("browser");
		String base = requestFromBrowser.getParameter("base");
		String threadsStr = requestFromBrowser.getParameter("threads");
		int threads = 1;
		try {
			threads = Integer.parseInt(threadsStr);
		}catch(Exception e) {}
		SahiTestSuite suite = new SahiTestSuite(suiteName, base, browser, session.id());
		for (int i = 0; i < threads; i++) {
			suite.executeNext();
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}	
}
