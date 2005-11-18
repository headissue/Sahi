package com.sahi.playback.log;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import com.sahi.config.Configuration;
import com.sahi.util.Utils;

/**
 * User: nraman Date: Jun 22, 2005 Time: 11:50:44 PM
 */
public class PlayBackLogger {
	private Logger logger;
	private FileHandler handler = null;
	private String logFileName;

	public PlayBackLogger(String scriptName) {
		this(scriptName, null);
	}

	public PlayBackLogger(String scriptName, String suiteLogDir) {
		boolean append = true;
		logFileName = Utils.createLogFileName(scriptName);
		try {
			String dir = Configuration.getPlayBackLogsRoot();
			if (suiteLogDir != null && !suiteLogDir.equals("")) {
				dir += "/" + suiteLogDir;
			}
			Configuration.createLogFolders(dir);
			handler = new FileHandler(dir + "/" + logFileName + ".htm", append);
			handler.setFormatter(new PlayBackLogFormatter());
			logger = Logger.getLogger("PlayBackLogger:" + logFileName);
			logger.addHandler(handler);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void log(String msg, String type, String debugInfo) {
		try {
			if (debugInfo != null) {
				msg = "(" + debugInfo + ")\t\t" + msg;
			}
			if ("error".equals(type)) {
				logger.log(PlayBackLogLevel.ERROR, msg);
			} else if ("failure".equals(type)) {
				logger.log(PlayBackLogLevel.FAILURE, msg);
			} else if ("success".equals(type)) {
				logger.log(PlayBackLogLevel.SUCCESS, msg);
			} else {
				logger.log(PlayBackLogLevel.INFO2, msg);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void stop() {
		handler.flush();
		handler.close();
		logger.removeHandler(handler);
	}

	public String getScriptLogFile() {
		return logFileName + ".htm";
	}
}
