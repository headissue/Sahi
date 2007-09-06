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

/**
 * User: nraman
 * Date: May 18, 2005
 * Time: 11:04:06 PM
 */
public class SimpleHttpResponse extends HttpResponse {
    public SimpleHttpResponse(String dataStr) {
        this(dataStr.getBytes());
    }

    public SimpleHttpResponse(byte[] data) {
        setData(data);
        setFirstLine("HTTP/1.0 200 OK");
        setHeader("Content-Type", "text/html");
        setHeader("Cache-Control", "no-cache");
        setHeader("Pragma", "no-cache");
        setHeader("Expires", "0");
        setHeader("Content-Length", "" + data().length);
        setRawHeaders(getRebuiltHeaderBytes());
    }
}
