package com.sahi;

import com.sahi.config.Configuration;


public class ProxyProcessorHelper {

	public static String scriptFileNamefromURI(String uri, String token) {
		StringBuffer sb = new StringBuffer();
		sb.append(Configuration.getScriptRoot());
		int endIndex = uri.indexOf("?");
		endIndex = endIndex == -1 ? uri.length() :  endIndex;
		sb.append(uri.substring(uri.lastIndexOf(token) + token.length(), endIndex));
		return sb.toString();
	}	
}
