package net.sf.sahi.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class HTMLDecorator {
	public static void main(String args[]) {
		String layoutFile = args[0];
		String pagesPath = args[1];
		String outputPath = args[2];
		skin(layoutFile, pagesPath, outputPath);
	}

	private static void skin(String layoutFile, String pagesPath, String outputPath) {
		final String layout = new String(Utils.readFile(layoutFile));
		File pagesDir = new File(pagesPath);
		if (pagesDir.exists() && pagesDir.isDirectory()) {
			File[] files = pagesDir.listFiles();
			for (int i = 0; i < files.length; i++) {
				if (!files[i].getName().endsWith("html"))
					continue;
				decorate(layout, files[i], outputPath);
			}
		}

	}

	private static void decorate(String layout, File file, String outputPath) {
		try {
			System.out.println(file.getName());
			StringBuffer sb = new StringBuffer(layout);

			final String fileContents = new String(Utils.readFile(file));
			String title = getTitle(fileContents);
			replaceToken(sb, "@title@", title);
			replaceToken(sb, "@content@", fileContents);

			String decorated = sb.toString();
			File outFile = new File(Utils.concatPaths(outputPath, file.getName()));
			if (outFile.exists())
				outFile.delete();
			outFile.createNewFile();
			final FileOutputStream out = new FileOutputStream(outFile);
			out.write(decorated.getBytes());
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static String getTitle(String fileContents) {
		try {
			String commentedTitle = fileContents.substring(0, fileContents.indexOf("\n")).trim();
			return commentedTitle.substring("<!--".length(), commentedTitle.indexOf("-->"));
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}

	private static void replaceToken(StringBuffer sb, String token, String content) {
		int ix = sb.indexOf(token);
		while (ix != -1) {
			sb.replace(ix, ix + token.length(), content);
			ix = sb.indexOf(token, ix+1);
		}
	}
}
