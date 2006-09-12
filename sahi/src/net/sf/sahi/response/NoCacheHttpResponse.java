/**
 * Copyright V Narayan Raman
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
        setHeader("Connection", "close");
        setHeader("Content-Length", "" + data().length);
        setRawHeaders(getRebuiltHeaderBytes());
        logger.fine(new String(rawHeaders()));
    }

	public NoCacheHttpResponse(HttpResponse httpResponse) {
		setNoCacheHeaders(httpResponse.data());
    }
}
