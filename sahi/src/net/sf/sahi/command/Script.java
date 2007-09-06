package net.sf.sahi.command;

import net.sf.sahi.playback.SahiScript;
import net.sf.sahi.report.LogViewer;
import net.sf.sahi.request.HttpRequest;
import net.sf.sahi.response.HttpFileResponse;
import net.sf.sahi.response.HttpResponse;
import net.sf.sahi.util.Utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

public class Script {
    public HttpResponse view(HttpRequest request) {
        String file = request.getParameter("script");
        return view(file);
    }

    public HttpResponse view(String file) {
        String js = makeIncludeALink(file);

        Properties props = new Properties();
        props.setProperty("name", file.replaceAll("\\\\", "/"));
        props.setProperty("js", js);
        props.setProperty("script", js);
        return new HttpFileResponse(net.sf.sahi.config.Configuration.getHtdocsRoot() + "spr/script.htm", props, false, true);
    }

    public static String makeIncludeALink(String baseFile) {
        String inputStr = new String(Utils.readFile(baseFile));
        inputStr = LogViewer.highlight(inputStr, -1);
        String patternStr = "[^\"']*.sah";
        Pattern pattern = Pattern.compile(patternStr);
        Matcher matcher = pattern.matcher(inputStr);

        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String includedScriptName = matcher.group(0);
            String scriptPath = Utils.concatPaths(baseFile, includedScriptName).replaceAll("\\\\", "/");
            String replaceStr = "";
            if (includedScriptName.startsWith("http://") || includedScriptName.startsWith("https://")){
                replaceStr = "<a href='"+includedScriptName+"'>"+includedScriptName+"</a>";
            }else{
                replaceStr = "<a href='/_s_/dyn/Script_view?script="+scriptPath+"'>"+includedScriptName+"</a>";
            }
            matcher.appendReplacement(sb, replaceStr);
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    HttpResponse dummyFunctions(HttpRequest request) {
        ArrayList words = SahiScript.getKeyWords();
        StringBuffer sb = new StringBuffer();
        for (Iterator iterator = words.iterator(); iterator.hasNext();) {
            String word = (String) iterator.next();
            sb.append("var " + word + " = b;\n");
            sb.append("var _" + word + " = b;\n");
        }
        String functions = sb.toString();

        Properties props = new Properties();
        props.setProperty("dummyFunctions", functions);
        return new HttpFileResponse(net.sf.sahi.config.Configuration.getHtdocsRoot() + "spr/dummyFunctions.js", props, false, true);
    }

}
