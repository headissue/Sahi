package com.sahi;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.logging.Logger;

import com.sahi.config.Configuration;
import com.sahi.request.HttpRequest;
import com.sahi.response.HttpFileResponse;
import com.sahi.response.HttpResponse;
import com.sahi.response.NoCacheHttpResponse;
import com.sahi.util.FileIsDirectoryException;
import com.sahi.util.FileNotFoundRuntimeException;
;

/**
 * User: nraman Date: May 13, 2005 Time: 7:06:11 PM To
 */
public class WebProcessor implements Runnable {
	private Socket client;
	private static Logger logger = Configuration
			.getLogger("com.sahi.WebProcessor");

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
			sendResponseToBrowser(new HttpFileResponse(fileName));
		} catch (FileIsDirectoryException dirEx) {
			try {
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
