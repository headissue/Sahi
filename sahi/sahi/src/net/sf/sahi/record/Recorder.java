package net.sf.sahi.record;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Logger;
import net.sf.sahi.config.Configuration;

/**
 * User: nraman Date: May 20, 2005 Time: 6:59:26 PM
 */
public class Recorder {
	private FileOutputStream out;
	boolean isStarted = false;
	private static Logger logger = Configuration
			.getLogger("net.sf.sahi.record.Recorder");
	private File file = null;

	public void start(String fileName) {
		logger.fine("Starting to write  to " + fileName + ".");
		if (isStarted)
			return;
		Configuration.createScriptsDirIfNeeded();
		file = new File(fileName).getAbsoluteFile();
		try {
			file.createNewFile();
			isStarted = true;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void stop() {
		logger.fine("Stopping.");
		if (!isStarted)
			return;
		try {
			if (out != null) out.close();
			isStarted = false;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public boolean isRecording() {
		return isStarted;
	}

	public void record(String cmd) {
		if (!isStarted)
			return;
		if (cmd == null)
			return;
		if (file == null)
			return;
		logger.fine("Record:" + cmd);
		try {
			out = new FileOutputStream(file, true);
			out.write((cmd + "\n").getBytes());
			out.close();
			out = null;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
