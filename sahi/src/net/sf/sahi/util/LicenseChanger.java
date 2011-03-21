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

public class LicenseChanger {

    private static final String LICENSE_FILE_PATH = "D:\\kamlesh\\sahi\\trunk\\config\\license.txt";
    private static final String LICENSE_FILE_CHANGE = "D:\\kamlesh\\sahi\\trunk\\htdocs";

    public static void changeLicense(final File f) throws IOException {
        if (f.isDirectory()) {
            File[] files = f.listFiles();
            int len = files.length;
            for (int i = 0; i < len; i++) {
                File file = files[i];
                changeLicense(file);
            }
        } else if (f.getName().endsWith(".java") || f.getName().endsWith(".js")) {
            String s = new String(Utils.readFile(f));
            if (s.indexOf("Copyright  2006  V Narayan Raman") != -1 && s.indexOf("LicenseChanger") == -1) {
                System.out.println(f.getName());
                int startIx = s.indexOf("/**");
                if (startIx == -1) {
                    return;
                }
                int endIx = s.indexOf("*/");
                StringBuilder sb = new StringBuilder();
                sb.append(s.substring(0, startIx));
                sb.append(new String(Utils.readFile(LICENSE_FILE_PATH)));
                sb.append(s.substring(endIx + 2));
                //String newS = s.replaceAll("[$]GPL_License[$]", new String(Utils.readFile("C:\\my\\sahi\\config\\gpl_inc.txt")));
                FileOutputStream fos = new FileOutputStream(f);
                fos.write(sb.toString().getBytes());
                fos.close();
            }
        }
    }

    public static void main(String[] args) throws IOException {
        LicenseChanger.changeLicense(new File(LICENSE_FILE_CHANGE));
    }
}
