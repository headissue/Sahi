/**
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

package net.sf.sahi.playback.log;

import net.sf.sahi.config.Configuration;
import net.sf.sahi.util.Utils;

import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

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
                dir = Utils.concatPaths(dir, suiteLogDir);
            }
            File logDir = new File(dir);
            if (!logDir.exists()) {
                logDir.mkdirs();
            }

            File file = new File(logDir, logFileName + ".htm");
            handler = new FileHandler(file.getAbsolutePath(), append);
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
                msg = msg + "\t\t[" + debugInfo + "]";
            }
            if ("error".equals(type)) {
                logger.log(PlayBackLogLevel.ERROR, msg);
            } else if ("failure".equals(type)) {
                logger.log(PlayBackLogLevel.FAILURE, msg);
            } else if ("success".equals(type)) {
                logger.log(PlayBackLogLevel.SUCCESS, msg);
            } else if ("start".equals(type)) {
                logger.log(PlayBackLogLevel.START, msg);
            } else if ("stop".equals(type)) {
                logger.log(PlayBackLogLevel.STOP, msg);
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
