package net.sf.sahi.playback;

import java.io.File;

import net.sf.sahi.config.Configuration;
import net.sf.sahi.util.Utils;

public class ScriptUtil {
    public static String getScriptsJs(String scriptName) {
        File[] fileList = getFileNames();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < fileList.length; i++) {
            String absolutePath = fileList[i].getAbsolutePath();
            if (absolutePath.indexOf(".sah") == -1) continue;
            sb.append("addToScriptList('");
            sb.append(Utils.escapeDoubleQuotesAndBackSlashes(absolutePath));
            sb.append("');\n");
        }
        sb.append("setSelectedScript('" + scriptName + "')");
        sb.append("\n\n\n");
        return sb.toString();
    }

    private static File[] getFileNames() {
        Configuration.createScriptsDirIfNeeded();
        return new File(Configuration.getScriptRoot()).listFiles();
    }
}
