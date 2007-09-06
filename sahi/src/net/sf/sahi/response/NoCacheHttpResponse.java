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

import java.util.logging.Logger;

/**
 * User: nraman
 * Date: May 18, 2005
 * Time: 11:04:06 PM
 */
public class NoCacheHttpResponse extends HttpResponse {
	private static final Logger logger = Logger.getLogger("net.sf.sahi.response.NoCacheHttpResponse");
	private int status = 200;
	private String statusMessage = "OK";

	public NoCacheHttpResponse() {
		this("");
    }

	public NoCacheHttpResponse(String dataStr) {
		setNoCacheHeaders(dataStr.getBytes());
    }

	public NoCacheHttpResponse(int status, String statusMessage, String dataStr) {
		this.status = status;
		if (status != 200) this.statusMessage = "";
		this.statusMessage = statusMessage == null ? "" : statusMessage;
		setNoCacheHeaders(dataStr.getBytes());
	}

    protected void setNoCacheHeaders(byte[] data) {
        setData(data);
		setFirstLine("HTTP/1.1 " + status + " " + statusMessage);
        setHeader("Content-Type", "text/html");
        setHeader("Cache-control", "no-store");
        setHeader("Pragma", "no-cache");
        setHeader("Expires", "-1");
        setHeader("Content-Length", "" + data().length);
        setRawHeaders(getRebuiltHeaderBytes());
        logger.fine(new String(rawHeaders()));
    }

	public NoCacheHttpResponse(HttpResponse httpResponse) {
		setNoCacheHeaders(httpResponse.data());
    }
}
