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

package net.sf.sahi.command;

import net.sf.sahi.request.HttpRequest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class Debug {
    public void toOut(HttpRequest request) {
        String msg = request.getParameter("msg");
        System.out.println(msg);
    }

    public void toErr(HttpRequest request) {
        String msg = request.getParameter("msg");
        System.err.println(msg);
    }

    public void toFile(HttpRequest request) {
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
