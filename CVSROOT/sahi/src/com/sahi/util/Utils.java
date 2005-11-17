package com.sahi.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * User: nraman Date: Jun 26, 2005 Time: 4:52:58 PM
 */
public class Utils {
	public static String escapeDoubleQuotesAndBackSlashes(String line) {
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

	public static String concatPaths(String s1, String s2) {
		boolean s1HasSlash = (s1.charAt(s1.length() - 1) == '/');
		boolean s2HasSlash = (s2.charAt(0) == '/');
		if (!s1HasSlash && !s2HasSlash)
			return s1 + '/' + s2;
		if (s1HasSlash && s2HasSlash)
			return s1 + s2.substring(1);
		return s1 + s2;
	}
}
