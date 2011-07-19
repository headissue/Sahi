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

//
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User: nraman Date: Jun 26, 2005 Time: 4:52:58 PM
 */
public class Utils {
	final static public SimpleDateFormat COMMON_DATE_FORMAT = new SimpleDateFormat("MMM dd, yyyy hh:mm:ss a");

	public static String escapeDoubleQuotesAndBackSlashes(final String line) {
		if (line == null) {
			return null;
		}
		return line.replaceAll("\\\\", "\\\\\\\\").replaceAll("\"", "\\\\\"");
	}

	public static byte[] getBytes(InputStream in) throws IOException {
		return getBytes(in, -1);
	}

	public static byte[] getBytes(final InputStream in, final int contentLength) throws IOException {
		BufferedInputStream bin = new BufferedInputStream(in, BUFFER_SIZE);

		if (contentLength != -1) {
			int totalBytesRead = 0;
			byte[] buffer = new byte[contentLength];
			while (totalBytesRead < contentLength) {
				int bytesRead = -1;
				try{
					bytesRead = bin.read(buffer, totalBytesRead, contentLength - totalBytesRead);
				}catch(EOFException e){}
				if (bytesRead == -1) {
					break;
				}
				totalBytesRead += bytesRead;
			}
			return buffer;
		} else {
			ByteArrayOutputStream byteArOut = new ByteArrayOutputStream();
			BufferedOutputStream bout = new BufferedOutputStream(byteArOut);
			try {
				int totalBytesRead = 0;
				byte[] buffer = new byte[BUFFER_SIZE];

				while (true) {
					int bytesRead = -1;
					try{
						bytesRead = bin.read(buffer);
					}catch(EOFException e){}
					if (bytesRead == -1) {
						break;
					}
					bout.write(buffer, 0, bytesRead);
					totalBytesRead += bytesRead;
				}
			} catch (SocketTimeoutException ste) {
				ste.printStackTrace();
			}
			bout.flush();
			bout.close();
			return byteArOut.toByteArray();
		}
	}
	
	public static byte[] readURL(final String url) {
		return readURL(url, true);
	}
	public static byte[] readURLThrowException(final String url) throws MalformedURLException, IOException {
		byte[] data = null;
		InputStream inputStream = null;
		try {
			inputStream = new URL(url).openStream();
			data = getBytes(inputStream, -1);
			inputStream.close();
		} finally {
				inputStream.close();
		}
		return data;		
	}
	public static byte[] readURL(final String url, boolean printExceptions) {
		byte[] data = null;
		InputStream inputStream = null;
		try {
			inputStream = new URL(url).openStream();
			data = getBytes(inputStream, -1);
			inputStream.close();
		} catch (Exception e) {
			if (printExceptions) 
				e.printStackTrace();
		} finally {
			try {
				inputStream.close();
			} catch (Exception e) {
				if (printExceptions) 
					e.printStackTrace();
			}
		}
		return data;
	}

	static Map<String, Object> fileCache = new HashMap<String, Object>();
	public static int BUFFER_SIZE = 8192;
	
	public static byte[] readCachedFile(final File file) {
		return readCachedFile(file.getAbsolutePath());
	}
	
	public static byte[] readCachedFileIfExists(final String fileName) {
		File file = new File(fileName);
		if (file.exists()) return readCachedFile(fileName);
		return new byte[0];
	}
	public static byte[] readCachedFile(final String fileName) {
		if (!fileCache.containsKey(fileName)) {
			fileCache.put(fileName, readFile(fileName));
		}
		return (byte[]) fileCache.get(fileName);
	}

	public static byte[] readFile(final String fileName) {
		File file = new File(fileName);
		return readFile(file);
	}

	public static String getString(byte[] bytes){
		try {
			return new String(bytes, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			return new String(bytes);
		}
	}
	
	public static byte[] readFile(final File file) {
		if (file != null && file.isDirectory()) {
			throw new FileIsDirectoryException();
		}
		byte[] data = null;
		InputStream inputStream = null;
		try {
			inputStream = new FileInputStream(file);
			data = getBytes(inputStream, -1);
		} catch (IOException e) {
			throw new FileNotFoundRuntimeException(e);
		} finally {
			try {
				if (inputStream != null) {
					inputStream.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return data;
	}

	public static void writeFile(String str, String filePath, boolean overWrite) {
		try {
			File file = new File(filePath);
			if (!file.exists()) {
				createNewFile(file);
			}
			FileOutputStream out;
			out = new FileOutputStream(file, !overWrite);
			out.write((str).getBytes());
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("File:" + filePath + " xx " + filePath.replace('\\', '/'));
			System.out.println(str);
		}
	}

	public static void createNewFile(String file) throws IOException {
		createNewFile(new File(file));
	}
	public static void createNewFile(File file) throws IOException {
		if (file.exists()) return;
		file.getParentFile().mkdirs();
		file.createNewFile();
	}

	public static boolean deleteFile(String filePath) {
		File file = new File(filePath);
		if (file.exists()) {
			return file.delete();
		}
		return false;
	}
	
	public static String readFileAsString(final File file) {
		return getString(readFile(file));
	}

	public static String readFileAsString(final String fileName) {
		return getString(readFile(fileName));
	}

	// Returns a string than has all its non-ASCII characters converted to its
	// ASCII
	// equivalent. Eg : By passing "Éléphant", the function would return
	// "Elephant".
	// Should consider refactoring to StringBuilder();
	public static String convertStringToASCII(String s) {
		return s.replaceAll("(è|é|ê|ë)", "e").replaceAll("(ù|ú|û|ü)", "u").replaceAll("(à|á|â|ã|ä|å)", "a").replaceAll(
				"æ", "ae").replaceAll("(ì|í|î|ï)", "i").replaceAll("(ò|ó|ô|õ|ö|ø)", "o").replaceAll("(ý|ÿ)", "y")
				.replaceAll("ñ", "n").replaceAll("ç", "c").replaceAll("(À|Á|Â|Ã|Ä|Å)", "A").replaceAll("Æ", "AE")
				.replaceAll("Ç", "C").replaceAll("(È|É|Ê|Ë)", "E").replaceAll("(Ì|Í|Î|Ï)", "I").replaceAll("Ñ", "N")
				.replaceAll("(Ò|Ó|Ô|Õ|Ö|Ø)", "O").replaceAll("(Ù|Ú|Û|Ü)", "U").replaceAll("Ý", "Y");
	}

	public static synchronized String createLogFileName(final String scriptFileName) {
		String TMPscriptFileName = new File(scriptFileName).getName();
		String date = getFormattedDateForFile(new Date());
		return convertStringToASCII(TMPscriptFileName.replaceAll("[.].*$", "") + "__" + date);
	}

	static String getFormattedDateForFile(Date date) {
		return new SimpleDateFormat("ddMMMyyyy__HH_mm_ss").format(date);
	}

	public static Date getDateFromFileName(String fileName) {
		int start = fileName.lastIndexOf("__");
		int length = fileName.length();
		if (start != -1)
			start = fileName.lastIndexOf("__", start - 1);
		int end = fileName.lastIndexOf(".");
		start = (start == -1) ? 0 : start + 2;
		if (end == -1)
			end = length;
		String dateString = fileName.substring(start, end);
		try {
			return new SimpleDateFormat("ddMMMyyyy__HH_mm_ss").parse(dateString);
		} catch (ParseException e) {
			return null;
		}
	}

	public static File getRelativeFile(File parent, final String s2) {
		File sf2 = new File(s2);
		if (sf2.isAbsolute()) {
			return sf2;
		}
		if (!parent.isDirectory()) {
			parent = parent.getParentFile();
		}
		File file = new File(parent, s2);
		return file;
	}

	public static String concatPaths(final String s1, final String s2, final String s3) {
		return concatPaths(concatPaths(s1, s2), s3);
	}

	public static String concatPaths(final String s1, final String s2) {
		return concatPaths(s1, s2, false);
	}

	public static String concatPaths(final String s1, final String s2, boolean returnRelative) {
		File sf2 = new File(s2);
		if (sf2.isAbsolute()) {
			return s2;
		}
		File parent = new File(s1);
		if (!parent.isDirectory()) {
			parent = parent.getParentFile();
		}
		File file = new File(parent, s2);
		return returnRelative ? file.getPath() : getAbsolutePath(file);
	}

	public static ArrayList<String> getTokens(final String s) {
		ArrayList<String> tokens = new ArrayList<String>();
		int ix1 = 0;
		int ix2 = -1;
		int len = s.length();
		while (ix1 < len && (ix2 = s.indexOf('\n', ix1)) != -1) {
			String token = s.substring(ix1, ix2 + 1);
			tokens.add(token);
			ix1 = ix2 + 1;
		}
		if (ix2 == -1) {
			String token = s.substring(ix1);
			tokens.add(token);
		}
		return tokens;
	}

	public static boolean isBlankOrNull(final String s) {
		return (s == null || "".equals(s));
	}

	@SuppressWarnings("unchecked")
	public static String substitute(final String content, final Map substitutions) {

		StringBuffer patternBuf = new StringBuffer();
		int i = 0;
		Iterator<?> keys = substitutions.keySet().iterator();
		while (keys.hasNext()) {
			String key = (String) keys.next();
			patternBuf.append(i++ == 0 ? "" : "|").append("\\$").append(key);
		}
		Pattern pattern = Pattern.compile(patternBuf.toString());
		patternBuf = null;
		Matcher matcher = pattern.matcher(content);

		StringBuffer buf = new StringBuffer();
		while (matcher.find()) {
			String key = matcher.group(0).substring(1);
			String replaceStr = ((String) substitutions.get(key)).replace("\\", "\\\\").replaceAll("\\$", "SDLR");
			matcher.appendReplacement(buf, replaceStr);
		}
		matcher.appendTail(buf);
		return buf.toString().replaceAll("SDLR", "\\$");
	}

	public static String makeString(String s) {
		if (s == null) {
			return null;
		}
		return escapeDoubleQuotesAndBackSlashes(s).replaceAll("\n", "\\\\n").replaceAll("\r", "");

	}

	public static String escapeQuotesForXML(final String input) {
		return input.replaceAll("\"", "&quot;");
	}

	public static String escapeQuotes(final String input) {
		return input.replaceAll("\"", "\\\\\"");
	}

	public static String addChildSessionId(String sessionId) {
//		return sessionId + "sahix" + "SAHI_CHILD_ID" + "x";
		return sessionId + "sahix" + Utils.getUUID() + "x";
	}

	public static String stripChildSessionId(final String sessionId) {
		return sessionId.replaceFirst("sahix[^x]+x", "");
	}

	public static void deleteDir(final File dir) {
		try {
			if (dir.exists()) {
				File[] files = dir.listFiles();
				int len = files.length; // cached length so it doesn't have to
										// be looked up in loop
				for (int i = 0; i < len; i++) {
					if (files[i].isDirectory()) {
						deleteDir(files[i]);
					} else {
						files[i].delete();
					}
				}
				dir.delete();
			}
		} catch (Exception e) {
		}
	}

	public static String makePathOSIndependent(final String path) {
		String separator = System.getProperty("file.separator");
		return path.replace(separator.charAt(0), '/');
	}

	public static boolean isWindows() {
		return System.getProperty("os.name").toLowerCase().startsWith("windows");
	}

	public static boolean isWindowsNT() {
		return System.getProperty("os.name").equals("Windows NT");
	}

	public static boolean isWindows95() {
		return System.getProperty("os.name").equals("Windows 95");
	}

	public static String[] getCommandTokens(String commandString) {
		boolean escaped = false;
		ArrayList<String> tokens = new ArrayList<String>();
		int length = commandString.length();
		int startIx = 0;
		int endIx = length;
		final char NONE = 'x';
		char startChar = NONE;
		for (int i = 0; i < length; i++) {
			char c = commandString.charAt(i);
			if (c == '\\') {
				escaped = !escaped;
			}
			if (!escaped) {
				if (c == ' ' && startChar == NONE) {
					endIx = i;
					if (startIx != endIx) // Happens just after quote
						tokens.add(commandString.substring(startIx, endIx + 1).trim());
					startChar = NONE; // reset
					startIx = i + 1;
				}
				if (c == '"' || c == '\'') {
					if (startChar == NONE) {
						startChar = c;
						startIx = i;
					} else {
						if (c == startChar) {
							endIx = i;
							tokens.add(commandString.substring(startIx+1 , endIx).trim());
							startChar = NONE; // reset
							startIx = i + 1;
						}
					}
				}
			}
			if (c != '\\') {
				escaped = false;
			}
		}
		if (startIx < length) {
			tokens.add(commandString.substring(startIx, length).trim());
		}
		return (String[]) tokens.toArray(new String[0]);
	}

	public static String getStackTraceString(Exception e) {
		return getStackTraceString(e, false);
	}

	public static String getStackTraceString(Exception e, boolean forHTML) {
		ByteArrayOutputStream b = new ByteArrayOutputStream();
		PrintStream s = new PrintStream(b);
		e.printStackTrace(s);
		String str = b.toString();
		if (forHTML)
			str = str.replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll("\\\r", "").replaceAll("\\\n", "<br/>");
		return str;
	}

	public static String getAbsolutePath(String fileStr) {
		return getAbsolutePath(new File(fileStr));
	}

	public static String getAbsolutePath(File file) {
		String path = "";
		try {
			path = file.getCanonicalPath();
		} catch (IOException e) {
			path = file.getAbsolutePath();
		}
		return path;
	}

	public static String getUUID() {
		return UUID.randomUUID().toString().replace('-', '0');
	}

	public static String generateId() {
//		return "SAHI_HARDCODED_ID";
		return "sahi_" + getUUID();
	}

	public static String toJSON(String[] list) {
		StringBuffer sb = new StringBuffer();
		sb.append("[");
		for (int i = 0; i < list.length; i++) {
			sb.append("\"");
			sb.append(Utils.makeString(list[i]));
			sb.append("\"");
			if (i != list.length - 1)
				sb.append(",");
		}
		sb.append("]");
		return sb.toString();
	}
	
	public static String executeCommand(final String command, boolean isSync, long timeout) throws Exception{
		final RunnableWithResult runnable = new RunnableWithResult(command);
		final Thread thread = new Thread(runnable);
		thread.start();
		if (isSync) thread.join(timeout);
		return runnable.getResult();
	}
	
	public static String executeCommand(String[] command) throws Exception {
		StringBuffer sb = new StringBuffer();
		Process p = Runtime.getRuntime().exec(command);
		InputStream stdInput = p.getInputStream();
		InputStream stdError = p.getErrorStream();
		StringBuffer inBuffer = new StringBuffer();
		StringBuffer errBuffer = new StringBuffer();
		Thread inThread = new Thread(new StreamReader(stdInput, inBuffer));
		inThread.start();
		Thread errThread = new Thread(new StreamReader(stdError, errBuffer));
		errThread.start();
		p.waitFor();
		inThread.join();
		errThread.join();
		sb.append(inBuffer);
		sb.append(errBuffer);
		return sb.toString();
	}
	
	public static Process executeAndGetProcess(String[] command) throws Exception {
		Process p = Runtime.getRuntime().exec(command);
		InputStream stdInput = p.getInputStream();
		InputStream stdError = p.getErrorStream();
		StringBuffer inBuffer = new StringBuffer();
		StringBuffer errBuffer = new StringBuffer();
		Thread inThread = new Thread(new StreamReader(stdInput, inBuffer));
		inThread.start();
		Thread errThread = new Thread(new StreamReader(stdError, errBuffer));
		errThread.start();
		return p;
	}

	public static String encode(String s) {
		try {
			return URLEncoder.encode(s, "UTF8");
		} catch (Exception e) {
			e.printStackTrace();
			return s;
		}
	}	
	
	public static void sleep(long time){
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public static String escapeForXML(String message) {
		if (message == null) return null;
		return message.replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;")
					  .replaceAll("\"", "&quot;").replaceAll("'", "&#039;");
	}

	public static byte[] getBytes(String dataStr) {
		try {
			return dataStr.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			return dataStr.getBytes();
		}
	}

	public static boolean isSahiTestFile(String testName) {
		return testName.endsWith(".sah") || testName.endsWith(".sahi");
	}

	public static HashMap<String, String> parseCLInput(String[] args) {
		HashMap<String, String> map = new HashMap<String, String>();
		for (int i = 0; i < args.length; i++) {
			if (args[i].indexOf("-") == 0){
				if (i+1 < args.length){
					final String key = args[i].substring(1);
					final String value = args[i+1];
					System.out.println(key+ "=" + value);
					map.put(key, value);
					i++;
				}
			}
		}
		return map;
	}

	public static String expandSystemProperties(String cmd) {
		return substitute(cmd, System.getenv());
	}

	public static String getOSFamily() {
		final String osName = "" + System.getProperty("os.name").toLowerCase();
		if (osName.contains("windows"))
			return System.getenv("PROGRAMFILES(X86)") != null ? "win64" : "win32";
		if (osName.contains("mac")) return "mac";
		return "linux";
	}
	
	public static String replaceLocalhostWithMachineName(String url){
		String computername;
		try {
			computername = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			return url;
		}
		return url.replace("localhost", computername);
	}
}
class StreamReader implements Runnable{
	private final InputStream in;
	private final StringBuffer data;
	StreamReader(InputStream in, StringBuffer data){
		this.in = in;
		this.data = data;
	}
	public void run(){
		try {
			data.append(new String(Utils.getBytes(in)));
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}
}

class RunnableWithResult implements Runnable {
	private final String command;
	String result = "RUNNING";

	RunnableWithResult(String command) {
		this.command = command;
	}

	@Override
	public void run() {
		try {
			result = Utils.executeCommand(Utils.getCommandTokens(command));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String getResult(){
		return result;
	}
}
