package com.sahi;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.URLDecoder;
import java.net.UnknownHostException;
import java.util.Properties;
import java.util.logging.Logger;

import javax.net.ssl.SSLSocket;

import com.sahi.config.Configuration;
import com.sahi.playback.FileScript;
import com.sahi.playback.SahiScript;
import com.sahi.playback.SahiScriptHTMLAdapter;
import com.sahi.playback.ScriptUtil;
import com.sahi.playback.URLScript;
import com.sahi.playback.log.LogFileConsolidator;
import com.sahi.processor.SuiteProcessor;
import com.sahi.request.HttpRequest;
import com.sahi.response.HttpFileResponse;
import com.sahi.response.HttpModifiedResponse;
import com.sahi.response.HttpResponse;
import com.sahi.response.NoCacheHttpResponse;
import com.sahi.response.SimpleHttpResponse;
import com.sahi.session.Session;
import com.sahi.test.SahiTestSuite;
import com.sahi.util.Utils;

/**
 * User: nraman Date: May 13, 2005 Time: 7:06:11 PM To
 */
public class ProxyProcessor implements Runnable {
	private Socket client;

	private SuiteProcessor suiteProcessor = new SuiteProcessor();

	private boolean isSSLSocket = false;

	private static Logger logger = Configuration
			.getLogger("com.sahi.ProxyProcessor");

	private static boolean externalProxyEnabled = Configuration
			.isExternalProxyEnabled();;

	private static String externalProxyHost = null;

	private static int externalProxyPort = 80;

	static {
		if (externalProxyEnabled) {
			externalProxyHost = Configuration.getExternalProxyHost();
			externalProxyPort = Configuration.getExternalProxyPort();
			logger.config("External Proxy is enabled for Host:"
					+ externalProxyHost + " and Port:" + externalProxyPort);
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
							&& requestFromBrowser.port() == Configuration
									.getPort()) {
						processLocally(uri, requestFromBrowser);
					} else if (uri.indexOf("favicon.ico") != -1) {
						sendResponseToBrowser(new HttpFileResponse(
								Configuration.getHtdocsRoot()
										+ "spr/favicon.ico"));
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
					|| InetAddress.getByName(host).getHostAddress().equals(
							"127.0.0.1");
		} catch (Exception e) {
			return false;
		}
	}

	private void processAsProxy(HttpRequest requestFromBrowser)
			throws IOException {
		if (requestFromBrowser.isConnect()) {
			processConnect(requestFromBrowser);
		} else {
			processHttp(requestFromBrowser);
		}
	}

	private void processConnect(HttpRequest requestFromBrowser) {
		try {
			client.getOutputStream().write(
					("HTTP/1.0 200 Ok\r\n\r\n").getBytes());
			SSLSocket sslSocket = new SSLHelper()
					.convertToSecureServerSocket(client);
			ProxyProcessor delegatedProcessor = new ProxyProcessor(sslSocket);
			delegatedProcessor.run();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void processHttp(HttpRequest requestFromBrowser)
			throws IOException, SocketException {
		logger.finest("### Type of socket is " + client.getClass().getName());
		Socket socketToHost = null;
		try {
			socketToHost = getSocketToHost(requestFromBrowser);
		} catch (UnknownHostException e) {
			sendResponseToBrowser(new NoCacheHttpResponse(404, "UnknownHost",
					"<html><h2>Host " + e.getMessage()
							+ " Not Found</h2></html>"));
			return;
		}
		socketToHost.setSoTimeout(120000);
		OutputStream outputStreamToHost = socketToHost.getOutputStream();
		InputStream inputStreamFromHost = socketToHost.getInputStream();
		HttpResponse responseFromHost = getResponseFromHost(
				inputStreamFromHost, outputStreamToHost, requestFromBrowser
						.modifyForFetch());
		sendResponseToBrowser(responseFromHost);
		socketToHost.close();
	}

	private void processLocally(String uri, HttpRequest requestFromBrowser)
			throws IOException {
		if (uri.indexOf("/dyn/") != -1) {
			// System.out.println(uri);
			Session session = getSession(requestFromBrowser);
//			System.out.println("----------- " + session.id() + " " + uri);
			if (uri.indexOf("/log") != -1) {
				if (session.getScript() != null) {
					session.logPlayBack(requestFromBrowser.getParameter("msg"),
							requestFromBrowser.getParameter("type"),
							requestFromBrowser.getParameter("debugInfo"));
				}
				sendResponseToBrowser(new NoCacheHttpResponse(""));
			} else if (uri.indexOf("/setscriptfile") != -1) {
				String fileName = URLDecoder.decode(requestFromBrowser
						.getParameter("file"), "UTF8");
				session.setScript(new FileScript(
						getScriptFileWithPath(fileName)));
				startPlayback(requestFromBrowser, session);
			} else if (uri.indexOf("/setscripturl") != -1) {
				String url = URLDecoder.decode(requestFromBrowser
						.getParameter("url"), "UTF8");
				session.setScript(new URLScript(url));
				startPlayback(requestFromBrowser, session);
			} else if (uri.indexOf("/recordstart") != -1) {
				// System.out.println("########### "+session.id());
				startRecorder(requestFromBrowser, session);
				sendBlankResponse(session);
			} else if (uri.indexOf("/recordstop") != -1) {
				session.getRecorder().stop();
				sendBlankResponse(session);
			} else if (uri.indexOf("/record") != -1) {
				session.getRecorder().record(requestFromBrowser.getParameter("cmd"));
				sendResponseToBrowser(new NoCacheHttpResponse(""));
			} else if (uri.indexOf("/scriptslist") != -1) {
				sendResponseToBrowser(new NoCacheHttpResponse(ScriptUtil
						.getScriptsJs(getScriptName(session))));
			} else if (uri.indexOf("/script") != -1) {
				String s = (session.getScript() != null) ? session.getScript()
						.modifiedScript() : "";
				sendResponseToBrowser(new NoCacheHttpResponse(s));
			} else if (uri.indexOf("/winclosed") != -1) {
				session.setIsWindowOpen(false);
				sendResponseToBrowser(new NoCacheHttpResponse(""));
			} else if (uri.indexOf("/winopen") != -1) {
				session.setIsWindowOpen(true);
				sendBlankResponse(session);
			} else if (uri.indexOf("/state") != -1) {
				sendResponseToBrowser(proxyStateResponse(session));
			} else if (uri.indexOf("/setCommonCookie") != -1) {
				sendBlankResponse(session);
			} else if (uri.indexOf("/auto") != -1) {
				String fileName = URLDecoder.decode(requestFromBrowser
						.getParameter("file"), "UTF8");
				session.setScript(new FileScript(
						getScriptFileWithPath(fileName)));
				String startUrl = URLDecoder.decode(requestFromBrowser
						.getParameter("startUrl"), "UTF8");
				session.startPlayBack();
				sendResponseToBrowser(proxyAutoResponse(startUrl, session.id()));
			} else if (uri.indexOf("/setvar") != -1) {
				String name = requestFromBrowser.getParameter("name");
				String value = requestFromBrowser.getParameter("value");
				session.setVariable(name, value);
				sendResponseToBrowser(new NoCacheHttpResponse(""));
			} else if (uri.indexOf("/getvar") != -1) {
				String name = requestFromBrowser.getParameter("name");
				String value = session.getVariable(name);
				sendResponseToBrowser(new NoCacheHttpResponse(value != null
						? value
						: "null"));
			} else if (uri.indexOf("/startplay") != -1) {
				startPlayback(requestFromBrowser, session);
			} else if (uri.indexOf("/stopplay") != -1) {
				sendResponseToBrowser(new NoCacheHttpResponse(""));
				stopPlay(session);
			} else if (uri.indexOf("/startsuite") != -1) {
				suiteProcessor.startSuite(requestFromBrowser, session);
				sendResponseToBrowser(new NoCacheHttpResponse(""));
			} else if (uri.indexOf("/getSuiteStatus") != -1) {
				SahiTestSuite suite = SahiTestSuite.getSuite(session.id());
				String status = "NONE";
				if (suite != null) {
					status = session.getPlayBackStatus();
				}
				sendResponseToBrowser(new NoCacheHttpResponse(status));
			} else if (uri.indexOf("/stopserver") != -1) {
				System.exit(1);
			} else if (uri.indexOf("/getSahiScript") != -1) {
				String code = requestFromBrowser.getParameter("code");
				sendResponseToBrowser(new NoCacheHttpResponse(SahiScript
						.modifyFunctionNames(code)));
			} else if (uri.indexOf("/alert.htm") != -1) {
				String msg = requestFromBrowser.getParameter("msg");
				sendResponseToBrowser(proxyAlertResponse(msg));
			} else if (uri.indexOf("/confirm.htm") != -1) {
				String msg = requestFromBrowser.getParameter("msg");
				sendResponseToBrowser(proxyConfirmResponse(msg));
			} else if (uri.indexOf("/sleep") != -1) {
				long millis = 1000;
				try {
					millis = Long.parseLong(requestFromBrowser
							.getParameter("ms"));
				} catch (Exception e) {
				}
				try {
					Thread.sleep(millis);
				} catch (Exception e) {
				}
				sendResponseToBrowser(new SimpleHttpResponse(""));
			} else if (uri.indexOf("/currentscript") != -1) {
				if (session.getScript() != null) {
					sendResponseToBrowser(new SimpleHttpResponse("<pre>"
							+ SahiScriptHTMLAdapter.createHTML(session
									.getScript().getOriginal()) + "</pre>"));
				} else {
					sendResponseToBrowser(new SimpleHttpResponse(
							"No Script has been set for playback."));
				}
			} else if (uri.indexOf("/currentlog") != -1) {
				if (session.getScriptLogFile() != null) {
					sendLogResponse(appendLogsRoot(session.getScriptLogFile()));
				}
			} else if (uri.indexOf("/highlighted") != -1) {
				int lineNumber = getLineNumber(requestFromBrowser);
				String fileName = ProxyProcessorHelper.scriptFileNamefromURI(
						requestFromBrowser.uri(), "/highlighted/");
				final HttpFileResponse response = new HttpFileResponse(fileName);
				if (lineNumber != -1) {
					response
							.data(ProxyProcessorHelper.highlight(
									new String(response.data()), lineNumber)
									.getBytes());
				}
				System.out.println(new String(response.data()));
				sendResponseToBrowser(response);
			} else if (uri.indexOf("/currentparsedscript") != -1) {
				if (session.getScript() != null) {
					sendResponseToBrowser(new SimpleHttpResponse("<pre>"
							+ SahiScriptHTMLAdapter.createHTML(session
									.getScript().modifiedScript()) + "</pre>"));
				} else {
					sendResponseToBrowser(new SimpleHttpResponse(
							"No Script has been set for playback."));
				}
			}

		} else if (uri.indexOf("/scripts/") != -1) {
			String fileName = ProxyProcessorHelper.scriptFileNamefromURI(
					requestFromBrowser.uri(), "/scripts/");
			sendResponseToBrowser(new HttpFileResponse(fileName));
		} else if (uri.indexOf("/logs/") != -1 || uri.endsWith("/logs")) {
			sendLogResponse(logFileNamefromURI(requestFromBrowser.uri()));
		} else if (uri.indexOf("/spr/") != -1) {
			String fileName = fileNamefromURI(requestFromBrowser.uri());
			sendResponseToBrowser(new HttpFileResponse(fileName));
		} else {
			sendResponseToBrowser(new SimpleHttpResponse(
					"<html><h2>You have reached the Sahi proxy.</h2></html>"));
		}
	}

	private int getLineNumber(HttpRequest req) {
		String p = req.getParameter("n");
		int i = -1;
		try {
			i = Integer.parseInt(p);
		} catch (Exception e) {
		}
		return i;
	}

	private void startPlayback(HttpRequest requestFromBrowser, Session session)
			throws IOException {
		if (session.getScript() != null)
			session.startPlayBack();
		session.setVariable("sahi_play", "1");
		session.setVariable("sahiPaused", "1");
		sendBlankResponse(session);
	}

	private void sendLogResponse(String fileName) throws IOException {
		if ("".equals(fileName))
			sendResponseToBrowser(new NoCacheHttpResponse(getLogsList()));
		else
			sendResponseToBrowser(new HttpFileResponse(fileName));
	}

	private void sendBlankResponse(Session session) throws IOException {
		sendResponseToBrowser(new NoCacheHttpResponse(""));
	}

	private HttpResponse addSahisidCookie(HttpResponse httpResponse,
			Session session) {
		httpResponse.addHeader("Set-Cookie", "sahisid=" + session.id()
				+ "; path=/; ");
		// P3P: policyref="http://catalog.example.com/P3P/PolicyReferences.xml",
		// CP="NON DSP COR CURa ADMa DEVa CUSa TAIa OUR SAMa IND"
		httpResponse
				.addHeader(
						"P3P",
						"policyref=\"http://www.sahidomain.com/p3p.xml\", CP=\"NON DSP COR CURa ADMa DEVa CUSa TAIa OUR SAMa IND\"");
		httpResponse.setRawHeaders(httpResponse.getRebuiltHeaderBytes());
		return httpResponse;
	}

	private void stopPlay(Session session) {
		if (session.getScript() != null)
			session.stopPlayBack();
		SahiTestSuite suite = SahiTestSuite.getSuite(session.id());
		if (suite != null) {
			suite.stop(session.getScript().getScriptName());
			waitASec();
			if (!suite.executeNext())
				consolidateLogs(session.getSuiteLogDir());
		} else {
			consolidateLogs(session.getScriptLogFile());
		}
	}

	private void waitASec() {
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void consolidateLogs(String consolidateBy) {
		try {
			new LogFileConsolidator(consolidateBy).summarize();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private String getLogsList() {
		return LogFileConsolidator.getLogsList();
	}

	private String getScriptName(Session session) {
		SahiScript script = session.getScript();
		if (script == null)
			return "";
		return script.getScriptName();
	}

	private HttpFileResponse proxyAutoResponse(String startUrl, String sessionId) {
		Properties props = new Properties();
		props.setProperty("startUrl", startUrl);
		props.setProperty("sessionId", sessionId);
		return new HttpFileResponse(Configuration.getHtdocsRoot()
				+ "spr/auto.htm", props);
	}

	private HttpFileResponse proxyAlertResponse(String msg) {
		Properties props = new Properties();
		props.setProperty("msg", msg);
		return new HttpFileResponse(Configuration.getHtdocsRoot()
				+ "spr/alert.htm", props);
	}

	private HttpFileResponse proxyConfirmResponse(String msg) {
		Properties props = new Properties();
		props.setProperty("msg", msg);
		return new HttpFileResponse(Configuration.getHtdocsRoot()
				+ "spr/confirm.htm", props);
	}

	private HttpResponse proxyStateResponse(Session session) {
		Properties props = new Properties();
		props.setProperty("sessionId", session.id());
		props.setProperty("isRecording", "" + session.isRecording());
		props.setProperty("isWindowOpen", "" + session.isWindowOpen());
		props.setProperty("hotkey", "" + Configuration.getHotKey());
		NoCacheHttpResponse httpResponse = new NoCacheHttpResponse(
				new HttpFileResponse(Configuration.getHtdocsRoot()
						+ "spr/state.js", props));
		addSahisidCookie(httpResponse, session);
		return httpResponse;
	}

	private Session getSession(HttpRequest requestFromBrowser) {
		String sessionId = null;
		sessionId = requestFromBrowser.getParameter("sahisid");
		// System.out.println("1:"+sessionId);
		if (isBlankOrNull(sessionId))
			sessionId = requestFromBrowser.getCookie(new String("sahisid"));
		if (isBlankOrNull(sessionId))
			sessionId = "sahi_" + System.currentTimeMillis();
		// System.out.println("2:"+sessionId);
		return Session.getInstance(sessionId);
	}

	private boolean isBlankOrNull(String s) {
		return (s == null || "".equals(s));
	}

	private String logFileNamefromURI(String uri) {
		String fileName = uri.substring(uri.indexOf("/logs/") + 6);
		if ("".equals(fileName))
			return "";
		return appendLogsRoot(fileName);
	}

	private String appendLogsRoot(String fileName) {
		return Utils.concatPaths(Configuration.getPlayBackLogsRoot(), fileName);
	}

	private String fileNamefromURI(String uri) {
		return Utils.concatPaths(Configuration.getHtdocsRoot(), uri
				.substring(uri.indexOf("_s_/") + 4));
	}

	private String getScriptFileWithPath(String fileName) {
		if (!fileName.endsWith(".sah"))
			fileName = fileName + ".sah";
		return Configuration.getScriptRoot() + fileName;
	}

	private void startRecorder(HttpRequest request, Session session) {
		String fileName = request.getParameter("file");
		session.getRecorder().start(getScriptFileWithPath(fileName));
		session.setVariable("sahi_record", "1");
		// System.out.println("$$$$$$$$$$$ "+session.id());
	}

	private HttpResponse getResponseFromHost(InputStream inputStreamFromHost,
			OutputStream outputStreamToHost, HttpRequest request)
			throws IOException {
		logger.finest(request.uri());
		logger.finest(new String(request.rawHeaders()));
		outputStreamToHost.write(request.rawHeaders());
		if (request.isPost())
			outputStreamToHost.write(request.data());
		outputStreamToHost.flush();
		HttpModifiedResponse modifiedResponse = new HttpModifiedResponse(
				inputStreamFromHost);
		logger.finest(new String(modifiedResponse.rawHeaders()));
		return modifiedResponse;
	}

	private Socket getSocketToHost(HttpRequest request) throws IOException {
		InetAddress addr = InetAddress.getByName(request.host());
		if (request.isSSL()) {
			return new SSLHelper().getSocket(request, addr);
		} else {
			if (externalProxyEnabled) {
				return new Socket(externalProxyHost, externalProxyPort);
			}
			return new Socket(addr, request.port());
		}
	}

	private HttpRequest getRequestFromBrowser() throws IOException {
		InputStream in = client.getInputStream();
		return new HttpRequest(in, isSSLSocket);
	}

	protected void sendResponseToBrowser(HttpResponse responseFromHost)
			throws IOException {
		OutputStream outputStreamToBrowser = client.getOutputStream();
		logger.fine(new String(responseFromHost.rawHeaders()));
		outputStreamToBrowser.write(responseFromHost.rawHeaders());
		outputStreamToBrowser.write(responseFromHost.data());
	}

	protected Socket client() {
		return client;
	}
}
