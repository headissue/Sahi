package com.sahi.playback;

import java.util.ArrayList;

public class ScriptFactory {
	public SahiScript getScript(String fileName, ArrayList parents) {
		if (fileName.indexOf("http://")==0 || fileName.indexOf("https://")==0) {
			return new URLScript(fileName, parents);
		}
		return new FileScript(fileName, parents);
	}
	public SahiScript getScript(String fileName) {
		return getScript(fileName, new ArrayList());
	}
}
