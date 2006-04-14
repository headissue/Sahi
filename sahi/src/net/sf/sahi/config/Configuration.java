package net.sf.sahi.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

import net.sf.sahi.util.Utils;

/**
 * User: nraman Date: Jun 3, 2005 Time: 12:48:07 AM To
 */
public class Configuration {
	private static Properties properties;
	private static final String LOG_PATTERN = "sahi%g.log";
	public static final String PLAYBACK_LOG_ROOT = "playback/";
	private static final String HTDOCS_ROOT = "../htdocs/";
	static {
		properties = new Properties();
		try {
			properties.load(new FileInputStream("../config/sahi.properties"));
			System.setProperty("java.util.logging.config.file",
					"../config/log.properties");
			createLogFolders(getPlayBackLogsRoot());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void createLogFolders(String dir) {
		File file = new File(dir);
		if (!file.exists()) {
			file.mkdirs();
		}
	}

	public static String getBrowserExecutable() {
		try {
			return properties.getProperty("browser");
		} catch (Exception e) {
			return "";
		}
	}

	public static int getPort() {
		try {
			return Integer.parseInt(properties.getProperty("proxy.port"));
		} catch (Exception e) {
			return 9999;
		}
	}

	public static Logger getLogger(String name) {
		FileHandler handler = null;
		try {
			int limit = 1000000; // 1 Mb
			int numLogFiles = 3;
			handler = new FileHandler(getLogsRoot() + LOG_PATTERN, limit,
					numLogFiles);
		} catch (IOException e) {
			e.printStackTrace();
		}
		Logger logger = Logger.getLogger(name);
		if (handler != null)
			logger.addHandler(handler);
		return logger;
	}

	public static String getLogsRoot() {
		return addEndSlash(properties.getProperty("logs.dir"));
	}

	public static String getScriptRoot() {
		return addEndSlash(properties.getProperty("scripts.dir"));
	}

	public static String getPlayBackLogsRoot() {
		return Utils.concatPaths(getLogsRoot(), PLAYBACK_LOG_ROOT);
	}

	private static String addEndSlash(String dir) {
		if (dir.endsWith("/") || dir.endsWith("\\"))
			return dir;
		return dir + "/";
	}

	public static String getHtdocsRoot() {
		return HTDOCS_ROOT;
	}

	public static String getPlaybackLogCSSFileName(boolean addHtdocsRoot) {
		final String path = "/spr/css/playback_log_format.css";
		return addHtdocsRoot ? Utils.concatPaths(getHtdocsRoot(), path) : path;
	}


	public static String getConsolidatedLogCSSFileName(boolean addHtdocsRoot) {
		final String path = "/spr/css/consolidated_log_format.css";
		return addHtdocsRoot ? Utils.concatPaths(getHtdocsRoot(), path) : path;
	}

	public static boolean isExternalProxyEnabled() {
		return "true".equalsIgnoreCase(properties
				.getProperty("ext.proxy.enable"));
	}

	public static String getExternalProxyHost() {
		return properties.getProperty("ext.proxy.host");
	}

	public static int getExternalProxyPort() {
		try {
			return Integer.parseInt(properties.getProperty("ext.proxy.port"));
		} catch (Exception e) {
			return 80;
		}
	}

	public static void createScriptsDirIfNeeded() {
		File file = new File(Configuration.getScriptRoot());
		file.mkdirs();
	}

	public static String getHotKey() {
		String hotkey = properties.getProperty("controller.hotkey");
		if ("SHIFT".equals(hotkey) || "ALT".equals(hotkey)
				|| "CTRL".equals(hotkey))
			return hotkey;
		return "ALT";
	}

	public static String getScriptFileWithPath(String fileName) {
		if (!fileName.endsWith(".sah"))
			fileName = fileName + ".sah";
		return getScriptRoot() + fileName;
	}

	public static String appendLogsRoot(String fileName) {
		return Utils.concatPaths(getPlayBackLogsRoot(), fileName);
	}

	public static boolean isDevMode() {
		return "true".equals(System.getProperty("sahi.mode.dev"));
	}

}
