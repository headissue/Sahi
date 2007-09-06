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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import net.sf.sahi.config.Configuration;
import net.sf.sahi.util.Utils;

/**
 * User: nraman Date: May 15, 2005 Time: 10:14:34 PM
 */
public class HttpFileResponse extends HttpResponse {
    private String fileName;
    boolean addCacheHeader = false;

    public HttpFileResponse(String fileName, Properties substitutions, boolean addCacheHeader, boolean cacheFileInMemory) {
        this.fileName = fileName;
        byte[] bytes;
        if (cacheFileInMemory && !Configuration.isDevMode())
            bytes = Utils.readCachedFile(fileName);
        else
            bytes = Utils.readFile(fileName);
        setData(bytes);
        if (substitutions != null) {
            setData(Utils.substitute(new String(data()), substitutions).getBytes());
        }
        this.addCacheHeader = addCacheHeader;
        setHeaders();
    }


    public HttpFileResponse(String fileName) {
        this(fileName, null, false, false);
//		this(fileName, null, true, true);
    }

    private void setHeaders() {
        setFirstLine("HTTP/1.1 200 OK");
        setHeader("Content-Type", MimeType.getMimeTypeOfFile(fileName));
        if (addCacheHeader) {
            setHeader("Expires", formatForExpiresHeader(new Date(
                    System.currentTimeMillis() + 10 * 60 * 1000))); // 10 minutes
        }
        setHeader("Content-Length", "" + data().length);
        setRawHeaders(getRebuiltHeaderBytes());
    }

    static String formatForExpiresHeader(Date date) {
        return new SimpleDateFormat("EEE, dd MMM yyyy hh:mm:ss z").format(date);
    }
}
