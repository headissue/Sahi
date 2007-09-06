/**
 * Sahi - Web Automation and Test Tool
 *
 * Copyright  2006  V Narayan Raman
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
