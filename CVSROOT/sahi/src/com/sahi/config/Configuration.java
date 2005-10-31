package com.sahi.config;

import com.sahi.record.ScriptFormat;
import com.sahi.record.SahiScriptFormat;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.File;
import java.util.Properties;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

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

	public static String getScriptFormatClassName() {
		return properties.getProperty("scriptformat.classname");
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

	public static ScriptFormat getScriptFormat() {
		Class clazz = null;
		try {
			clazz = Class.forName(getScriptFormatClassName());
			return (ScriptFormat) clazz.newInstance();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		}
		System.out.println("Loading default SahiScriptFormat due to errors.");
		return new SahiScriptFormat();
	}

	public static Logger getLogger(String name) {
		FileHandler handler = null;
		try {
			int limit = 1000000; // 1 Mb
			int numLogFiles = 3;
			handler = new FileHandler(getLogsRoot()+LOG_PATTERN, limit, numLogFiles);
		} catch (IOException e) {
			e.printStackTrace();
		}
		Logger logger = Logger.getLogger(name);
		if (handler!=null) logger.addHandler(handler);
		return logger;
	}
	
	public static String getLogsRoot() {
		return addEndSlash(properties.getProperty("logs.dir"));
	}

	public static String getScriptRoot() {
		return addEndSlash(properties.getProperty("scripts.dir"));
	}

	public static String getPlayBackLogsRoot() {
		return getLogsRoot()+PLAYBACK_LOG_ROOT;
	}

	private static String addEndSlash(String dir) {
		if (dir.endsWith("/") || dir.endsWith("\\"))
			return dir;
		return dir + "/";
	}

	public static String getHtdocsRoot() {
		return HTDOCS_ROOT;
	}

	public static boolean isMultiDomainEnabled() {
		return "true".equals(properties.getProperty("multidomains"));
	}

	public static boolean isExternalProxyEnabled() {
		return "true".equalsIgnoreCase(properties.getProperty("ext.proxy.enable"));
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
}
