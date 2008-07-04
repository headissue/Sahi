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
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User: nraman Date: Jun 26, 2005 Time: 4:52:58 PM
 */
public class Utils {

    public static String escapeDoubleQuotesAndBackSlashes(final String line) {
        if (line == null) {
            return null;
        }
        return line.replaceAll("\\\\", "\\\\\\\\").replaceAll("\"", "\\\\\"");
    }

    public static byte[] getBytes(InputStream in) throws IOException {
        return getBytes(in, -1);
    }

    public static byte[] getBytes(final InputStream in, final int contentLength)
            throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int c = ' ';
        int count = 0;
        try {
            while ((contentLength == -1 || count < contentLength) && (c = in.read()) != -1) {
                count++;
                out.write(c);
            }
        } catch (SocketTimeoutException ste) {
            ste.printStackTrace();
        }
        return out.toByteArray();
    }

    public static byte[] readURL(final String url) {
        byte[] data = null;
        InputStream inputStream = null;
        try {
            inputStream = new URL(url).openStream();
            data = getBytes(inputStream, -1);
            inputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                inputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return data;
    }
    static Map fileCache = new HashMap();

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

    // Returns a string than has all its non-ASCII characters converted to its
    // ASCII
    // equivalent. Eg : By passing "Éléphant", the function would return
    // "Elephant".
    // Should consider refactoring to StringBuffer(); 
    public static String convertStringToASCII(String s) {
        return s.replaceAll("(è|é|ê|ë)", "e").replaceAll("(ù|ú|û|ü)", "u").replaceAll("(à|á|â|ã|ä|å)", "a").replaceAll("æ", "ae").replaceAll("(ì|í|î|ï)", "i").replaceAll("(ò|ó|ô|õ|ö|ø)", "o").replaceAll("(ý|ÿ)", "y").replaceAll("ñ", "n").replaceAll("ç",
                "c").replaceAll("(À|Á|Â|Ã|Ä|Å)", "A").replaceAll("Æ",
                "AE").replaceAll("Ç", "C").replaceAll("(È|É|Ê|Ë)", "E").replaceAll("(Ì|Í|Î|Ï)", "I").replaceAll("Ñ", "N").replaceAll(
                "(Ò|Ó|Ô|Õ|Ö|Ø)", "O").replaceAll("(Ù|Ú|Û|Ü)", "U").replaceAll("Ý", "Y");
    }

    public static synchronized String createLogFileName(final String scriptFileName) {
        String TMPscriptFileName = new File(scriptFileName).getName();
        String date = new SimpleDateFormat("ddMMMyyyy__HH_mm_ss").format(new Date());
        return convertStringToASCII(TMPscriptFileName.replaceAll("[.].*$", "") + "__" + date);
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

    public static String concatPaths(final String s1, final String s2) {
        File sf2 = new File(s2);
        if (sf2.isAbsolute()) {
            return s2;
        }
        File parent = new File(s1);
        if (!parent.isDirectory()) {
            parent = parent.getParentFile();
        }
        File file = new File(parent, s2);
        try {
            return file.getCanonicalPath();
        } catch (IOException e) {
            return file.getAbsolutePath();
        }
    }

    public static ArrayList getTokens(final String s) {
        ArrayList tokens = new ArrayList();
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

    public static String substitute(final String content, final Properties substitutions) {

        StringBuffer patternBuf = new StringBuffer();
        int i = 0;
        Enumeration keys = substitutions.keys();
        while (keys.hasMoreElements()) {
            String key = (String) keys.nextElement();
            patternBuf.append(i++ == 0 ? "" : "|").append("\\$").append(key);
        }

        Pattern pattern = Pattern.compile(patternBuf.toString());
        patternBuf = null;
        Matcher matcher = pattern.matcher(content);

        StringBuffer buf = new StringBuffer();
        while (matcher.find()) {
            String key = matcher.group(0).substring(1);
            String replaceStr = substitutions.getProperty(key).replaceAll(
                    "\\$", "SDLR");
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

    public static String stripChildSessionId(final String sessionId) {
        return sessionId.replaceFirst("sahix[0-9]+x", "");
    }

    public static void deleteDir(final File dir) {
        try {
            if (dir.exists()) {
                File[] files = dir.listFiles();
                int len = files.length; // cached length so it doesn't have to be looked up in loop
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
        return path.replace('/', separator.charAt(0));
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
        String[] ar = commandString.split("\"");
        ArrayList tokens = new ArrayList();
        int len = ar.length;
        for (int i = 0; i < len; i++) {
            if (commandString.indexOf("\"" + ar[i] + "\"") != -1) {
                tokens.add(ar[i]);
            } else {
                String[] subtokens = ar[i].split(" ");
                int length = subtokens.length; // cached length so it doesn't have to be looked up in loop
                for (int j = 0; j < length; j++) {
                    tokens.add(subtokens[j]);
                }
            }
        }
        return (String[]) tokens.toArray(new String[0]);
    }
}
