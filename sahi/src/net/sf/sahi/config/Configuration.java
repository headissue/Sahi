/**
 * Copyright V Narayan Raman
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

import net.sf.sahi.util.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

/**
 * User: nraman Date: Jun 3, 2005 Time: 12:48:07 AM To
 */
public class Configuration {
    private static Properties properties;
    private static final String LOG_PATTERN = "sahi.log";
    public static final String PLAYBACK_LOG_ROOT = "playback";
    private static final String HTDOCS_ROOT = "../htdocs/";
    public static FileHandler handler;

    static {
        properties = new Properties();
        try {
            properties.load(new FileInputStream("../config/sahi.properties"));
            System.setProperty("java.util.logging.config.file",
                    "../config/log.properties");
            createLogFolders(new File(getPlayBackLogsRoot()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void createLogFolders(File file) {
        if (!file.exists()) {
            file.mkdirs();
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
        if (handler == null) {
            try {
                handler = new FileHandler(Utils.concatPaths(getLogsRoot(), LOG_PATTERN).replaceAll("\\\\", "/"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Logger logger = Logger.getLogger(name);
        if (handler != null)
            logger.addHandler(handler);
        return logger;
    }

    public static String getLogsRoot() {
        String fileName = properties.getProperty("logs.dir");
        File file = new File(fileName);
        if (!file.exists()) file.mkdirs();
        return fileName;
    }

    public static String getSSLPassword() {
        return properties.getProperty("ssl.password");
    }

    public static String[] getScriptRoots() {
        String[] propertyArray = getPropertyArray("scripts.dir");
        for (int i = 0; i < propertyArray.length; i++) {
            propertyArray[i] = new File(propertyArray[i]).getAbsolutePath() + System.getProperty("file.separator");
        }
        return propertyArray;
    }

    public static String[] getScriptExtensions() {
        return getPropertyArray("script.extension");
    }

    private static String[] getPropertyArray(String key) {
        String property = properties.getProperty(key);
        String[] tokens = property.split(";");
        for (int i = 0; i < tokens.length; i++) {
            tokens[i] = tokens[i].trim();
        }
        return tokens;
    }


    public static String getPlayBackLogsRoot() {
        String fileName = Utils.concatPaths(getLogsRoot(), PLAYBACK_LOG_ROOT);
        File file = new File(fileName);
        if (!file.exists()) file.mkdirs();
        return fileName;
    }

    public static String getHtdocsRoot() {
        return HTDOCS_ROOT;
    }

    public static String getPlaybackLogCSSFileName(boolean addHtdocsRoot) {
        final String path = "spr/css/playback_log_format.css";
        return addHtdocsRoot ? Utils.concatPaths(getHtdocsRoot(), path) : path;
    }


    public static String getConsolidatedLogCSSFileName(boolean addHtdocsRoot) {
        final String path = "spr/css/consolidated_log_format.css";
        return addHtdocsRoot ? Utils.concatPaths(getHtdocsRoot(), path) : path;
    }

    public static boolean isExternalProxyEnabled() {
        return "true".equalsIgnoreCase(properties
                .getProperty("ext.proxy.enable"));
    }

    public static String getExternalProxyHost() {
        return properties.getProperty("ext.proxy.host");
    }


    public static int getTimeBetweenTestsInSuite() {
        try {
            return Integer.parseInt(properties.getProperty("suite.time_between_tests"));
        } catch (Exception e) {
            return 1000;
        }
    }

    public static int getExternalProxyPort() {
        try {
            return Integer.parseInt(properties.getProperty("ext.proxy.port"));
        } catch (Exception e) {
            return 80;
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
        String hotkey = properties.getProperty("controller.hotkey");
        if ("SHIFT".equals(hotkey) || "ALT".equals(hotkey)
                || "CTRL".equals(hotkey))
            return hotkey;
        return "ALT";
    }

    public static String appendLogsRoot(String fileName) {
        return Utils.concatPaths(getPlayBackLogsRoot(), fileName);
    }

    public static boolean isDevMode() {
        return "true".equals(System.getProperty("sahi.mode.dev"));
    }

    public static boolean autoCreateSSLCertificates() {
        return "true".equals(properties.getProperty("ssl.auto_create_keystore"));
    }

    public static String getCertsPath() {
        return properties.getProperty("certs.dir");
    }

    public static String getConfigPath() {
        return "../config/";
    }

    public static String getKeytoolPath() {
        return properties.getProperty("keytool.path", "keytool");
    }

    public static int getTimeBetweenSteps() {
        try {
            return Integer.parseInt(properties.getProperty("script.time_between_steps"));
        } catch (Exception e) {
            return 100;
        }
    }

    public static int getTimeBetweenStepsOnError() {
        try {
            return Integer.parseInt(properties.getProperty("script.time_between_steps_on_error"));
        } catch (Exception e) {
            return 1000;
        }
    }

    public static int getMaxReAttemptsOnError() {
        try {
            return Integer.parseInt(properties.getProperty("script.max_reattempts_on_error"));
        } catch (Exception e) {
            return 10;
        }
    }

    public static int getMaxCyclesForPageLoad() {
        try {
            return Integer.parseInt(properties.getProperty("script.max_cycles_for_page_load"));
        } catch (Exception e) {
            return 10;
        }
    }
}
