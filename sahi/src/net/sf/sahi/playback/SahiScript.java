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

    private static Logger logger = Configuration.getLogger("net.sf.sahi.playback.SahiScript");
    private static ArrayList actionKeywords;
    private static ArrayList normalKeywords;
    protected String script;
    private static final String PREFIX = "_sahi.schedule(\"";
    private static final String CONJUNCTION = "\", \"";
    private static final String SUFFIX = "\");";
    private String filePath;
    protected String scriptName;
    protected ArrayList parents;
    private String original = "";
    private static final String REG_EXP_FOR_ADDING = getRegExp(false);
    private static final String REG_EXP_FOR_STRIPPING = getRegExp(true);
    private static final String REG_EXP_FOR_ACTIONS = getActionRegExp();
    protected String path;
    private String jsString;

    public SahiScript(String filePath, ArrayList parents, String scriptName) {
        this.filePath = filePath;
        this.scriptName = scriptName;
        this.parents = parents;
        init(filePath);
    }

    protected void setScript(final String s) {
        original = s;
        jsString = modify(s);
        script = appendFunction(jsString);//addJSEvalCode(jsString);
    }

    private String appendFunction(final String jsString) {

        StringBuffer buf = new StringBuffer();

        buf.append("_sahi.scriptScope = function (){");
        buf.append("\n\t_sahi.scriptScope.execute = function(s){eval(s);};\n\n");
        buf.append("//Your code starts\n\n");
        buf.append(jsString);
        buf.append("\n\n//Your code ends\n\n}");
        buf.append("\ntry{_sahi.scriptScope();}catch(e){_sahi.loadError = e; throw e;}");

        return buf.toString();
    }

    public String jsString() {
        return jsString;
    }

    static String addJSEvalCode(String str) {
        StringBuffer sb = new StringBuffer();
        sb.append("_sahi.execSteps = \"");
        sb.append(Utils.makeString(str));
        sb.append("\";\neval(_sahi.execSteps);");
        return sb.toString();
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
            if ("".equals(line)) {
                continue;
            }
            if (line.startsWith("_include")) {
                sb.append(processInclude(line));
            } else if (line.startsWith("while")) {
                sb.append(modifyWhile(line, lineNumber));
            } else if (line.startsWith("if")) {
                sb.append(modifyIf(line, lineNumber));
            } else if (line.startsWith("_wait")) {
                sb.append(modifyWait(line, lineNumber));
            } else if (line.startsWith("_") && lineStartsWithActionKeyword(line)) {
                sb.append(scheduleLine(line, lineNumber));
            } else if (line.startsWith("_set")) {
                sb.append(processSet(line, lineNumber));
            } else {
                sb.append(modifyLine(line));
            }
        }
        String toString = sb.toString();
        logger.fine(toString);
        return toString;
    }

    String processSet(String line, int lineNumber) {
        String patternStr = "_set\\s*\\(\\s*([^,]*),\\s*(.*)\\)";

        Pattern pattern = Pattern.compile(patternStr);
        Matcher matcher = pattern.matcher(line);
        boolean matchFound = matcher.find();

        if (!matchFound) {
            return modifyLine(line);
        } else {
            String varName = matcher.group(1);
            String varValue = matcher.group(2);
            StringBuffer sb = new StringBuffer();
            sb.append("var $sahi_cmdLen = _sahi.cmds.length+1;\r\n");
            String dollarVarName = varName.startsWith("$") ? "\\" + varName : varName;
            sb.append(scheduleLine("_sahi.handleSet('" + dollarVarName + "' + $sahi_cmdLen, " + varValue + ");", lineNumber, true));
            sb.append(modifyLine(varName + "Temp = _getGlobal('" + varName + "' + _sahi.cmds.length);"));
            sb.append(modifyLine("if (" + varName + "Temp) " + varName + " = " + varName + "Temp;"));
            return sb.toString();
        }
    }

    String modifyWait(String line, int lineNumber) {
        int comma = line.indexOf(",");
        if (comma == -1) {
            return scheduleLine(line, lineNumber);
        }
        StringBuffer sb = new StringBuffer();
        sb.append(line.substring(0, comma));
        sb.append(", ");
        int close = line.lastIndexOf(")");
        if (close == -1) {
            close = line.length();
        }
        sb.append("\"");
        sb.append(separateVariables(line.substring(comma + 1, close).trim()));
        sb.append("\");");
        return scheduleLine(sb.toString(), lineNumber);
    }

    private String modifyLine(String line) {
        StringBuffer sb = new StringBuffer();
        sb.append(modifyFunctionNames(line));
        sb.append("\r\n");
        return sb.toString();
    }

    private String scheduleLine(String line, int lineNumber) {
        return scheduleLine(line, lineNumber, true);
    }

    private String scheduleLine(String line, int lineNumber, boolean separate) {
        StringBuffer sb = new StringBuffer();
        sb.append(PREFIX);
        if (separate) {
            line = separateVariables(line);
        }
        sb.append(modifyFunctionNames(line));
        sb.append(CONJUNCTION);
        sb.append(Utils.escapeDoubleQuotesAndBackSlashes(filePath));
        sb.append("&n=");
        sb.append(lineNumber);
        sb.append(SUFFIX);
        sb.append("\r\n");
        return sb.toString();
    }

    static boolean lineStartsWithActionKeyword(String line) {
        return line.matches(REG_EXP_FOR_ACTIONS);
    }

    private String processInclude(String line) {
        final String include = getInclude(line);
        if (include != null && !isRecursed(include)) {
            ArrayList clone = (ArrayList) parents.clone();
            clone.add(this.path);
            return new ScriptFactory().getScript(getFQN(include), clone).jsString;
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
            if (i != size - 1) {
                sb.append("|");
            }
        }
        sb.append(")\\s*\\(.*");
        return sb.toString();
    }

    public static String modifyFunctionNames(String unmodified) {
        unmodified = stripSahiFromFunctionNames(unmodified);
        return unmodified.replaceAll(REG_EXP_FOR_ADDING, "_sahi.$1$2");
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
        if (isForStripping) {
            sb.append("_sahi.");
        }
        sb.append("_?(");
        for (int i = 0; i < size; i++) {
            String keyword = (String) keywords.get(i);
            sb.append(keyword);
            if (i != size - 1) {
                sb.append("|");
            }
        }
        sb.append(")(\\s*\\()");
        return sb.toString();
    }

    public static ArrayList getActionKeyWords() {
        if (actionKeywords == null) {
            actionKeywords = loadActionKeyWords();
        }
        return actionKeywords;
    }

    static ArrayList loadActionKeyWords() {
        ArrayList keywords = new ArrayList();
        keywords.addAll(loadKeyWords("scheduler"));
        return keywords;
    }

    public static ArrayList getKeyWords() {
        if (normalKeywords == null) {
            normalKeywords = loadKeyWords();
        }
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
        boolean isVar = false;
        boolean escaped = false;
        boolean doubleQuoted = false;
        boolean quoted = false;
        int len = s.length();
        int bracket = 0;
        int square = 0;

        for (int i = 0; i < len; i++) {
            c = s.charAt(i);
            if (c == '\\') {
                escaped = !escaped;
            }
            if (!isVar && c == '$' && !escaped && i + 1 < len && Character.isJavaIdentifierStart(s.charAt(i + 1))) {
                isVar = true;
                bracket = 0;
                square = 0;
                sb.append("\"+s_v(");
            }
            if (isVar && !escaped && !(Character.isJavaIdentifierPart(c) || c == '.')) {
                boolean append = false;

                if (c == '"') {
                    if (!(escaped || quoted)) {
                        doubleQuoted = !doubleQuoted;
                    }
                } else if (c == '\'') {
                    if (!(escaped || doubleQuoted)) {
                        quoted = !quoted;
                    }
                } else if (!escaped && !quoted && !doubleQuoted) {
                    if (c == '(') {
                        bracket++;
                    } else if (c == ')') {
                        bracket--;
                        if (bracket < 0) {
                            append = true;
                        }
                    } else if (c == '[') {
                        square++;
                    } else if (c == ']') {
                        square--;
                        if (square < 0) {
                            append = true;
                        }
                    } else {
                        append = true;
                    }
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
            if (c != '\\') {
                escaped = false;
            }
        }
        return sb.toString();
    }

    String findCondition(String s) {
        char c = ' ';
        boolean escaped = false;
        boolean doubleQuoted = false;
        boolean quoted = false;
        int len = s.length();
        int bracket = 0;

        int i = 0;
        i = s.indexOf("_condition");
        i = s.indexOf("(", i) + 1;
        if (i == 0) {
            return null;
        }
        int start = i;
        int end = -1;

        for (; i < len; i++) {
            c = s.charAt(i);
            if (c == '\\') {
                escaped = !escaped;
            }
            if (!escaped && !(Character.isJavaIdentifierPart(c) || c == '.')) {
                if (c == '"') {
                    if (!(escaped || quoted)) {
                        doubleQuoted = !doubleQuoted;
                    }
                } else if (c == '\'') {
                    if (!(escaped || doubleQuoted)) {
                        quoted = !quoted;
                    }
                } else if (!escaped && !quoted && !doubleQuoted) {
                    if (c == '(') {
                        bracket++;
                    } else if (c == ')') {
                        bracket--;
                        if (bracket < 0) {
                            end = i;
                            break;
                        }
                    }
                }
            }
            if (c != '\\') {
                escaped = false;
            }
        }
        if (end == -1) {
            return null;
        }
        return s.substring(start, end);
    }

    public String modifyWhile(String s, int lineNumber) {
        return modifyIfWhile(s, lineNumber, "while (true) {\r\n", "if (\"true\" != _sahi._getGlobal(\"condn\" + (_sahi.cmds.length))) break;//");
    }

    public String modifyIf(String s, int lineNumber) {
        return modifyIfWhile(s, lineNumber, "", "if (\"true\" == _sahi._getGlobal(\"condn\" +(_sahi.cmds.length))");
    }

    public String modifyIfWhile(String s, int lineNumber, String prefix, String suffix) {
        if (s.indexOf("_condition") == -1) {
            return modifyLine(s);
        }
        String condition = findCondition(s);
        if (condition == null) {
            return modifyLine(s);
        }
        StringBuffer sb = new StringBuffer();
        sb.append(prefix);
        sb.append(scheduleLine("_sahi.saveCondition(" + condition + ");", lineNumber));
        sb.append(suffix);
        int end = s.indexOf(condition) + condition.length() + 1;
        sb.append(s.substring(end));
        return sb.toString();
    }

    protected String read(final InputStream in) {
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

    protected void init(final String url) {
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
