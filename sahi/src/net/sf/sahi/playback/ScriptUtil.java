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
package net.sf.sahi.playback;

import net.sf.sahi.config.Configuration;
import net.sf.sahi.util.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

public class ScriptUtil {

    public static String getScriptsJs(final String scriptName) {
        String[] fileList = getScriptFiles();
        Arrays.sort(fileList);
        return getJs(fileList, scriptName, false);
    }

    public static String getScriptRootsJs(final String dir) {
        String[] fileList = Configuration.getScriptRoots();
        return getJs(fileList, Utils.escapeDoubleQuotesAndBackSlashes(dir), true);
    }

    private static String getJs(final String[] list, final String selected, final boolean isDir) {
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
        List<String> allFiles = new ArrayList<String>();
        Configuration.createScriptsDirIfNeeded();
        String[] scriptRoots = Configuration.getScriptRoots();
        for (int i = 0; i < scriptRoots.length; i++) {
            String scriptRoot = scriptRoots[i];
            File file = new File(scriptRoot);
            getFilesRecursively(file, allFiles);
        }
        return allFiles.toArray(new String[0]);
    }
    
	public static String[] getScriptFiles(String dir) {
		List<String> allFiles = new ArrayList<String>();
		File file = new File(dir);
		getFilesRecursively(file, allFiles);
		return allFiles.toArray(new String[0]);
	}
	
    private static List<String> getFilesRecursively(final File dir, final List<String> allFiles) {
        File[] files = dir.listFiles();
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            if (file.isDirectory()) {
                getFilesRecursively(file, allFiles);
            } else {
                if (isAddable(file)) {
                    allFiles.add(Utils.getAbsolutePath(file));
                }
            }
        }
        return allFiles;
    }

    private static boolean isAddable(final File file) {
        String absolutePath = Utils.getAbsolutePath(file);
        String[] extensions = Configuration.getScriptExtensions();
        for (int i = 0; i < extensions.length; i++) {
            if (absolutePath.endsWith(extensions[i])) {
                return true;
            }
        }
        return false;
    }
}
