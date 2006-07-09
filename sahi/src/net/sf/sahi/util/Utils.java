package net.sf.sahi.util;

import java.io.*;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * User: nraman Date: Jun 26, 2005 Time: 4:52:58 PM
 */
public class Utils {
    public static String escapeDoubleQuotesAndBackSlashes(String line) {
        if (line == null) return null;
        return line.replaceAll("\\\\", "\\\\\\\\").replaceAll("\"", "\\\\\"");
    }

    public static byte[] getBytes(InputStream in) throws IOException {
        return getBytes(in, -1);
    }

    public static byte[] getBytes(InputStream in, int contentLength)
            throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int c = ' ';
        int count = 0;
        try {
            while ((contentLength == -1 || count < contentLength)
                    && (c = in.read()) != -1) {
                count++;
                out.write(c);
            }
        } catch (SocketTimeoutException ste) {
            ste.printStackTrace();
        }
        return out.toByteArray();
    }

    public static byte[] readURL(String url) {
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

    public static byte[] readCachedFile(String fileName) {
        if (!fileCache.containsKey(fileName)) {
            fileCache.put(fileName, readFile(fileName));
        }
        return (byte[]) fileCache.get(fileName);
    }

    public static byte[] readFile(String fileName) {
        File file = new File(fileName);
        return readFile(file);
    }

    public static byte[] readFile(File file) {
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
                if (inputStream != null)
                    inputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return data;
    }

    public static synchronized String createLogFileName(String scriptFileName) {
        scriptFileName = new File(scriptFileName).getName();
        String date = new SimpleDateFormat("ddMMMyyyy__HH_mm_ss")
                .format(new Date());
        return scriptFileName.replaceAll("[.].*$", "") + "__" + date;
    }

    public static File getRelativeFile(File parent, String s2) {
        File sf2 = new File(s2);
        if (sf2.isAbsolute()) return sf2;
        if (!parent.isDirectory()) parent = parent.getParentFile();
        File file = new File(parent, s2);
        return file;
    }

    public static String concatPaths(String s1, String s2) {
        File sf2 = new File(s2);
        if (sf2.isAbsolute()) return s2;
        File parent = new File(s1);
        if (!parent.isDirectory()) parent = parent.getParentFile();
        File file = new File(parent, s2);
        return file.getAbsolutePath();
    }

    public static ArrayList getTokens(String s) {
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

    public static boolean isBlankOrNull(String s) {
        return (s == null || "".equals(s));
    }

    public static String substitute(String content, Properties substitutions) {
        Enumeration keys = substitutions.keys();
        while (keys.hasMoreElements()) {
            String key = (String) keys.nextElement();
            content = content.replaceAll("\\$" + key, substitutions
                    .getProperty(key));
        }
        return content;
    }
}
