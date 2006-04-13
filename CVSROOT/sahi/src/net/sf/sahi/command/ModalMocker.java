package com.sahi.command;

import java.util.Properties;

import com.sahi.config.Configuration;
import com.sahi.request.HttpRequest;
import com.sahi.response.HttpFileResponse;
import com.sahi.response.HttpResponse;

public class ModalMocker {

	public HttpResponse alert(HttpRequest requestFromBrowser) {
		return proxyAlertResponse(requestFromBrowser.getParameter("msg"));
	}

	public HttpResponse confirm(HttpRequest requestFromBrowser) {
		return proxyConfirmResponse(requestFromBrowser.getParameter("msg"));
	}	

	private HttpFileResponse proxyAlertResponse(String msg) {
		Properties props = new Properties();
		props.setProperty("msg", msg);
		return new HttpFileResponse(Configuration.getHtdocsRoot()
				+ "spr/alert.htm", props);
	}

	private HttpFileResponse proxyConfirmResponse(String msg) {
		Properties props = new Properties();
		props.setProperty("msg", msg);
		return new HttpFileResponse(Configuration.getHtdocsRoot()
				+ "spr/confirm.htm", props);
	}	
}
