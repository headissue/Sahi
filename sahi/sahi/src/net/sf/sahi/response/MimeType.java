package net.sf.sahi.response;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class MimeType {
	private static Properties properties;
	static {
		properties = new Properties();
		try {
			properties.load(new FileInputStream("../config/mime-types.mapping"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static String get(String fileExtension) {
		fileExtension = fileExtension == null ? "" : fileExtension.toLowerCase();
		return properties.getProperty(fileExtension, "text/plain");
	}

	
	public static String getMimeTypeOfFile(String fileName) {
		return get(getExtension(fileName));
	}


	static String getExtension(String fileName) {
		int ix = fileName.lastIndexOf('.');
		if (ix == -1) return "";
		return fileName.substring(ix);
	}
}
