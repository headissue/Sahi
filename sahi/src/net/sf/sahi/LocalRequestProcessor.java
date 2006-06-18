package net.sf.sahi;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import net.sf.sahi.command.CommandExecuter;
import net.sf.sahi.command.Hits;
import net.sf.sahi.playback.log.LogFileConsolidator;
import net.sf.sahi.request.HttpRequest;
import net.sf.sahi.response.HttpFileResponse;
import net.sf.sahi.response.HttpResponse;
import net.sf.sahi.response.NoCacheHttpResponse;
import net.sf.sahi.response.SimpleHttpResponse;
import net.sf.sahi.util.URLParser;

public class LocalRequestProcessor {
	public HttpResponse getLocalResponse(String uri, HttpRequest requestFromBrowser) throws UnsupportedEncodingException, IOException {
		HttpResponse httpResponse = new NoCacheHttpResponse("");
		if (uri.indexOf("/dyn/") != -1) {
			String command = URLParser.getCommandFromUri(uri);
			Hits.increment(command);
			if (uri.indexOf("/stopserver") != -1) {
				System.exit(1);
			} else if (command != null) {
				httpResponse = new CommandExecuter(command, requestFromBrowser).execute();
			}

		} else if (uri.indexOf("/scripts/") != -1) {
			String fileName = URLParser.scriptFileNamefromURI(
					requestFromBrowser.uri(), "/scripts/");
			httpResponse = new HttpFileResponse(fileName, null, false, false);
		} else if (uri.indexOf("/logs/") != -1 || uri.endsWith("/logs")) {
			httpResponse = getLogResponse(URLParser.logFileNamefromURI(requestFromBrowser.uri()));
		} else if (uri.indexOf("/spr/") != -1) {
			String fileName = URLParser.fileNamefromURI(requestFromBrowser.uri());
			httpResponse = new HttpFileResponse(fileName, null, true, true);
		} else {
			httpResponse = new SimpleHttpResponse(
					"<html><h2>You have reached the Sahi proxy.</h2></html>");
		}
		return httpResponse;
	}



	private HttpResponse getLogResponse(String fileName) throws IOException {
		if ("".equals(fileName))
			return new NoCacheHttpResponse(getLogsList());
		else
			return new HttpFileResponse(fileName, null, false, false);
	}

	private String getLogsList() {
		return LogFileConsolidator.getLogsList();
	}





}
