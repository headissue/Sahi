package com.sahi.util;

import com.sahi.config.Configuration;

public class URLParser {

	public static String logFileNamefromURI(String uri) {
		String fileName = uri.substring(uri.indexOf("/logs/") + 6);
		if ("".equals(fileName))
			return "";
		return Configuration.appendLogsRoot(fileName);
	}

	public static String fileNamefromURI(String uri) {
		return Utils.concatPaths(Configuration.getHtdocsRoot(), uri
				.substring(uri.indexOf("_s_/") + 4));
	}

	public static String scriptFileNamefromURI(String uri, String token) {
		StringBuffer sb = new StringBuffer();
		sb.append(Configuration.getScriptRoot());
		int endIndex = uri.indexOf("?");
		endIndex = endIndex == -1 ? uri.length() :  endIndex;
		sb.append(uri.substring(uri.lastIndexOf(token) + token.length(), endIndex));
		return sb.toString();
	}

}
