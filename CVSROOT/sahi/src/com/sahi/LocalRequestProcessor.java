package com.sahi;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Properties;

import com.sahi.command.CommandExecuter;
import com.sahi.config.Configuration;
import com.sahi.playback.log.LogFileConsolidator;
import com.sahi.request.HttpRequest;
import com.sahi.response.HttpFileResponse;
import com.sahi.response.HttpResponse;
import com.sahi.response.NoCacheHttpResponse;
import com.sahi.response.SimpleHttpResponse;
import com.sahi.util.Utils;

public class LocalRequestProcessor {
	public HttpResponse getLocalResponse(String uri, HttpRequest requestFromBrowser) throws UnsupportedEncodingException, IOException {
		HttpResponse httpResponse = new NoCacheHttpResponse("");
		if (uri.indexOf("/dyn/") != -1) {
			String command = getCommandFromUri(uri);
			// System.out.println(uri);
//			System.out.println("----------- " + session.id() + " " + uri);
			if (uri.indexOf("/alert.htm") != -1) {
				String msg = requestFromBrowser.getParameter("msg");
				httpResponse = proxyAlertResponse(msg);
			} else if (uri.indexOf("/confirm.htm") != -1) {
				String msg = requestFromBrowser.getParameter("msg");
				httpResponse =  proxyConfirmResponse(msg);
			} else if (uri.indexOf("/stopserver") != -1) {
				System.exit(1);
			} else if (uri.indexOf("/setscripturl") != -1) {
				httpResponse = new CommandExecuter("Player_setScriptUrl", requestFromBrowser).execute();
			} else if (command != null) {
				httpResponse = new CommandExecuter(command, requestFromBrowser).execute();				
			}

		} else if (uri.indexOf("/scripts/") != -1) {
			String fileName = ProxyProcessorHelper.scriptFileNamefromURI(
					requestFromBrowser.uri(), "/scripts/");
			httpResponse = new HttpFileResponse(fileName);
		} else if (uri.indexOf("/logs/") != -1 || uri.endsWith("/logs")) {
			httpResponse = getLogResponse(logFileNamefromURI(requestFromBrowser.uri()));
		} else if (uri.indexOf("/spr/") != -1) {
			String fileName = fileNamefromURI(requestFromBrowser.uri());
			httpResponse = new HttpFileResponse(fileName);
		} else {
			httpResponse = new SimpleHttpResponse(
					"<html><h2>You have reached the Sahi proxy.</h2></html>");
		}
		return httpResponse;
	}

	String getCommandFromUri(String uri) {
		int ix1 = uri.indexOf("/dyn/");
		if (ix1 == -1) return null; 
		ix1 = ix1 + 5;
		int ix2 = uri.indexOf("/", ix1);
		int ix3 = uri.indexOf("?", ix1);
		int endIx = -1;
		if (ix2 > -1 && ix3 == -1) endIx = ix2;
		else if (ix3 > -1 && ix2 == -1) endIx = ix3;
		else {
			endIx = ix3 < ix2 ? ix3 : ix2;
		}
		if (endIx == -1) endIx = uri.length();
		return uri.substring(ix1, endIx);
	}

	private HttpResponse getLogResponse(String fileName) throws IOException {
		if ("".equals(fileName))
			return new NoCacheHttpResponse(getLogsList());
		else
			return new HttpFileResponse(fileName);
	}

	private String getLogsList() {
		return LogFileConsolidator.getLogsList();
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

	private String fileNamefromURI(String uri) {
		return Utils.concatPaths(Configuration.getHtdocsRoot(), uri
				.substring(uri.indexOf("_s_/") + 4));
	}


	private String logFileNamefromURI(String uri) {
		String fileName = uri.substring(uri.indexOf("/logs/") + 6);
		if ("".equals(fileName))
			return "";
		return appendLogsRoot(fileName);
	}

	private String appendLogsRoot(String fileName) {
		return Utils.concatPaths(Configuration.getPlayBackLogsRoot(), fileName);
	}




}
