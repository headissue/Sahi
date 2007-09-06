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

public class LicenseChanger {
    public static void changeLicense(File f) throws IOException {
        if (f.isDirectory()){
            File[] files = f.listFiles();
            for (int i = 0; i < files.length; i++) {
                File file = files[i];
                changeLicense(file);
            }
        }
        else if (f.getName().endsWith(".java") || f.getName().endsWith(".htm")) {
            String s = new String(Utils.readFile(f));
            if (s.indexOf("Copyright  2006  V Narayan Raman") != -1 && s.indexOf("LicenseChanger") == -1){
                System.out.println(f.getName());
                int startIx = s.indexOf("<!--");
                if (startIx == -1) return;
                int endIx = s.indexOf("-->");
                StringBuffer sb = new StringBuffer();
                sb.append(s.substring(0, startIx));
                sb.append(new String(Utils.readFile("C:\\my\\sahi\\config\\gpl_inc_html.txt")));
                sb.append(s.substring(endIx + 2));
                //String newS = s.replaceAll("[$]GPL_License[$]", new String(Utils.readFile("C:\\my\\sahi\\config\\gpl_inc.txt")));
                FileOutputStream fos = new FileOutputStream(f);
                fos.write(sb.toString().getBytes());
                fos.close();
            }
        }
    }


    public static void main(String[] args) throws IOException {
        LicenseChanger.changeLicense(new File("c:\\my\\sahi\\htdocs"));
    }
}
