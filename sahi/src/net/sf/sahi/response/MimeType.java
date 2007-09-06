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

package net.sf.sahi.response;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class MimeType {
    private static Properties properties;

    static {
        properties = new Properties();
        try {
            properties.load(new FileInputStream("../config/mime-types.mapping"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String get(String fileExtension, String defaultValue) {
        fileExtension = fileExtension == null ? "" : fileExtension.toLowerCase();
        return properties.getProperty(fileExtension, defaultValue);
    }

    public static String getMimeTypeOfFile(String fileName) {
        return getMimeTypeOfFile(fileName, "text/plain");
    }

    public static String getMimeTypeOfFile(String fileName, String defaultValue) {
        return get(getExtension(fileName), defaultValue);
    }


    static String getExtension(String fileName) {
        int ix = fileName.lastIndexOf('.');
        if (ix == -1) return "";
        return fileName.substring(ix);
    }
}
