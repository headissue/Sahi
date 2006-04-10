package com.sahi;

import com.sahi.config.Configuration;

public class ProxyProcessorHelper {

	static String scriptFileNamefromURI(String uri, String token) {
		StringBuffer sb = new StringBuffer();
		sb.append(Configuration.getScriptRoot());
		int endIndex = uri.indexOf("?");
		endIndex = endIndex == -1 ? uri.length() :  endIndex;
		sb.append(uri.substring(uri.lastIndexOf(token) + token.length(), endIndex));
		return sb.toString();
	}

//	static int getLineNumber(String uri) {
//		int lineNumber = -1;
//		try {
//			String lineNumberStr = uri.substring(uri.lastIndexOf("#")+1);
//			lineNumber = Integer.parseInt(lineNumberStr);
//		}catch(Exception e) {}
//		return lineNumber;
//	}
	
	static String highlight(String s, int lineNumber) {
		s = s.replaceAll("<", "&lt;");
		s = s.replaceAll(">", "&gt;");
		int startIx = 0;
		int endIx = -1;
		int len = s.length();
		StringBuffer sb = new StringBuffer();
		for (int i=0; i<lineNumber; i++) {
			startIx = endIx+1;
			endIx = s.indexOf("\n", startIx);
			if (endIx == -1) break;
		}
		if (endIx==-1) endIx = len;
		sb.append(s.substring(0, startIx));
		sb.append("<b>");
		sb.append(s.substring(startIx, endIx));
		sb.append("</b>");
		sb.append(s.substring(endIx, len));		
		return sb.toString();
	}	
}
