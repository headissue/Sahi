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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
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
			setData(substitute(new String(data()), substitutions).getBytes());
		}
		this.addCacheHeader = addCacheHeader;
		setHeaders();
	}

	static String substitute(String content, Properties substitutions) {
		Enumeration keys = substitutions.keys();
		while (keys.hasMoreElements()) {
			String key = (String) keys.nextElement();
			content = content.replaceAll("\\$" + key, substitutions
					.getProperty(key));
		}
		return content;
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
        setHeader("Connection","keep-alive");
		setHeader("Content-Length", "" + data().length);
		setRawHeaders(getRebuiltHeaderBytes());
	}

	static String formatForExpiresHeader(Date date) {
		return new SimpleDateFormat("EEE, dd MMM yyyy hh:mm:ss z").format(date);
	}
}
