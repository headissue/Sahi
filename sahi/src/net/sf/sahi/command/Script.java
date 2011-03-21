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


public class Script {

    public HttpResponse view(final HttpRequest request) {
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

    public static String makeIncludeALink(final String baseFile) {
        String inputStr = Utils.readFileAsString(baseFile);
        inputStr = inputStr.replaceAll("<", "&lt;").replaceAll(">", "&gt;");
        inputStr = LogViewer.highlight(inputStr, -1);
        String patternStr = "[\"'](.*[.]sah)[\"']";
        Pattern pattern = Pattern.compile(patternStr);
        Matcher matcher = pattern.matcher(inputStr);

        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String includedScriptName = matcher.group(1);
            String scriptPath = Utils.concatPaths(baseFile, includedScriptName).replaceAll("\\\\", "/");
            String replaceStr = "";
            if (includedScriptName.startsWith("http://") || includedScriptName.startsWith("https://")) {
                replaceStr = "<a href='" + includedScriptName + "'>" + includedScriptName + "</a>";
            } else {
                replaceStr = "<a href='/_s_/dyn/Script_view?script=" + scriptPath + "'>" + includedScriptName + "</a>";
            }
            matcher.appendReplacement(sb, replaceStr);
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    HttpResponse dummyFunctions(final HttpRequest request) {
        ArrayList<String> words = SahiScript.getKeyWords();
        StringBuffer sb = new StringBuffer();
        for (Iterator<String> iterator = words.iterator(); iterator.hasNext();) {
            String word = iterator.next();
            sb.append("var " + word + " = b;\n");
            sb.append("var _" + word + " = b;\n");
        }
        String functions = sb.toString();

        Properties props = new Properties();
        props.setProperty("dummyFunctions", functions);
        return new HttpFileResponse(net.sf.sahi.config.Configuration.getHtdocsRoot() + "spr/dummyFunctions.js", props, false, true);
    }
}
