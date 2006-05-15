package net.sf.sahi.command;

import net.sf.sahi.request.HttpRequest;
import net.sf.sahi.response.HttpResponse;

public class TestClass {
	public HttpResponse act(HttpRequest request) {
		CommandExecuterTest.called = true;
		return null;
	}
}
