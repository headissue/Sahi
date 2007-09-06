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

import net.sf.sahi.StreamHandler;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;

/**
 * User: nraman
 * Date: May 13, 2005
 * Time: 10:25:31 PM
 */
public class HttpResponse extends StreamHandler {
    private static final Logger logger = Logger.getLogger("net.sf.sahi.response.HttpResponse");
    protected HttpResponse(){
    }

    public HttpResponse(InputStream in) throws IOException {
        populateHeaders(in, true);
        populateData(in);
        logger.fine("First line:"+firstLine());
    }

    public String contentType() {
        return getLastSetValueOfHeader("Content-Type");
    }

    public void keepAlive(boolean keepAliveEnabled) {
        setFirstLine(firstLine().replaceAll("HTTP/1.0", "HTTP/1.1"));
        removeHeader("Content-length");
        setHeader("Content-Length", "" + data().length);
        removeHeader("Connection");
        removeHeader("Accept-ranges");
        setHeader("Connection", keepAliveEnabled ? "Keep-Alive" : "close");
        resetRawHeaders();
    }

    public void proxyKeepAlive(boolean keepAliveEnabled) {
        removeHeader("Content-length");
        setHeader("Content-Length", "" + data().length);
        removeHeader("Connection");
        removeHeader("Accept-ranges");
        setHeader("Proxy-Connection", keepAliveEnabled ? "Keep-Alive" : "close");
        resetRawHeaders();
    }

    public String status(){
        int ix = firstLine.indexOf(" ");
        return firstLine.substring(ix, firstLine.indexOf(" ", ix +1)).trim();
    }
}
