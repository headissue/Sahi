package net.sf.sahi.command;

import net.sf.sahi.config.Configuration;
import net.sf.sahi.request.HttpRequest;
import net.sf.sahi.response.HttpFileResponse;
import net.sf.sahi.response.HttpResponse;
import net.sf.sahi.response.SimpleHttpResponse;
import net.sf.sahi.util.Utils;

public class ConfigureUI {
	public HttpResponse execute(HttpRequest request) {
		return new HttpFileResponse(Configuration.getHtdocsRoot() + "/spr/manage/configureUI.htm");
	}
	
	public HttpResponse readFile(HttpRequest request) {
		final String fileName = request.getParameter("fileName");
		return new SimpleHttpResponse(Utils.readFile(Configuration.getAbsoluteUserPath(fileName)));
	}
	
	public void saveFile(HttpRequest request) {
		final String fileName = request.getParameter("fileName");
		final String contents = request.getParameter("contents");
		Utils.writeFile(contents, Configuration.getAbsoluteUserPath(fileName), true);
	}
	public HttpResponse view(HttpRequest request) {
		final String fileName = request.getParameter("fileName");
		final boolean useBase = "true".equals(request.getParameter("useBase"));
		final String path = useBase ?  Configuration.getAbsolutePath(fileName) : Configuration.getAbsoluteUserPath(fileName);
		return new SimpleHttpResponse(new String(Utils.readFile(path)).replace("\r\n", "\n").replace("\n", "<br/>"));
	}
}
