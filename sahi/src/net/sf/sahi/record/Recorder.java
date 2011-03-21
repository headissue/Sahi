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
    private static Logger logger = Configuration.getLogger("net.sf.sahi.record.Recorder");
    private File file = null;
    private String dir;

    public void start(final String fileName) {
        logger.fine("Starting to write  to " + fileName + ".");
        if (isStarted) {
            return;
        }
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
        if (!isStarted) {
            return;
        }
        try {
            if (out != null) {
                out.close();
            }
            isStarted = false;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isRecording() {
        return isStarted;
    }

    public void record(final String cmd) {
        if (!isStarted) {
            return;
        }
        if (cmd == null) {
            return;
        }
        if (file == null) {
            return;
        }
        logger.fine("Record:" + cmd);
        try {
            out = new FileOutputStream(file, true);
            out.write(cmd.getBytes("UTF8"));
            out.write("\n".getBytes());
            out.close();
            out = null;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setDir(String dir) {
        this.dir = dir;
    }

    public String getDir() {
        return dir;
    }
}
