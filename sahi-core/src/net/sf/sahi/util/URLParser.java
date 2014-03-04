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

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import net.sf.sahi.config.Configuration;
import net.sf.sahi.command.Command;

public class URLParser {

    public static String logFileNamefromURI(String uri) {
    	try {
			uri = URLDecoder.decode(uri, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
        String fileName = getRelativeLogFile(uri);
        if ("".equals(fileName)) {
            return "";
        }
        return Configuration.appendLogsRoot(fileName);
    }

    static String getRelativeLogFile(final String uri) {
        String fileName = uri.substring(uri.indexOf(Command.LOG_VIEW) + Command.LOG_VIEW.length());
        while (fileName.startsWith("/")) {
            fileName = fileName.substring(1);
        }
        return fileName;
    }

    public static String fileNamefromURI(final String uri) {
    	int questionIndex = uri.indexOf("?");
    	if (questionIndex == -1) questionIndex = uri.length();
        return Utils.concatPaths(Configuration.getHtdocsRoot(), uri.substring(uri.indexOf("_s_/") + 4, questionIndex));
    }

    public static String scriptFileNamefromURI(final String uri, final String token) {
        int endIndex = uri.indexOf("?");
        endIndex = endIndex == -1 ? uri.length() : endIndex;
        String fileName = uri.substring(uri.lastIndexOf(token) + token.length(), endIndex);
        return Utils.concatPaths(Configuration.getScriptRoots()[0], fileName); //TODO FIX ME
    }

    public static String getCommandFromUri(final String uri, String initialToken) {
        int ix1 = uri.indexOf(initialToken);
        if (ix1 == -1) {
            return null;
        }
        ix1 = ix1 + initialToken.length();
        int ix2 = uri.indexOf("/", ix1);
        int ix3 = uri.indexOf("?", ix1);
        int endIx = -1;
        if (ix2 > -1 && ix3 == -1) {
            endIx = ix2;
        } else if (ix3 > -1 && ix2 == -1) {
            endIx = ix3;
        } else {
            endIx = ix3 < ix2 ? ix3 : ix2;
        }
        if (endIx == -1) {
            endIx = uri.length();
        }
        return uri.substring(ix1, endIx);
    }
}
