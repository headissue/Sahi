package net.sf.sahi;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Properties;
import java.util.logging.Logger;

import javax.net.ssl.SSLSocket;

import net.sf.sahi.config.Configuration;
import net.sf.sahi.request.HttpRequest;
import net.sf.sahi.response.HttpFileResponse;
import net.sf.sahi.response.HttpModifiedResponse;
import net.sf.sahi.response.HttpResponse;
import net.sf.sahi.ssl.SSLHelper;
import net.sf.sahi.util.SocketPool;

/**
 * User: nraman Date: May 13, 2005 Time: 7:06:11 PM To
 */
public class ProxyProcessor implements Runnable {
	private Socket client;

	private boolean isSSLSocket = false;

	private static Logger logger = Configuration.getLogger("net.sf.sahi.ProxyProcessor");

	private static boolean externalProxyEnabled = Configuration.isExternalProxyEnabled();;

	private static String externalProxyHost = null;

	private static int externalProxyPort = 80;

	private static SocketPool socketPool = new SocketPool(20);

	static {
		if (externalProxyEnabled) {
			externalProxyHost = Configuration.getExternalProxyHost();
			externalProxyPort = Configuration.getExternalProxyPort();
			logger.config("External Proxy is enabled for Host:" + externalProxyHost + " and Port:"
					+ externalProxyPort);
		} else {
			logger.config("External Proxy is disabled");
		}
	}

	public ProxyProcessor(Socket client) {
		this.client = client;
		isSSLSocket = (client instanceof SSLSocket);
	}

	public void run() {
		try {
			HttpRequest requestFromBrowser = getRequestFromBrowser();
			String uri = requestFromBrowser.uri();
			if (uri != null) {
				if (uri.indexOf("/_s_/") != -1) {
					processLocally(uri, requestFromBrowser);
				} else {
					if (isHostTheProxy(requestFromBrowser.host())
							&& requestFromBrowser.port() == Configuration.getPort()) {
						processLocally(uri, requestFromBrowser);
					} else if (uri.indexOf("favicon.ico") != -1) {
						sendResponseToBrowser(new HttpFileResponse(Configuration.getHtdocsRoot()
								+ "spr/favicon.ico"), false);
					} else {
						processAsProxy(requestFromBrowser);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.warning(e.getMessage());
		} finally {
			try {
				client.close();
			} catch (IOException e) {
				logger.warning(e.getMessage());
			}
		}
	}

	private boolean isHostTheProxy(String host) throws UnknownHostException {
		try {
			return InetAddress.getByName(host).getHostAddress().equals(
					InetAddress.getLocalHost().getHostAddress())
					|| InetAddress.getByName(host).getHostAddress().equals("127.0.0.1");
		} catch (Exception e) {
			return false;
		}
	}

	private void processAsProxy(HttpRequest requestFromBrowser) throws IOException {
		if (requestFromBrowser.isConnect()) {
			processConnect(requestFromBrowser);
		} else {
			processHttp(requestFromBrowser);
		}
	}

	private void processConnect(HttpRequest requestFromBrowser) {
		try {
			client.getOutputStream().write(("HTTP/1.0 200 OK\r\n\r\n").getBytes());
			SSLSocket sslSocket = new SSLHelper().convertToSecureServerSocket(client,
					requestFromBrowser.host());
			ProxyProcessor delegatedProcessor = new ProxyProcessor(sslSocket);
			delegatedProcessor.run();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void processHttp(HttpRequest requestFromBrowser) {
		logger.finest("### Type of socket is " + client.getClass().getName());
		Socket socketToHost = null;
		try {
			try {
				socketToHost = getSocketToHost(requestFromBrowser);
			} catch (IOException e) {
				sendCannotConnectResponse(e);
				return;
			}
			socketToHost.setSoTimeout(120000);
			OutputStream outputStreamToHost = socketToHost.getOutputStream();
			InputStream inputStreamFromHost = socketToHost.getInputStream();
			HttpResponse responseFromHost = getResponseFromHost(inputStreamFromHost,
					outputStreamToHost, requestFromBrowser, true);
			socketPool.release(socketToHost);
			sendResponseToBrowser(responseFromHost, true);
		} catch (Exception ioe) {
		}
	}

	private void sendCannotConnectResponse(IOException e) throws IOException {
		Properties props = new Properties();
		props.put("message", e.getMessage());
		final HttpFileResponse httpFileResponse = new HttpFileResponse(Configuration
				.getHtdocsRoot()
				+ "spr/cannotConnect.htm", props, false, true);
		final HttpResponse modifiedResponse = HttpModifiedResponse.modify(httpFileResponse);
		sendResponseToBrowser(modifiedResponse, true);
		e.printStackTrace();
	}

	private void processLocally(String uri, HttpRequest requestFromBrowser) throws IOException {
		HttpResponse httpResponse = new LocalRequestProcessor().getLocalResponse(uri,
				requestFromBrowser);
		sendResponseToBrowser(httpResponse, false);
	}

	private HttpResponse getResponseFromHost(InputStream inputStreamFromHost,
			OutputStream outputStreamToHost, HttpRequest request, boolean modify)
			throws IOException {
		// if (modify)
		request.modifyForFetch();
		logger.finest(request.uri());
		logger.finest(new String(request.rawHeaders()));
		outputStreamToHost.write(request.rawHeaders());
		if (request.isPost())
			outputStreamToHost.write(request.data());
		outputStreamToHost.flush();
		HttpResponse response;
		if (modify) {
			response = new HttpModifiedResponse(new HttpResponse(inputStreamFromHost), request
					.isSSL(), request.fileExtension());
		} else {
			response = new HttpResponse(inputStreamFromHost);
		}
		logger.finest(new String(response.rawHeaders()));
		return response;
	}

	private Socket getSocketToHost(HttpRequest request) throws IOException {
		if (request.isSSL()) {
			return new SSLHelper().getSocket(request, InetAddress.getByName(request.host()));
		} else {
			if (externalProxyEnabled) {
				Socket socket = socketPool.get(externalProxyHost, externalProxyPort);
				socket.setReuseAddress(true);
				return socket;
			}else {
				Socket socket = socketPool.get(request.host(), request.port());
				socket.setReuseAddress(true);
				return socket; 
			}
		}
	}

	private HttpRequest getRequestFromBrowser() throws IOException {
		InputStream in = client.getInputStream();
		return new HttpRequest(in, isSSLSocket);
	}

	protected void sendResponseToBrowser(HttpResponse responseFromHost, boolean wait) throws IOException {
		OutputStream outputStreamToBrowser = new BufferedOutputStream(client.getOutputStream());
		outputStreamToBrowser.write(responseFromHost.rawHeaders());
		outputStreamToBrowser.flush();
		outputStreamToBrowser.flush();
		final byte[] data = responseFromHost.data();
		outputStreamToBrowser.write(data);
		outputStreamToBrowser.flush();
		if (wait) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		outputStreamToBrowser.close();
	}

	protected Socket client() {
		return client;
	}
}
