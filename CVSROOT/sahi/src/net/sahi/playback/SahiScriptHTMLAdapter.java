package com.sahi.playback;

public class SahiScriptHTMLAdapter {
	public static String createHTML(String original) {
		return "<pre>"+original.replaceAll("\\\r", "").replaceAll("\\\n", "<br/>")+"</pre>";
	}


}
