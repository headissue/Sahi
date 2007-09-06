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

package net.sf.sahi;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.logging.Logger;

import net.sf.sahi.config.Configuration;
import net.sf.sahi.request.HttpRequest;
import net.sf.sahi.response.HttpFileResponse;
import net.sf.sahi.response.HttpResponse;
import net.sf.sahi.response.NoCacheHttpResponse;
import net.sf.sahi.util.FileIsDirectoryException;
import net.sf.sahi.util.FileNotFoundRuntimeException;

/**
 * User: nraman Date: May 13, 2005 Time: 7:06:11 PM To
 */
public class WebProcessor implements Runnable {
	private Socket client;
	private static Logger logger = Configuration
			.getLogger("net.sf.sahi.WebProcessor");

	public WebProcessor(Socket client) {
		this.client = client;
	}

	public void run() {
		String uri = null;
		try {
			HttpRequest requestFromBrowser = getRequestFromBrowser();
			uri = requestFromBrowser.uri();
			if (uri.indexOf("/dyn/stopserver") != -1) {
				sendResponseToBrowser(new NoCacheHttpResponse(200, "OK", "Killing Server"));
				System.exit(1);
			}
			String fileName = fileNamefromURI(uri);
			sendResponseToBrowser(new HttpFileResponse(fileName, null, false, false));
		} catch (FileIsDirectoryException dirEx) {
			try {
				if ("/".equals(uri)) uri = "/demo";
				sendResponseToBrowser(new NoCacheHttpResponse(200, "OK", "<script>location.href='"+uri+"/index.htm'</script>"));
			} catch (IOException e) {
				logger.warning(dirEx.getMessage());
			}
			logger.warning(dirEx.getMessage());
		} catch (FileNotFoundRuntimeException fnfre) {
			try {
				sendResponseToBrowser(new NoCacheHttpResponse(404, "FileNotFound", "<html><h2>404 File Not Found</h2></html>"));
			} catch (IOException e) {
				logger.warning(fnfre.getMessage());
			}
			logger.warning(fnfre.getMessage());
		}
		catch (Exception e) {
			System.out.println(">>>>>>>>>>"+e.getClass().getName());
			logger.warning(e.getMessage());
		} finally {
			try {
				client.close();
			} catch (IOException e) {
				logger.severe(e.getMessage());
			}
		}
	}

	private String fileNamefromURI(String uri) {
		StringBuffer sb = new StringBuffer();
		sb.append(Configuration.getHtdocsRoot());
		sb.append(uri.substring(uri.indexOf("/")));
		logger.fine(sb.toString());
		return sb.toString();
	}

	private HttpRequest getRequestFromBrowser() throws IOException {
		InputStream in = client.getInputStream();
		return new HttpRequest(in);
	}

	protected void sendResponseToBrowser(HttpResponse responseFromHost)
			throws IOException {
		OutputStream outputStreamToBrowser = client.getOutputStream();
		outputStreamToBrowser.write(responseFromHost.rawHeaders());
		outputStreamToBrowser.write(responseFromHost.data());
	}

	protected Socket client() {
		return client;
	}
}
