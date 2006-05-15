package net.sf.sahi.playback;

import java.util.ArrayList;
import net.sf.sahi.util.Utils;

public class URLScript extends SahiScript {
	public URLScript(String fileName) {
		super(fileName);
	}

	public URLScript(String fileName, ArrayList parents) {
		super(fileName, parents);
	}

	protected void loadScript(String url) {
		setScript(new String(Utils.readURL(url)));
	}

	SahiScript getNewInstance(String scriptName, ArrayList parents) {
		scriptName = getFQN(scriptName);
		URLScript urlScript = new URLScript(scriptName, parents);
		urlScript.parents = parents;
		return urlScript;
	}
}
