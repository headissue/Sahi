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

import net.sf.sahi.config.Configuration;
import net.sf.sahi.util.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

public class ScriptUtil {
    public static String getScriptsJs(String scriptName) {
        String[] fileList = getScriptFiles();
        Arrays.sort(fileList);
        return getJs(fileList, scriptName, false);
    }

    public static String getScriptRootsJs(String dir) {
        String[] fileList = Configuration.getScriptRoots();
        return getJs(fileList, Utils.escapeDoubleQuotesAndBackSlashes(dir), true);
    }

    private static String getJs(String[] list, String selected, boolean isDir) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < list.length; i++) {
            String absolutePath = list[i];
            sb.append("addToScript").append(isDir ? "Dir" : "").append("List").append("('");
            sb.append(Utils.escapeDoubleQuotesAndBackSlashes(absolutePath));
            sb.append("');\n");
        }
        sb.append("setSelectedScript").append(isDir ? "Dir" : "").append("('").append(selected).append("')");
        sb.append("\n\n\n");
        return sb.toString();
    }

    private static String[] getScriptFiles() {
        List allFiles = new ArrayList();
        Configuration.createScriptsDirIfNeeded();
        String[] scriptRoots = Configuration.getScriptRoots();
        for (int i = 0; i < scriptRoots.length; i++) {
            String scriptRoot = scriptRoots[i];
            File file = new File(scriptRoot);
            getFilesRecursively(file, allFiles);
        }
        return (String[]) allFiles.toArray(new String[0]);
    }

    private static List getFilesRecursively(File dir, List allFiles) {
        File[] files = dir.listFiles();
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            if (file.isDirectory()) {
                getFilesRecursively(file, allFiles);
            } else {
                if (isAddable(file))
                    allFiles.add(file.getAbsolutePath());
            }
        }
        return allFiles;
    }

    private static boolean isAddable(File file) {
        String absolutePath = file.getAbsolutePath();
        String[] extensions = Configuration.getScriptExtensions();
        for (int i = 0; i < extensions.length; i++) {
            if (absolutePath.endsWith(extensions[i])) return true;
        }
        return false;
    }
}
