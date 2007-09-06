/**
 * Sahi - Web Automation and Test Tool
 *
 * Copyright  2006  V Narayan Raman
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
	private static Logger logger = Configuration
			.getLogger("net.sf.sahi.record.Recorder");
	private File file = null;
    private String dir;

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

    public void setDir(String dir) {
        this.dir = dir;
    }

    public String getDir() {
        return dir;
    }
}
