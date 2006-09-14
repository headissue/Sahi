/**
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

/**
 * User: nraman
 * Date: May 18, 2005
 * Time: 11:04:06 PM
 */
public class SimpleHttpResponse extends HttpResponse {
    public SimpleHttpResponse(String dataStr) {
        this(dataStr.getBytes());
    }

    public SimpleHttpResponse(String dataStr, boolean closeConnection) {
        this(dataStr.getBytes(), closeConnection);
    }

    public SimpleHttpResponse(byte[] data) {
    	this(data, true);
    }

    public SimpleHttpResponse(byte[] data, boolean closeConnection) {
        setData(data);
        setFirstLine("HTTP/1.1 200 OK");
        setHeader("Content-Type", "text/html");
		setHeader("Cache-Control","no-cache");
		setHeader("Pragma","no-cache");
		setHeader("Expires", "0");
        if (closeConnection) {
        	setHeader("Connection", "close");
        }
        setHeader("Content-Length", "" + data().length);
        setRawHeaders(getRebuiltHeaderBytes());
    }
}
