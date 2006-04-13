package net.sf.sahi.command;

import java.util.Properties;

import net.sf.sahi.config.Configuration;
import net.sf.sahi.request.HttpRequest;
import net.sf.sahi.response.HttpFileResponse;
import net.sf.sahi.response.HttpResponse;

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
