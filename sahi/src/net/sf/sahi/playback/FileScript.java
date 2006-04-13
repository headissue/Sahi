package net.sf.sahi.playback;

import java.util.ArrayList;
import net.sf.sahi.util.Utils;

public class FileScript extends SahiScript {
	public FileScript(String fileName) {
		super(fileName);
	}

	public FileScript(String fileName, ArrayList parents) {
		super(fileName, parents);
	}

	protected void loadScript(String fileName) {
		setScript(new String(Utils.readFile(fileName)));
	}

	SahiScript getNewInstance(String scriptName, ArrayList parents) {
		FileScript fileScript = new FileScript(getFQN(scriptName), parents);
		fileScript.parents = parents;
		return fileScript;
	}
}
