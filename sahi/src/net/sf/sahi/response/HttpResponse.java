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

    public String status(){
        int ix = firstLine.indexOf(" ");
        return firstLine.substring(ix, firstLine.indexOf(" ", ix +1)).trim();
    }
}
