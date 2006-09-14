/**
 * User: nraman
 * Date: May 18, 2005
 * Time: 10:19:59 PM
 */
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

package net.sf.sahi.playback;

import net.sf.sahi.config.Configuration;
import net.sf.sahi.util.Utils;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class SahiScript {
    private static Logger logger = Configuration
            .getLogger("net.sf.sahi.playback.SahiScript");
    private static ArrayList actionKeywords;
    private static ArrayList normalKeywords;
    protected String script;
    private static final String PREFIX = "sahiSchedule(\"";
    private static final String CONJUNCTION = "\", \"";
    private static final String SUFFIX = "\")";
    private String filePath;
    protected String scriptName;
    protected ArrayList parents;
    private String original = "";
    private static final String REG_EXP_FOR_ADDING = getRegExp(false);
    private static final String REG_EXP_FOR_STRIPPING = getRegExp(true);
    private static final String REG_EXP_FOR_ACTIONS = getActionRegExp();
    protected String path;

    public SahiScript(String filePath, ArrayList parents, String scriptName) {
        this.filePath = filePath;
        this.scriptName = scriptName;
        this.parents = parents;
        init(filePath);
    }

    protected void setScript(String s) {
        original = s;
        script = modify(s);
    }

    public String modifiedScript() {
        return script == null ? "" : script;
    }

    String modify(String s) {
        StringBuffer sb = new StringBuffer();
        Iterator tokens = Utils.getTokens(s).iterator();
        int lineNumber = 0;
        while (tokens.hasNext()) {
            lineNumber++;
            String line = ((String) tokens.next()).trim();
            if ("".equals(line))
                continue;
            if (line.startsWith("_include")) {
                sb.append(processInclude(line));
            } else if (line.startsWith("_") && lineStartsWithActionKeyword(line)) {
                sb.append(PREFIX);
                sb.append(modifyFunctionNames(separateVariables(line)));
                sb.append(CONJUNCTION);
                sb.append(Utils.escapeDoubleQuotesAndBackSlashes(filePath));
                sb.append("&n=");
                sb.append(lineNumber);
                sb.append(SUFFIX);
                sb.append("\r\n");
            } else {
                sb.append(modifyFunctionNames(line));
                sb.append("\r\n");
            }
        }
        String toString = sb.toString();
        logger.fine(toString);
        return toString;
    }

    static boolean lineStartsWithActionKeyword(String line) {
        return line.matches(REG_EXP_FOR_ACTIONS);
    }

    private String processInclude(String line) {
        final String include = getInclude(line);
        if (include != null && !isRecursed(include)) {
            ArrayList clone = (ArrayList) parents.clone();
            clone.add(this.path);
            return new ScriptFactory().getScript(getFQN(include), clone).modifiedScript();
        }
        return "";
    }

    private boolean isRecursed(final String include) {
        return parents.contains(getFQN(include));
    }

    abstract String getFQN(String scriptName);

    static String getInclude(String line) {
        final String re = ".*_include\\([\"|'](.*)[\"|']\\).*";
        Pattern p = Pattern.compile(re);
        Matcher m = p.matcher(line.trim());
        if (m.matches()) {
            return line.substring(m.start(1), m.end(1));
        }
        return null;
    }


    static String getActionRegExp() {
        ArrayList keywords = getActionKeyWords();
        return getActionRegExp(keywords);
    }

    static String getActionRegExp(ArrayList keywords) {
        StringBuffer sb = new StringBuffer();
        int size = keywords.size();
        sb.append("^(?:");
        for (int i = 0; i < size; i++) {
            String keyword = (String) keywords.get(i);
            sb.append(keyword);
            if (i != size - 1)
                sb.append("|");
        }
        sb.append(")\\s*\\(.*");
        return sb.toString();
    }

    public static String modifyFunctionNames(String unmodified) {
        unmodified = stripSahiFromFunctionNames(unmodified);
        return unmodified.replaceAll(REG_EXP_FOR_ADDING, "sahi$1$2");
    }

    public static String stripSahiFromFunctionNames(String unmodified) {
        return unmodified.replaceAll(REG_EXP_FOR_STRIPPING, "$1$2");
    }

    static String getRegExp(boolean isForStripping) {
        ArrayList keywords = getKeyWords();
        return getRegExp(isForStripping, keywords);
    }

    static String getRegExp(boolean isForStripping, ArrayList keywords) {
        StringBuffer sb = new StringBuffer();
        int size = keywords.size();
        if (isForStripping)
            sb.append("sahi");
        sb.append("_?(");
        for (int i = 0; i < size; i++) {
            String keyword = (String) keywords.get(i);
            sb.append(keyword);
            if (i != size - 1)
                sb.append("|");
        }
        sb.append(")(\\s*\\()");
        return sb.toString();
    }

    static ArrayList getActionKeyWords() {
        if (actionKeywords == null) actionKeywords = loadActionKeyWords();
        return actionKeywords;
    }

    static ArrayList loadActionKeyWords() {
        ArrayList keywords = new ArrayList();
        keywords.addAll(loadKeyWords("scheduler"));
        return keywords;
    }

    static ArrayList getKeyWords() {
        if (normalKeywords == null) normalKeywords = loadKeyWords();
        return normalKeywords;
    }

    static ArrayList loadKeyWords() {
        ArrayList keywords = new ArrayList();
        keywords.addAll(loadKeyWords("scheduler"));
        keywords.addAll(loadKeyWords("normal"));
        return keywords;
    }

    static ArrayList loadKeyWords(String type) {
        ArrayList keywords = new ArrayList();
        Properties fns = new Properties();
        try {
            fns.load(new FileInputStream("../config/" + type + "_functions.txt"));
        } catch (Exception e) {

        }
        keywords.addAll(fns.keySet());
        return keywords;
    }

    String separateVariables(String s) {
        StringBuffer sb = new StringBuffer();
        char c = ' ';
        char prev = ' ';
        boolean isVar = false;
        int len = s.length();
        int bracket = 0;
        int square = 0;
        int doubleQuote = 0;
        int quote = 0;

        for (int i = 0; i < len; i++) {
            c = s.charAt(i);
            if (!isVar && c == '$' && prev != '\\' && i + 1 < len
                    && Character.isJavaIdentifierStart(s.charAt(i + 1))) {
                isVar = true;
                bracket = 0;
                square = 0;
                doubleQuote = 0;
                quote = 0;
                sb.append("\"+s_v(");
            }
            if (isVar && !(Character.isJavaIdentifierPart(c) || c == '.')) {
                boolean append = false;
                if (c == '(') {
                    bracket++;
                } else if (c == ')') {
                    bracket--;
                    if (bracket < 0) append = true;
                } else if (c == '[') {
                    square++;
                } else if (c == ']') {
                    square--;
                    if (square < 0) append = true;
                } else if (c == '"') {
                    doubleQuote++;
                } else if (c == '"') {
                    doubleQuote--;
                    if (doubleQuote < 0) append = true;
                } else if (c == '\'') {
                    quote++;
                } else if (c == '\'') {
                    quote--;
                    if (quote < 0) append = true;
                } else {
                    append = true;
                }
                if (append) {
                    sb.append(")+\"");
                    isVar = false;
                }
            }
            if (!isVar && (c == '\\' || c == '"')) {
                sb.append('\\');
            }
            sb.append(c);
            prev = c;
        }
        return sb.toString();
    }

    protected String read(InputStream in) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int c = ' ';
        try {
            while ((c = in.read()) != -1) {
                out.write(c);
            }
        } catch (IOException ste) {
            ste.printStackTrace();
        }
        return new String(out.toByteArray());
    }

    public String getScriptName() {
        return scriptName;
    }

    protected void init(String url) {
        this.path = url;
        loadScript(url);
    }

    protected abstract void loadScript(String url);

    public String getOriginal() {
        return original;
    }

    public String getOriginalInHTML() {
        return SahiScriptHTMLAdapter.createHTML(original);
    }

    public String getFilePath() {
        return filePath;
    }
}
