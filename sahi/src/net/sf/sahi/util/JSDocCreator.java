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

    public Function(String line) {
        this.line = line;
        String[] words = line.split("(\\(|,|\\))");
        name = words[0].trim();
        arguments = new String[words.length - 1];
        System.arraycopy(words, 1, arguments, 0, words.length - 1);
    }

    public String toString() {
        String s = "<br>" +
                (SahiScript.getActionKeyWords().contains(name) ?  "This is a scheduler function." : "" ) +
                "<br><br>\n" +
                "<h3>Syntax</h3>\n" +
                "<div class=\"syntax\">\n" +
                "<h4>" + line + "</h4>\n" +
                "The parameters are:\n" +
                "<ul>\n";
        for (int i = 0; i < arguments.length; i++) {
            s += "<li><b>" + arguments[i].trim() + "</b>: </li>\n";
        }
        s +=  "<br>\n</ul>\n" +
                "</div>\n" +
                "\n" +
                "<h3>Example:</h3>\n" +
                "<pre>\n" +
                "</pre>" +
                "\n" +
                "\n";
        return s;
    }
}
