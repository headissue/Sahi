package net.sf.sahi.command;

import net.sf.sahi.playback.SahiScript;
import net.sf.sahi.report.Report;
import net.sf.sahi.request.HttpRequest;
import net.sf.sahi.session.Session;

/**
 * @author dlewis
 * 
 */
public class TestReporter {
	public void logTestResult(HttpRequest request) {
		Session session = request.session();
		Report report = session.getReport();
		report.addResult(SahiScript.stripSahiFromFunctionNames(request
				.getParameter("msg")), request.getParameter("type"), request
				.getParameter("debugInfo"),request
				.getParameter("failureMsg"));
	}
}
