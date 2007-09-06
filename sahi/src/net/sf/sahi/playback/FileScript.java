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

package net.sf.sahi.playback;

import net.sf.sahi.util.Utils;

import java.io.File;
import java.util.ArrayList;

public class FileScript extends SahiScript {
    public FileScript(String fileName) {
        super(fileName, new ArrayList(), new File(fileName).getName());
    }

    public FileScript(String fileName, ArrayList parents) {
        super(fileName, parents, new File(fileName).getName());
    }

    protected void loadScript(String fileName) {
        try {
            setScript(new String(Utils.readFile(fileName)));
        } catch (Exception e) {
            setScript("_log(\"Script: " + Utils.escapeDoubleQuotesAndBackSlashes(fileName) + " does not exist.\", \"failure\");\n");
        }
    }

    String getFQN(String scriptName) {
        if (scriptName.indexOf("http") == 0) {
            return scriptName;
        }
        return Utils.getRelativeFile(new File(path), scriptName).getAbsolutePath();
    }


    SahiScript getNewInstance(String scriptName, ArrayList parents) {
        FileScript fileScript = new FileScript(getFQN(scriptName), parents);
        fileScript.parents = parents;
        return fileScript;
    }
}
