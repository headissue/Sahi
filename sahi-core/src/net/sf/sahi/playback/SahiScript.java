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

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.sahi.config.Configuration;
import net.sf.sahi.report.LogViewer;
import net.sf.sahi.util.Utils;

public abstract class SahiScript {

	private static Logger logger = Configuration
			.getLogger("net.sf.sahi.playback.SahiScript");

	private static ArrayList<String> actionKeywords;

	private static ArrayList<String> normalKeywords;

	private static Pattern pattern_set;

	private static Pattern pattern_popup_set;

	protected String script;

	private static final String PREFIX = "_sahi.schedule(\"";

	private static final String CONJUNCTION = "\", \"";

	private static final String SUFFIX = "\");";

	private String filePath;

	protected String scriptName;

	protected ArrayList<String> parents;

	private String original = "";

	private static final Pattern REG_EXP_FOR_ADDING = Pattern.compile(getRegExp(false));

	private static final Pattern REG_EXP_FOR_STRIPPING = Pattern.compile(getRegExp(true));

	private static final Pattern REG_EXP_FOR_ACTIONS = Pattern.compile(getActionRegExp());

	protected String path;

	private String jsString;

	private String browserJS;

	private String browserJSWithLineNumbers;
	
	private ArrayList<String> lineDebugInfo = new ArrayList<String>();

	static {
		pattern_set = Pattern.compile("_set\\s*\\(.*");
		pattern_popup_set = Pattern.compile("_popup\\s*\\(.*\\)\\s*[.]\\s*_set\\s*\\(.*");
	}
	
	public SahiScript(String filePath, ArrayList<String> parents, String scriptName) {
		this.filePath = filePath;
		this.scriptName = scriptName;
		this.parents = parents;
		init(filePath);
	}

	protected void setScript(final String s) {
		original = s;
		browserJS = modifyFunctionNames(extractBrowserJS(s, false));
		browserJSWithLineNumbers = modifyFunctionNames(extractBrowserJS(s, true));
		jsString = modify(removeBrowserJS(s));
		script = jsString;
	}

	String removeBrowserJS(String s) {
		StringBuffer sb = new StringBuffer(s);
		int startIx = s.indexOf("<browser>");
		while (startIx != -1) {
			int endIx = s.indexOf("</browser>", startIx);
			if (endIx == -1)
				endIx = s.length();
			else endIx = endIx  + 10;
			String browserJSSnippet = s.substring(startIx, endIx);
			browserJSSnippet = browserJSSnippet.replaceAll("[^\\n]", " ");
			sb.replace(startIx, endIx, browserJSSnippet);
			startIx = s.indexOf("<browser>", endIx);
		}
		return sb.toString();
//		return s.replaceAll("(?s)<browser>.*?</browser>", "");
	}

	String extractBrowserJS(String s, boolean addLineNumberInfo) {
		StringBuffer sb = new StringBuffer(s);
		int startIx = 0;//s.indexOf("<browser>");
		while (startIx != -1) {
			int endIx = s.indexOf("<browser>", startIx);
			if (endIx == -1)
				endIx = s.length();
			else endIx= endIx+9;
			String nonBrowserJSSnippet = s.substring(startIx, endIx);
			nonBrowserJSSnippet = nonBrowserJSSnippet.replaceAll("[^\\n]", " ");
			sb.replace(startIx, endIx, nonBrowserJSSnippet);
			startIx = s.indexOf("</browser>", endIx);
		}
		String js = sb.toString();
		if (!addLineNumberInfo) {
	        return stripBlankLines(js);
		} else
			return LogViewer.addLineNumbers(js, filePath);
	}

	private String stripBlankLines(String js) {
		String[] lines = js.split("\n");
		StringBuffer sb2 = new StringBuffer();
		int len = lines.length;
		for (int i = 0; i < len; i++) {
			String line = lines[i];
			if ("".equals(line.trim())) continue;
			sb2.append(line+"\n");
		}
		return sb2.toString();
	}

	String extractBrowserJS2(String s, boolean addLineNumberInfo) {
		StringBuffer sb = new StringBuffer();
		int startIx = s.indexOf("<browser>");
		while (startIx != -1) {
			int endIx = s.indexOf("</browser>", startIx);
			if (endIx == -1)
				endIx = s.length();
			sb.append(s.substring(startIx + 9, endIx));
			sb.append("\n");
			startIx = s.indexOf("<browser>", endIx + 10);
		}
		String withoutLineNumbers = sb.toString();
		return withoutLineNumbers;
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
		s = normalizeNewLinesForOSes(s);
		Iterator<?> tokens = Utils.getTokens(s).iterator();
		int lineNumber = 0;
		while (tokens.hasNext()) {
			lineNumber++;
			String line = ((String) tokens.next());
			if ("".equals(line)) {
				continue;
			}
			boolean isInclude = false;
			line = line.trim();
			if (line.startsWith("_include")) {
				SahiScript included = processInclude(line);
				if (included != null) {
					sb.append(included.jsString);
					browserJS += included.browserJS;
					browserJSWithLineNumbers += included.browserJSWithLineNumbers;
					lineDebugInfo.addAll(included.lineDebugInfo);
					isInclude = true;
				}
			} else if (line.contains("_condition")) {
				sb.append(modifyCondition(line, lineNumber));
			} else if (line.startsWith("_wait")) {
				sb.append(modifyWait(line, lineNumber));
			} else if (isSet(line)) {
				sb.append(processSet(line, lineNumber));
			} else if (line.startsWith("_")
					&& lineStartsWithActionKeyword(line)) {
				sb.append(scheduleLine(line, lineNumber));
			} else {
				sb.append(modifyLine(line));
			}
			if (!isInclude) lineDebugInfo.add(getDebugInfo(lineNumber));
		}
		String toString = sb.toString();
		logger.fine(toString);
		return toString;
	}

	String normalizeNewLinesForOSes(String s) {
		return s.replace("\r\n", "\n").replace("\r", "\n");
	}

	boolean isSet(String line) {
		return pattern_set.matcher(line).matches() || pattern_popup_set.matcher(line).matches();
	}

	String processSet(String line, int lineNumber) {
		String patternStr = "(.*)_set\\s*\\(\\s*([^,]*),\\s*(.*)\\)";
		Pattern pattern = Pattern.compile(patternStr);
		Matcher matcher = pattern.matcher(line);
		boolean matchFound = matcher.find();

		if (!matchFound) {
			return modifyLine(line);
		} else {
			String popupPrefix = matcher.group(1);
			String varName = matcher.group(2);
			String varValue = matcher.group(3);
			StringBuffer sb = new StringBuffer();
			String tempVarName = varName.replaceAll("[$]", "\\\\\\$");
//			System.out.println("tempVarName="+tempVarName);
			sb.append(scheduleLine(popupPrefix + "_sahi.setServerVar('" + tempVarName + "', "+ varValue + ");", lineNumber, true));
			sb.append(modifyLine(varName + " = _sahi.getServerVar('" + tempVarName + "');"));
			return sb.toString();
		}
	}

	String modifyWait(String line, int lineNumber) {
		int comma = line.indexOf(",");
		if (comma == -1) {
			return convertToExecuteWait(scheduleLine(line, lineNumber));
		}
		StringBuffer sb = new StringBuffer();
		sb.append(line.substring(0, comma));
		sb.append(", ");
		int close = line.lastIndexOf(")");
		if (close == -1) {
			close = line.length();
		}
		sb.append(line.substring(comma + 1, close).trim());
		sb.append(");");
		return convertToExecuteWait(scheduleLine(sb.toString(), lineNumber));
	}

	private String convertToExecuteWait(String s){
		return s.replaceFirst("_sahi[.]schedule", "_sahi.executeWait");
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
		sb.append(getDebugInfo(lineNumber));
		sb.append(SUFFIX);
		sb.append("\r\n");
		return sb.toString();
	}

	public String getDebugInfo(int lineNumber) {
		StringBuffer sb = new StringBuffer();
		sb.append(Utils.escapeDoubleQuotesAndBackSlashes(getDebugFilePath()));
		sb.append("&n=");
		sb.append(lineNumber);
		return sb.toString();
	}

	protected String getDebugFilePath() {
		return filePath;
	}
	
	public String getLineDebugInfo(int lineNumber) {
		return (lineNumber != -1 && lineNumber < lineDebugInfo.size()) ? lineDebugInfo.get(lineNumber) : "";
	}
	
	public static boolean lineStartsWithActionKeyword(String line) {
		return REG_EXP_FOR_ACTIONS.matcher(line).matches();
	}

	public static String modifySingleLine(String line) {
		if (line.startsWith("_") && lineStartsWithActionKeyword(line)) {
			StringBuilder sb = new StringBuilder();
			sb.append(PREFIX);
			sb.append(modifyFunctionNames(separateVariables(line)));
			sb.append(SUFFIX);
			return sb.toString();
		} else {
			return modifyFunctionNames(line);
		}
	}

	@SuppressWarnings("unchecked")
	private SahiScript processInclude(String line) {
		final String include = getInclude(line);
		if (include != null && !isRecursed(include)) {
			ArrayList<String> clone = (ArrayList<String>) parents.clone();
			clone.add(this.path);
			return new ScriptFactory().getScript(getFQN(include), clone);
		}
		return null;
	}

	private boolean isRecursed(final String include) {
		return parents.contains(getFQN(include));
	}

	abstract String getFQN(String scriptName);

	static String getInclude(String line) {
		final String re = ".*_include[\\s]*\\([\"|'](.*)[\"|']\\).*";
		Pattern p = Pattern.compile(re);
		Matcher m = p.matcher(line.trim());
		if (m.matches()) {
			return line.substring(m.start(1), m.end(1));
		}
		return null;
	}

	static String getActionRegExp() {
		ArrayList<String> keywords = getActionKeyWords();
		return getActionRegExp(keywords);
	}

	static String getActionRegExp(ArrayList<String> keywords) {
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
		return REG_EXP_FOR_ADDING.matcher(unmodified).replaceAll("_sahi.$1$2");
	}

	public static String stripSahiFromFunctionNames(String unmodified) {
		if (unmodified == null) return "";
		return REG_EXP_FOR_STRIPPING.matcher(unmodified).replaceAll("$1$2");
	}

	static String getRegExp(boolean isForStripping) {
		ArrayList<String> keywords = getKeyWords();
		return getRegExp(isForStripping, keywords);
	}

	static String getRegExp(boolean isForStripping, ArrayList<String> keywords) {
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

	public static ArrayList<String> getActionKeyWords() {
		if (actionKeywords == null) {
			actionKeywords = loadActionKeyWords();
		}
		return actionKeywords;
	}

	static ArrayList<String> loadActionKeyWords() {
		ArrayList<String> keywords = new ArrayList<String>();
		keywords.addAll(loadKeyWords("scheduler"));
		return keywords;
	}

	public static ArrayList<String> getKeyWords() {
		if (normalKeywords == null) {
			normalKeywords = loadKeyWords();
		}
		return normalKeywords;
	}

	static ArrayList<String> loadKeyWords() {
		ArrayList<String> keywords = new ArrayList<String>();
		keywords.addAll(loadKeyWords("scheduler"));
		keywords.addAll(loadKeyWords("normal"));
		return keywords;
	}

	@SuppressWarnings("unchecked")
	static ArrayList<String> loadKeyWords(String type) {
		ArrayList<String> keywords = new ArrayList();
		Properties fns = new Properties();
		try {
			String typePath = Utils.concatPaths(Configuration.getConfigPath(), type + "_functions.txt");
			fns.load(new FileInputStream(typePath));
		} catch (Exception e) {
		}
		keywords.addAll((Set<String>)(Set)fns.keySet());
		return keywords;
	}

	static String separateVariables(String s) {
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
			} else if (c == '"') {
				if (!(escaped || quoted)) {
					doubleQuoted = !doubleQuoted;
				}
			} else if (c == '\'') {
				if (!(escaped || doubleQuoted)) {
					quoted = !quoted;
				}
			} else if (c == '$' && !isVar && !escaped && !quoted && !doubleQuoted && i + 1 < len
					&& Character.isJavaIdentifierStart(s.charAt(i + 1))) {
				isVar = true;
				bracket = 0;
				square = 0;
				sb.append("\"+s_v(");
			} else if (isVar && !escaped
					&& !(Character.isJavaIdentifierPart(c) || c == '.')) {
				boolean append = false;

				if (!escaped && !quoted && !doubleQuoted) {
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
						if (bracket > 0 && (c == ',' || Character.isWhitespace(c) || c == '/')){
							append = false;
						} else { 
							append = true;
						}
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
		if (isVar){ // $var was at the end. like $a==$b
			sb.append(")+\"");
			isVar = false;
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

//	public String modifyWhile(String s, int lineNumber) {
//		return modifyIfWhile(s, lineNumber, "while (true) {\r\n",
//				"if (\"true\" != _sahi.getServerVar(\"condn\")) {break;} ");
//	}
//
//	public String modifyIf(String s, int lineNumber) {
//		return modifyIfWhile(s, lineNumber, "",
//				"if (\"true\" == _sahi.getServerVar(\"condn\")");
//	}
	public String modifyCondition(String s, int lineNumber) {
		if (s.indexOf("_condition") == -1) {
			return modifyLine(s);
		}
		String condition = findCondition(s);
		if (condition == null) {
			return modifyLine(s);
		}
		StringBuffer sb = new StringBuffer();		
		String prefix = s.substring(0, s.indexOf(condition));
		String suffix = s.substring(s.indexOf(condition) + condition.length());
		sb.append(prefix);
		String separated = "\"" + separateVariables(condition) + "\"";
		sb.append(separated);
//		System.out.println(separated);
		sb.append(", \"" + getDebugInfo(lineNumber) + "\"");
		sb.append(suffix);
		return modifyFunctionNames(sb.toString());
	}
	
//	public String modifyIfWhile(String s, int lineNumber, String prefix,
//			String suffix) {
//		if (s.indexOf("_condition") == -1) {
//			return modifyLine(s);
//		}
//		String condition = findCondition(s);
//		if (condition == null) {
//			return modifyLine(s);
//		}
//		StringBuffer sb = new StringBuffer();
//		sb.append(prefix);
//		sb.append(scheduleLine("_sahi.saveCondition(" + condition + ");",
//				lineNumber));
//		sb.append(suffix);
//		int end = s.indexOf(condition) + condition.length() + 1;
//		sb.append(s.substring(end));
//		return sb.toString();
//	}

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

	public String getBrowserJS() {
		return browserJS;
	}
	
	/*
	 * Called from lib.js
	 */
	public void addIncludeInfo(SahiScript included){
		browserJS += included.browserJS;
		browserJSWithLineNumbers += included.browserJSWithLineNumbers;
		lineDebugInfo.addAll(included.lineDebugInfo);
	}

	public static void main(String args[]){
		System.out.println("x"+"<browser>aaa\nbb\ncc\n</browser>".replaceAll("[^\\n]", "")+"y");
	}

	public String getBrowserJSWithLineNumbers() {
		return browserJSWithLineNumbers;
	}
}
