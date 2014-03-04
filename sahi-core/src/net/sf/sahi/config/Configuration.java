/**
 * Sahi - Web Automation and Test Tool
 *
 * Copyright  2006  V Narayan Raman
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.sf.sahi.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import net.sf.sahi.util.FileUtils;
import net.sf.sahi.util.Utils;

/**
 * Configuration resolves all properties and paths required by Sahi.<br/>
 * Invoking Configuration.init is mandatory before invoking Proxy.start
 * 
 */
public class Configuration {

	// static Properties properties = new Properties();

	static Properties userProperties; // = new Properties(properties);

	private static final String HTDOCS_ROOT = "htdocs/";

	private static final String SAHI_PROPERTIES = "config/sahi.properties";

	private static final String SAHI_USER_PROPERTIES = "config/userdata.properties";

	private static final String LOG_PROPERITES = "config/log.properties";

	private static final String TMP_DOWNLOAD_DIR = "temp/download";

	public static final String PLAYBACK_LOG_ROOT = "playback";

	private static String userDataDir = null;

	private static String basePath = null;

	private static String[] exclusionList;

	private static String keytoolPath;

	private static boolean keytoolFound;

	/**
	 * Initializes Sahi's properties and relative paths.<br/>
	 * This is required before invoking Proxy.start()<br/>
	 * This assumes the current folder as Sahi's basePath. <br/>
	 * Same as init(".")<br/>
	 * A call to <code>init</code> or <code>initJava</code> is required before
	 * invoking Proxy.start()
	 * 
	 */
	public static void init() {
		init(".", "userdata");
	}

	/**
	 * Initializes Sahi's properties and relative paths.<br/>
	 * A call to <code>init</code> or <code>initJava</code> is required before
	 * invoking Proxy.start()
	 * 
	 * @param basePath
	 *            String basePath to folder where sahi is located
	 * @param userDataDirectory
	 *            String path to user data directory
	 */
	public static void init(String basePath1, String userDataDirectory) {
		try {
			basePath = basePath1;
			userDataDir = userDataDirectory;

			String propsPath = Utils.concatPaths(basePath, SAHI_PROPERTIES);
			System.out.println("Sahi properties file = " + propsPath);

			String userPropsPath = Utils.concatPaths(userDataDir, SAHI_USER_PROPERTIES);
			System.out.println("Sahi user properties file = " + userPropsPath);

			Properties properties = new Properties();
			loadProperties(propsPath, properties);
			userProperties = new Properties(properties);
			loadProperties(userPropsPath, userProperties);
			System.setProperty("java.util.logging.config.file", LOG_PROPERITES);
			createFolders(new File(getPlayBackLogsRoot()));
			createFolders(new File(getCertsPath()));
			createFolders(new File(tempDownloadDir()));
			copyProfiles();
			Utils.BUFFER_SIZE = getBufferSize();
			System.setProperty("java.util.logging.config.file", getLogPropertyFile());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static String getLogPropertyFile() {
		return Utils.concatPaths(userDataDir, "/config/log.properties");
	}

	private static int getBufferSize() {
		try {
			return Integer.parseInt(getUserProperty("io.buffer_size"));
		} catch (Exception e) {
			return 4096;
		}
	}

	public static void loadProperties(String sahiProperties, Properties props) throws FileNotFoundException,
			IOException {
		FileInputStream inStream = new FileInputStream(sahiProperties);
		props.load(inStream);
		inStream.close();
	}

	/**
	 * Initializes Sahi's properties and relative paths and additionally sets
	 * the Controller to Java mode.<br/>
	 * This is required before invoking Proxy.start()<br/>
	 * This assumes the current folder as Sahi's basePath. <br/>
	 * Same as initJava(".")<br/>
	 * A call to <code>init</code> or <code>initJava</code> is required before
	 * invoking Proxy.start()
	 * 
	 */
	public static void initJava() {
		initJava(".", "userdata");
	}

	/**
	 * Initializes Sahi's properties and relative paths and additionally sets
	 * the Controller to Java mode.<br/>
	 * A call to <code>init</code> or <code>initJava</code> is required before
	 * invoking Proxy.start()
	 * 
	 * @param basePath
	 *            String basePath to folder where sahi is located
	 * @param userDataDirectory
	 *            String path to user data directory
	 */
	public static void initJava(String basePath, String userDataDirectory) {
		init(basePath, userDataDirectory);
		setControllerMode("java");
	}

	public static void copyProfiles() throws IOException {
		final String templateDir = Utils.concatPaths(basePath, getUserProperty("ff.profiles.template"));
		final String profilesDirPath = Utils.concatPaths(userDataDir, getUserProperty("ff.profiles.dir"));

		File profileDir = new File(profilesDirPath);
		profileDir.mkdirs();
		String prefix = getUserProperty("ff.profiles.prefix");

		String profile0 = Utils.concatPaths(profileDir.getCanonicalPath(), prefix + 0);
		if (!new File(profile0).exists()) {
			try {
				FileUtils.copyDir(templateDir, profile0);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		int maxProfiles = Integer.parseInt(getUserProperty("ff.profiles.max_number", "10"));
		for (int i = 1; i < maxProfiles; i++) {
			String profileN = Utils.concatPaths(profileDir.getCanonicalPath(), prefix + i);
			// System.out.println("Copying profile to " + profileN);
			copyFile(profile0, profileN, "prefs.js");
			copyFile(profile0, profileN, "cert8.db");
			copyFile(profile0, profileN, "key3.db");
			copyFile(profile0, profileN, "cert_override.txt");
		}
	}

	public static void copyFile(final String origDir, final String destDir, final String fileName) {
		try {
			final File src = new File(origDir, fileName);
			if (src.exists())
				FileUtils.copyFile(src, new File(destDir, fileName));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void createFolders(final File file) {
		if (!file.exists()) {
			file.mkdirs();
		}
	}

	public static int getPort() {
		try {
			return Integer.parseInt(getUserProperty("proxy.port"));
		} catch (Exception e) {
			return 9999;
		}
	}

	public static Logger getLogger(final String name) {
		return Logger.getLogger(name);
	}

	public static String getLogsRoot() {
		String fileName = Utils.concatPaths(userDataDir, getUserProperty("logs.dir", "logs"));
		File file = new File(fileName);
		if (!file.exists()) {
			file.mkdirs();
		}
		return fileName;
	}

	public static String getSSLPassword() {
		return getUserProperty("ssl.password");
	}

	public static String[] getScriptRoots() {
		String[] propertyArray = getPropertyArray("scripts.dir", userProperties, "scripts");
		for (int i = 0; i < propertyArray.length; i++) {
			propertyArray[i] = Utils.getAbsolutePath(Utils.concatPaths(userDataDir, propertyArray[i]))
					+ System.getProperty("file.separator");
		}
		return propertyArray;
	}

	public static String[] getScriptExtensions() {
		return getPropertyArray("script.extension", userProperties, "sah;sahi;inc");
	}

	private static String[] getPropertyArray(final String key, Properties props, String defaultValue) {
		String property = props.getProperty(key, defaultValue);
		String[] tokens = property.split(";");
		for (int i = 0; i < tokens.length; i++) {
			tokens[i] = tokens[i].trim();
		}
		return tokens;
	}

	public static String getPlayBackLogsRoot() {
		String fileName = Utils.concatPaths(getLogsRoot(), PLAYBACK_LOG_ROOT);
		File file = new File(fileName);
		if (!file.exists()) {
			file.mkdirs();
		}
		return fileName;
	}

	public static String getHtdocsRoot() {
		return Utils.concatPaths(basePath, HTDOCS_ROOT) + "/";
	}

	public static String getPlaybackLogCSSFileName(final boolean addHtdocsRoot) {
		final String path = "spr/css/playback_log_format.css";
		return addHtdocsRoot ? Utils.concatPaths(getHtdocsRoot(), path) : path;
	}

	public static String getConsolidatedLogCSSFileName(final boolean addHtdocsRoot) {
		final String path = "spr/css/consolidated_log_format.css";
		return addHtdocsRoot ? Utils.concatPaths(getHtdocsRoot(), path) : path;
	}

	public static String getRhinoLibJS() {
		return new String(Utils.readFileAsString(Utils.concatPaths(getHtdocsRoot(),
				"spr/lib.js")));
	}

	public static boolean isKeepAliveEnabled() {
		return (enableKeepAlive > 0)
				|| (enableKeepAlive <= 0 && "true".equalsIgnoreCase(getUserProperty("http.keep_alive")));
	}

	public static int getTimeBetweenTestsInSuite() {
		try {
			return Integer.parseInt(getUserProperty("suite.time_between_tests"));
		} catch (Exception e) {
			return 1000;
		}
	}

	public static int getMaxInactiveTimeForScript() {
		try {
			return Integer.parseInt(getUserProperty("suite.max_inactive_time_for_script")) * 1000;
		} catch (Exception e) {
			return 20000;
		}
	}

	public static void createScriptsDirIfNeeded() {
		String[] scriptRoots = Configuration.getScriptRoots();
		for (int i = 0; i < scriptRoots.length; i++) {
			String scriptRoot = scriptRoots[i];
			File file = new File(scriptRoot);
			file.mkdirs();
		}
	}

	public static String getHotKey() {
		String hotkey = getUserProperty("controller.hotkey");
		if ("SHIFT".equals(hotkey) || "ALT".equals(hotkey) || "CTRL".equals(hotkey) || "META".equals(hotkey)) {
			return hotkey;
		}
		return "ALT";
	}

	public static String appendLogsRoot(final String fileName) {
		return Utils.concatPaths(getPlayBackLogsRoot(), fileName);
	}

	public static boolean isDevMode() {
		return "true".equals(System.getProperty("sahi.mode.dev"));
	}

	public static boolean autoCreateSSLCertificates() {
		return "true".equals(getUserProperty("ssl.auto_create_keystore"));
	}

	public static boolean isStrictVisibilityCheckEnabled() {
		return "true".equals(getUserProperty("element.visibility_check.strict"));
	}

	public static String xhrReadyStatesToWaitFor() {
		return getUserProperty("xhr.wait_ready_states", "2");
	}

	public static String getCertsPath() {
		return Utils.concatPaths(userDataDir, getUserProperty("certs.dir", "certs"));
	}

	public static String getConfigPath() {
		return Utils.concatPaths(basePath, "config/");
	}

	public static String getToolsPath() {
		return Utils.concatPaths(basePath, "tools/");
	}

	public static String getKeytoolPath() {
		if (keytoolPath == null)
			keytoolPath = fixKeytoolPath();
		return keytoolPath;
	}

	private static String fixKeytoolPath() {
		keytoolFound = true;
		String keytoolPath = getUserProperty("keytool.path", "keytool");
		if (isExecutable(keytoolPath))
			return keytoolPath;
		keytoolPath = Utils.concatPaths(System.getProperty("java.home"), "bin/keytool");
		if (isExecutable(keytoolPath))
			return keytoolPath;
		keytoolFound = false;
		return keytoolPath;
	}

	public static boolean isKeytoolFound() {
		getKeytoolPath();
		return keytoolFound;
	}

	private static boolean isExecutable(String keytoolPath) {
		try {
			Utils.executeCommand(new String[] { keytoolPath });
			System.out.println("Keytool command found at: " + keytoolPath);
			return true;
		} catch (Exception e) {
			System.out.println("Keytool command not found at: " + keytoolPath);
			return false;
		}
	}

	public static int getTimeBetweenSteps() {
		try {
			return Integer.parseInt(getUserProperty("script.time_between_steps"));
		} catch (Exception e) {
			return 100;
		}
	}

	public static void setTimeBetweenSteps(int speed) {
		userProperties.setProperty("script.time_between_steps", "" + speed);
	}

	public static int getTimeBetweenStepsOnError() {
		try {
			return Integer.parseInt(getUserProperty("script.time_between_steps_on_error"));
		} catch (Exception e) {
			return 1000;
		}
	}

	public static int getMaxReAttemptsOnError() {
		try {
			return Integer.parseInt(getUserProperty("script.max_reattempts_on_error"));
		} catch (Exception e) {
			return 10;
		}
	}

	public static int getMaxCyclesForPageLoad() {
		try {
			return Integer.parseInt(getUserProperty("script.max_cycles_for_page_load"));
		} catch (Exception e) {
			return 10;
		}
	}

	public static String[] getExclusionList() {
		if (exclusionList == null) {
			File file = new File(Utils.concatPaths(userDataDir, "config/exclude_inject.txt"));
			exclusionList = (file.exists()) ? getNonBlankLines(Utils.readCachedFile(file)) : new String[0];
		}
		return exclusionList;
	}

	public static String getDomainFixInfo() {
		if (domainFixInfo == null) {
			StringBuilder sb = new StringBuilder();
			sb.append("{");
			boolean start = true;
			File file = new File(Utils.concatPaths(userDataDir, "config/domainfix.txt"));
			String[] lines = (file.exists()) ? getNonBlankLines(Utils.readCachedFile(file)) : new String[0];
			for (int i = 0; i < lines.length; i++) {
				String line = lines[i];
				if (line.startsWith("#"))
					continue;
				String[] split = line.split("[\\s]+");
				String key = split[0];
				String value = key;
				if (split.length == 1) {
					value = split[1];
				} else {
					int ix = value.lastIndexOf(".", value.lastIndexOf(".") - 1);
					if (ix != -1)
						value = value.substring(ix + 1);
				}
				if (start) {
					start = false;
				} else {
					sb.append(",");
				}
				key = key.replace(".", "[.]").replace("*", ".*");
				sb.append("'" + key + "': '" + value + "'");
			}
			sb.append("}");
			domainFixInfo = sb.toString();
		}
		return domainFixInfo;
	}

	static int enableKeepAlive = 0;

	private static String overriddenControllerMode;

	private static String[] downloadURLList;

	private static String[] blockableSSLDomainList;

	private static String domainFixInfo;

	public static void enableKeepAlive() {
		enableKeepAlive++;
	}

	public static void disableKeepAlive() {
		enableKeepAlive--;
	}

	public static int getRemoteSocketTimeout() {
		try {
			return Integer.parseInt(getUserProperty("proxy.remote_socket_timeout"));
		} catch (Exception e) {
			return 120000;
		}
	}

	public static boolean modifyActiveX() {
		return "true".equals(getUserProperty("response.modify_activex"));
	}

	public static boolean spanVariablesAcrossSuite() {
		return "true".equals(getUserProperty("suite.global_variables"));
	}

	public static int getMaxReAttemptsOnNotMyWindowError() {
		try {
			return Integer.parseInt(getUserProperty("script.max_reattempts_on_window_not_found_error"));
		} catch (Exception e) {
			return 30;
		}
	}

	public static Pattern getDownloadContentTypesRegExp() {
		String[] downloadables = getNonBlankLines(Utils.readCachedFile(Utils.concatPaths(userDataDir,
				"config/download_contenttypes.txt")));
		if (downloadables.length != 0) {
			try {
				StringBuilder sb = new StringBuilder("(?:.*");
				for (int i = 0; i < downloadables.length; i++) {
					sb.append(downloadables[i]);
					if (i != downloadables.length - 1) {
						sb.append(".*)|(?:");
					}
				}
				sb.append(".*)");
				return Pattern.compile(sb.toString());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return Pattern.compile("");
	}

	public static String[] getDownloadURLList() {
		if (downloadURLList == null) {
			downloadURLList = getNonBlankLines(Utils.readCachedFileIfExists(Utils.concatPaths(userDataDir,
					"config/download_urls.txt")));
		}
		return downloadURLList;
	}

	public static String[] getBlockableSSLDomainsList() {
		if (blockableSSLDomainList == null) {
			blockableSSLDomainList = getNonBlankLines(Utils.readCachedFileIfExists(Utils.concatPaths(userDataDir,
					"config/block_ssl_domains.txt")));
		}
		return blockableSSLDomainList;
	}

	protected static String[] getNonBlankLines(byte[] b) {
		return getNonBlankLines(new String(b));
	}

	protected static String[] getNonBlankLines(String s) {
		s = s.trim().replaceAll("\\\r", "");
		String[] tokens = s.split("\n");
		ArrayList<String> l = new ArrayList<String>();
		for (int i = 0; i < tokens.length; i++) {
			String token = tokens[i].trim();
			if (!token.equals("")) {
				l.add(token);
			}
		}
		return (String[]) l.toArray(new String[] {});
	}

	public static String tempDownloadDir() {
		return Utils.concatPaths(userDataDir, TMP_DOWNLOAD_DIR);
	}

	public static String getPIDListCommand() {
		return getUserProperty("processhelper.pid_list_cmd", "");
	}

	public static String getPIDKillCommand() {
		return getUserProperty("processhelper.pid_kill_cmd", "");
	}

	public static int getPIDListColumnNo() {
		try {
			return Integer.parseInt(getUserProperty("processhelper.pid_list_pid_column_no"));
		} catch (Exception e) {
			return 2;
		}
	}

	public static int getScriptMaxIdleTime() {
		try {
			return Integer.parseInt(getUserProperty("script.max_idle_time"));
		} catch (Exception e) {
			return 1000;
		}
	}

	public static void setProxyProperties() {
		Properties systemProperties = System.getProperties();
		if (isHttpProxyEnabled()) {
			systemProperties.setProperty("http.proxyHost", getHttpProxyHost());
			systemProperties.setProperty("http.proxyPort", "" + getHttpProxyPort());
			systemProperties.setProperty("http.nonProxyHosts", "" + getHttpNonProxyHosts());
		}
		if (isHttpsProxyEnabled()) {
			systemProperties.setProperty("https.proxyHost", getHttpsProxyHost());
			systemProperties.setProperty("https.proxyPort", "" + getHttpsProxyPort());
			systemProperties.setProperty("http.nonProxyHosts", "" + getHttpsNonProxyHosts());
			systemProperties.setProperty("https.nonProxyHosts", "" + getHttpsNonProxyHosts());// ?
																								// Is
																								// this
																								// used?
		}
	}

	public static boolean isHttpProxyEnabled() {
		return "true".equals(getUserProperty("ext.http.proxy.enable"));
	}

	public static boolean isHttpsProxyEnabled() {
		return "true".equals(getUserProperty("ext.https.proxy.enable"));
	}

	public static String getHttpProxyHost() {
		return getUserProperty("ext.http.proxy.host");
	}

	public static String getHttpProxyPort() {
		return (String) getUserProperty("ext.http.proxy.port");
	}

	public static String getHttpNonProxyHosts() {
		return getUserProperty("ext.http.both.proxy.bypass_hosts");
	}

	public static String getHttpProxyAuthName() {
		return getUserProperty("ext.http.proxy.auth.name");
	}

	public static String getHttpProxyAuthPassword() {
		return getUserProperty("ext.http.proxy.auth.password");
	}

	public static String getHttpsProxyPort() {
		return (String) getUserProperty("ext.https.proxy.port");
	}

	public static String getHttpsProxyHost() {
		return getUserProperty("ext.https.proxy.host");
	}

	static String getHttpsNonProxyHosts() {
		return getUserProperty("ext.http.both.proxy.bypass_hosts");
	}

	public static String getHttpsProxyAuthName() {
		return getUserProperty("ext.https.proxy.auth.name");
	}

	public static String getHttpsProxyAuthPassword() {
		return getUserProperty("ext.https.proxy.auth.password");
	}

	public static boolean isHttpProxyAuthEnabled() {
		return "true".equals(getUserProperty("ext.http.proxy.auth.enable"));
	}

	public static boolean isHttpsProxyAuthEnabled() {
		return "true".equals(getUserProperty("ext.https.proxy.auth.enable"));
	}

	public static boolean isModifiedTrafficLoggingOn() {
		return "true".equals(getUserProperty("debug.traffic.log.modified"));
	}

	public static boolean isUnmodifiedTrafficLoggingOn() {
		return "true".equals(getUserProperty("debug.traffic.log.unmodified"));
	}

	public static void setModifiedTrafficLogging(boolean flag) {
		userProperties.setProperty("debug.traffic.log.modified", "" + flag);
	}

	public static void setUnmodifiedTrafficLogging(boolean flag) {
		userProperties.setProperty("debug.traffic.log.unmodified", "" + flag);
	}

	public static void main(String args[]) {
		String[] scriptRoots = Configuration.getScriptRoots();
		System.out.println(scriptRoots[0]);
	}

	public static boolean downloadIfContentDispositionIsAttachment() {
		return "true".equals(getUserProperty("download.download_if_contentdisposition_is_attachment"));
	}

	public static String getMimeTypesMappingFile() {
		return Utils.concatPaths(basePath, "config/mime-types.mapping");
	}

	public static String getAbsolutePath(String relPath) {
		return Utils.concatPaths(basePath, relPath);
	}

	public static String getAbsoluteTestDataPath(String relPath) {
		final String testDataPath = Utils.concatPaths(basePath, "testdata");
		return Utils.concatPaths(testDataPath, relPath);
	}

	public static String getAbsoluteUserPath(String relPath) {
		return Utils.concatPaths(userDataDir, relPath);
	}

	public static int sampleLength() {
		try {
			return Integer.parseInt(getUserProperty("response.sample_length"));
		} catch (Exception e) {
			return 500;
		}
	}

	public static String getSSLClientCertPath() {
		final String path = getUserProperty("ssl.client.cert.path");
		if (path == null)
			return null;
		return getAbsoluteUserPath(path);
	}

	private static String getUserProperty(String key) {
		return userProperties.getProperty(key);
	}

	private static String getUserProperty(String key, String defaultValue) {
		return userProperties.getProperty(key, defaultValue);
	}

	public static String getSSLClientCertPassword() {
		return getUserProperty("ssl.client.cert.password");
	}

	public static String getSSLClientKeyStoreType() {
		return getUserProperty("ssl.client.keystore.type", "JKS");
	}

	public static String getControllerMode() {
		if (overriddenControllerMode == null) {
			return getUserProperty("controller.mode", "sahi");
		}
		return overriddenControllerMode;
	}

	/**
	 * Sets the Controller mode. <br/>
	 * Currently valid values are "sahi" and "java".<br/>
	 * Set this to "java" to use the java Controller instead of Sahi's default
	 * Controller.
	 * 
	 * @param mode
	 *            "java" or "sahi"
	 */
	public static void setControllerMode(String mode) {
		overriddenControllerMode = mode;
	}

	public static String getSSLAlgorithm() {
		return getUserProperty("ssl.algorithm", "SunX509").trim();
	}

	public static String getInjectTop() {
		return Configuration.getAbsolutePath("config/inject_top.txt");
	}

	public static String getInjectBottom() {
		return Configuration.getAbsolutePath("config/inject_bottom.txt");
	}

	public static String getSSLCommandFile() {
		return Utils.concatPaths(getConfigPath(), "ssl.txt");
	}

	public static String getJiraPropertyPath() {
		return Utils.concatPaths(userDataDir, "config/jira.properties");
	}

	public static String getUserDataDir() {
		return Utils.getAbsolutePath(userDataDir);
	}

	public static String getChangeLogFilePath() {
		return Utils.concatPaths(getConfigPath(), "../docs/changelog.txt");
	}

	public static String getOSPropertiesFile() throws Exception {
		return Utils.concatPaths(getConfigPath(), "os.properties");
	}

	public static String getVersion() {
		String path = Utils.concatPaths(getConfigPath(), "version.txt");
		return new String(Utils.readCachedFile(path));
	}

	public static int getRhinoOptimizationLevel() {
		try {
			return Integer.parseInt(getUserProperty("rhino.optimization_level"));
		} catch (Exception e) {
			return 0;
		}
	}

	public static int getStabilityIndex() {
		try {
			int i = Integer.parseInt(getUserProperty("script.stability_index"));
			if (i < 1)
				i = 1;
			return i;
		} catch (Exception e) {
			return 5;
		}
	}

	public static boolean getEscapeUnicode() {
		return "true".equals(getUserProperty("script.escape_unicode"));
	}

	public static boolean addJSModifierFilter() {
		return !"false".equals(getUserProperty("filters.addJSModifierFilter"));
	}

	public static boolean addHTMLModifierFilter() {
		return !"false".equals(getUserProperty("filters.addHTMLModifierFilter"));
	}

	public static boolean addCharacterFilter() {
		return !"false".equals(getUserProperty("filters.addCharacterFilter"));
	}

	public static String getCommonDomain() {
		return getUserProperty("sahi.common_domain", "sahi.example.com").trim();
	}

	public static String getBrowserTypesPath() {
		return getAbsoluteUserPath("config/browser_types.xml");
	}

	public static boolean getChromeExplicitCheckboxRadioToggle() {
		return "true".equals(getUserProperty("simulation.chrome.explicit_checkbox_radio_toggle"));
	}

	public static String getIgnorableIdsPattern() {
		return getUserProperty("recorder.ignorable_ids.pattern", "^z_").trim();
	}

	public static boolean forceTreatAsXHTML() {
		return "true".equals(getUserProperty("sahi.inject.force_treat_as_xhtml"));
	}
	public static int getMaxTimeForPIDGatherFromDashboard() {
		try {
			return Integer.parseInt(getUserProperty("dashboard.max_time_for_pid_gather", "5000"));
		} catch (Exception e) {
			return 5000;
		}
	}
	public static int getMaxTimeForPIDGather() {
		try {
			return Integer.parseInt(getUserProperty("script.max_time_for_pid_gather", "60000"));
		} catch (Exception e) {
			return 60000;
		}
	}

	// Pro start
}