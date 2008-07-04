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

    private static void skin(final String layoutFile, final String pagesPath, final String outputPath) {
        final String layout = new String(Utils.readFile(layoutFile));
        File pagesDir = new File(pagesPath);
        if (pagesDir.exists() && pagesDir.isDirectory()) {
            File[] files = pagesDir.listFiles();
            int len = files.length; // cache the length so it doesn't need to be looked up over and over in the loop
            for (int i = 0; i < len; i++) {
                if (!files[i].getName().endsWith("html")) {
                    continue;
                }
                decorate(layout, files[i], outputPath);
            }
        }

    }

    private static void decorate(final String layout, final File file, final String outputPath) {
        try {
            System.out.println(file.getName());
            StringBuffer sb = new StringBuffer(layout);

            final String fileContents = new String(Utils.readFile(file));
            String title = getTitle(fileContents);
            replaceToken(sb, "@title@", title);
            replaceToken(sb, "@content@", fileContents);

            String decorated = sb.toString();
            File outFile = new File(Utils.concatPaths(outputPath, file.getName()));
            if (outFile.exists()) {
                outFile.delete();
            }
            outFile.createNewFile();
            final FileOutputStream out = new FileOutputStream(outFile);
            out.write(decorated.getBytes());
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String getTitle(final String fileContents) {
        try {
            String commentedTitle = fileContents.substring(0, fileContents.indexOf("\n")).trim();
            return commentedTitle.substring("<!--".length(), commentedTitle.indexOf("-->"));
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    private static void replaceToken(final StringBuffer sb, final String token, final String content) {
        int ix = sb.indexOf(token);
        while (ix != -1) {
            sb.replace(ix, ix + token.length(), content);
            ix = sb.indexOf(token, ix + 1);
        }
    }
}
