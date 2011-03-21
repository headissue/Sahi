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
package net.sf.sahi.response;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import net.sf.sahi.config.Configuration;

public class MimeType {

    private static Properties properties;
    

    static {
        properties = new Properties();
        try {
            properties.load(new FileInputStream(Configuration.getMimeTypesMappingFile()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String get(String fileExtension, final String defaultValue) {
        fileExtension = (fileExtension == null ? "" : fileExtension.toLowerCase());
        return properties.getProperty(fileExtension, defaultValue);
    }

    public static String getMimeTypeOfFile(final String fileName) {
        return getMimeTypeOfFile(fileName, "text/plain");
    }

    public static String getMimeTypeOfFile(final String fileName, final String defaultValue) {
        return get(getExtension(fileName), defaultValue);
    }

    static String getExtension(final String fileName) {
        int ix = fileName.lastIndexOf('.');
        if (ix == -1) {
            return "";
        }
        return fileName.substring(ix);
    }
}
