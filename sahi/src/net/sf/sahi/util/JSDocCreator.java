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
package net.sf.sahi.util;

import net.sf.sahi.config.Configuration;
import net.sf.sahi.playback.SahiScript;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.StringTokenizer;

public class JSDocCreator {

    public static void main(String args[]) {

        FileOutputStream out;
        try {
            File file = new File(Utils.concatPaths(Configuration.getHtdocsRoot(), "spr/jsdoc.htm"));
            if (!file.exists()) {
                file.createNewFile();
            }
            out = new FileOutputStream(file, false);

            String content = new String(Utils.readFile(Utils.concatPaths(Configuration.getHtdocsRoot(), "spr/concat.js")));
            StringTokenizer tokenizer = new StringTokenizer(content, "\n");
            while (tokenizer.hasMoreTokens()) {
                String line = tokenizer.nextToken().trim();
                if (line.startsWith("function sahi_")) {
                    line = line.replaceAll("[ ]*[{]$", "");
                    line = line.replaceAll("function sahi", "");
                    Function function = new Function(line);
                    out.write((function.toString() + "\n").getBytes());
                }
            }
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class Function {

    String name = "";
    String[] arguments;
    private String line;

    public Function(final String line) {
        this.line = line;
        String[] words = line.split("(\\(|,|\\))");
        name = words[0].trim();
        arguments = new String[words.length - 1];
        System.arraycopy(words, 1, arguments, 0, words.length - 1);
    }

    public String toString() {

        // converted to use a StringBuilder
        StringBuilder s = new StringBuilder();
        s.append("<br>");
        s.append(SahiScript.getActionKeyWords().contains(name) ? "This is a scheduler function." : "");
        s.append("<br><br>\n");
        s.append("<h3>Syntax</h3>\n");
        s.append("<div class=\"syntax\">\n");
        s.append("<h4>");
        s.append(line);
        s.append("</h4>\n");
        s.append("The parameters are:\n");
        s.append("<ul>\n");

//        String s = "<br>" +
//                (SahiScript.getActionKeyWords().contains(name) ?  "This is a scheduler function." : "" ) +
//                "<br><br>\n" +
//                "<h3>Syntax</h3>\n" +
//                "<div class=\"syntax\">\n" +
//                "<h4>" + line + "</h4>\n" +
//                "The parameters are:\n" +
//                "<ul>\n";

        int len = arguments.length;   // cache the length so it doesn't need to be looked up over and over in the loop
        for (int i = 0; i < len; i++) {
            s.append("<li><b>");
            s.append(arguments[i].trim());
            s.append("</b>: </li>\n");
        }

        s.append("<br>\n</ul>\n");
        s.append("</div>\n");
        s.append("\n");
        s.append("<h3>Example:</h3>\n");
        s.append("<pre>\n");
        s.append("</pre>");
        s.append("\n\n");

//        s +=  "<br>\n</ul>\n" +
//                "</div>\n" +
//                "\n" +
//                "<h3>Example:</h3>\n" +
//                "<pre>\n" +
//                "</pre>" +
//                "\n" +
//                "\n";
        return s.toString();
    }
}
