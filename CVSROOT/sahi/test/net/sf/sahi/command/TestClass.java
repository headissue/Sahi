package com.sahi.command;

import com.sahi.request.HttpRequest;
import com.sahi.response.HttpResponse;

public class TestClass {
	public HttpResponse act(HttpRequest request) {
		CommandExecuterTest.called = true;
		return null;
	}
}
