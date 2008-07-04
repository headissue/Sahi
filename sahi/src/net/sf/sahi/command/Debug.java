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
package net.sf.sahi.command;

import net.sf.sahi.request.HttpRequest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class Debug {

    public void toOut(final HttpRequest request) {
        String msg = request.getParameter("msg");
        System.out.println(msg);
    }

    public void toErr(final HttpRequest request) {
        String msg = request.getParameter("msg");
        System.err.println(msg);
    }

    public void toFile(final HttpRequest request) {
        String msg = request.getParameter("msg");
        try {
            File file = new File(request.getParameter("file"));
            if (!file.exists()) {
                file.createNewFile();
            }
            FileOutputStream out;
            out = new FileOutputStream(file, true);
            out.write((msg + "\n").getBytes());
            out.close();
        } catch (IOException e) {
            System.out.println(msg);
        }
    }
}
