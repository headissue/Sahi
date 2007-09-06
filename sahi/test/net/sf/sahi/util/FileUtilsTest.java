package net.sf.sahi.util;

import junit.framework.TestCase;

import java.io.IOException;
import java.io.File;
import java.io.FileOutputStream;

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

public class FileUtilsTest extends TestCase {
    String src = "../temp/copysrc/";
    String dest = "../temp/copydest/";
    String file1 = "a.txt";
    String file2 = "nested/b.txt";
    public String content = "Some text";


    protected void setUp() throws Exception {
        new File(src).mkdirs();
        new File(src+"nested").mkdirs();

        new File(src+file1).createNewFile();
        new File(src+file2).createNewFile();

        File srcFile = new File(src + file1);
        srcFile.createNewFile();

        FileOutputStream out = new FileOutputStream(srcFile);
        out.write(content.getBytes());
        out.close();
    }

    public void testCopyDir() throws IOException, InterruptedException {
        FileUtils.copyDir(src, dest);
        assertTrue(new File(dest + file1).exists());
        assertTrue(new File(dest + file2).exists());

    }


    public void testCopyFile() throws IOException {
        String destFile = dest + file1;
        FileUtils.copyFile(src+file1, destFile);
        assertTrue(new File(destFile).exists());
        assertEquals(content, new String(Utils.readFile(destFile)));
    }


    protected void tearDown() throws Exception {
        new File(src+file1).delete();
        new File(src+file2).delete();
        new File(dest+file1).delete();
        new File(dest+file2).delete();

        new File(src+"/nested").delete();
        new File(src).delete();
        new File(dest+"/nested").delete();
        new File(dest).delete();
    }
}
