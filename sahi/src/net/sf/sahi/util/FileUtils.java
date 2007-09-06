package net.sf.sahi.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

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

public class FileUtils {
    public static void copyDir(String src, String dest) throws IOException, InterruptedException {
        copyDir(new File(src), new File(dest));
    }

    public static void copyDir(File src, File dest) throws IOException {
        dest.mkdirs();
        File[] files = src.listFiles();

        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            if (file.isDirectory()) copyDir(file, new File(dest, file.getName()));
            else copyFile(file, new File(dest, file.getName()));
        }
    }

    public static void copyFile(String src, String dest) throws IOException {
        copyFile(new File(src), new File(dest));
    }

    public static void copyFile(File src, File dest) throws IOException {
        dest.getParentFile().mkdirs();
        dest.createNewFile();

        FileChannel sourceChannel = new FileInputStream(src).getChannel();
        FileChannel targetChannel = new FileOutputStream(dest).getChannel();
        sourceChannel.transferTo(0, sourceChannel.size(), targetChannel);
        sourceChannel.close();
        targetChannel.close();
    }
}
