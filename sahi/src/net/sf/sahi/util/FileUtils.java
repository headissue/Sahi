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
