package com.sahi.playback;

import java.io.File;

import com.sahi.config.Configuration;

public class ScriptUtil {
	public static String getScriptsJs(String scriptName) {
		String[] fileList = getFileNames();
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < fileList.length; i++) {
			if (fileList[i].indexOf(".sah") == -1) continue;
			sb.append("addToScriptList('");
			sb.append(fileList[i]);
			sb.append("');\n");
		}
		sb.append("setSelectedScript('" + scriptName + "')");			
		sb.append("\n\n\n");
		return sb.toString();
	}

	private static String[] getFileNames() {
		Configuration.createScriptsDirIfNeeded();
		return new File(Configuration.getScriptRoot()).list();
	}
}
