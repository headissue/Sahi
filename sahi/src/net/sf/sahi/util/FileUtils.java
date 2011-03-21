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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

public class FileUtils {

    public static void copyDir(final String src, final String dest) throws IOException, InterruptedException {
        copyDir(new File(src), new File(dest));
    }

    public static void copyDir(final File src, final File dest) throws IOException {
        dest.mkdirs();
        File[] files = src.listFiles();

        int j = files.length;   // cache the length so it doesn't need to be looked up over and over in the loop
        for (int i = 0; i < j; i++) {
            File file = files[i];
            if (file.isDirectory()) {
                copyDir(file, new File(dest, file.getName()));
            } else {
                copyFile(file, new File(dest, file.getName()));
            }
        }
    }

    public static void copyFile(final String src, final String dest) throws IOException {
        copyFile(new File(src), new File(dest));
    }

	public static boolean renameFile(String oldPath, String newPath) {
		File oldFile = new File(oldPath);
		File newFile = new File(newPath);
		if (newFile.exists()) newFile.delete();
		return oldFile.renameTo(newFile);
	}    
    
    public static void copyFile(final File src, final File dest) throws IOException {
        dest.getParentFile().mkdirs();
        dest.createNewFile();

        FileChannel sourceChannel = new FileInputStream(src).getChannel();
        FileChannel targetChannel = new FileOutputStream(dest).getChannel();
        sourceChannel.transferTo(0, sourceChannel.size(), targetChannel);
        sourceChannel.close();
        targetChannel.close();
    }

    public static String cleanFileName(String fileName) {
    	if (fileName == null) return fileName;
		return fileName.replaceAll("[\\\\/:*?\"<>|]", "");
	}
}
