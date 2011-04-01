/**
 * Sahi - Web Automation and Test Tool
 *
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

package net.sf.sahi;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.HashMap;
import java.util.Properties;
import java.util.logging.Logger;

import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLSocket;

import net.sf.sahi.command.MockResponder;
import net.sf.sahi.config.Configuration;
import net.sf.sahi.request.HttpRequest;
import net.sf.sahi.response.HttpFileResponse;
import net.sf.sahi.response.HttpResponse;
import net.sf.sahi.response.SimpleHttpResponse;
import net.sf.sahi.ssl.SSLHelper;
import net.sf.sahi.util.ThreadLocalMap;
import net.sf.sahi.util.TrafficLogger;
import net.sf.sahi.util.Utils;

/**
 * User: nraman Date: May 13, 2005 Time: 7:06:11 PM To
 */
public class ProxyProcessor implements Runnable {
    private Socket client;

    private boolean isSSLSocket = false;

    private static Logger logger = Configuration.getLogger("net.sf.sahi.ProxyProcessor");
    public RemoteRequestProcessor remoteRequestProcessor = new RemoteRequestProcessor();

	private static HashMap<String, String> hostAddresses = new HashMap<String, String>(100);

	private static String localhost;

	static {
		try {
			localhost = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}

    public ProxyProcessor(Socket client) {
        this.client = client;
        isSSLSocket = (client instanceof SSLSocket);
    }

    public void run() {
    	if (client.isClosed()) return;
    	ThreadLocalMap.clearAll();
    	HttpRequest requestFromBrowser = null;
        try {
            requestFromBrowser = getRequestFromBrowser();
            String uri = requestFromBrowser.uri();
            logger.finest(uri);
            if (uri != null) {
                int _s_ = uri.indexOf("/_s_/");
                int q = uri.indexOf("?");
                if (_s_ != -1 && (q == -1 || (q > _s_))) {
                    processLocally(uri, requestFromBrowser);
                } else {
                    if (isHostTheProxy(requestFromBrowser.host()) && requestFromBrowser.port() == Configuration.getPort()) {
                        processLocally(uri, requestFromBrowser);
                    } else if (uri.indexOf("favicon.ico") != -1) {
                        sendResponseToBrowser(new HttpFileResponse(Configuration.getHtdocsRoot() + "spr/favicon.ico"));
                    } else {
                        processAsProxy(requestFromBrowser);
                    }
                }
            } else {
                sendResponseToBrowser(new SimpleHttpResponse(""));
            }
            if (isKeepAlive() && !client.isClosed()) {
                new Thread(new ProxyProcessor(client)).start();
            }
        } catch (SSLHandshakeException ssle) {
        	logger.fine(ssle.getMessage());
        } catch (Exception e) {
        	//e.printStackTrace();
            logger.fine(e.getMessage());
            try {
            	// should close only in case of exception. Do not move this to finally. Will cause sockets to not be reused.
                client.close();
            } catch (IOException e2) {
                logger.warning(e2.getMessage());
            }
        }
    }

    private boolean isHostTheProxy(final String host) {
        try {
        	if (host.equals(Configuration.getCommonDomain())) return true;
            String hostAddress = getHostAddress(host);
			return hostAddress.equals(localhost) || hostAddress.equals("127.0.0.1");
        } catch (Exception e) {
            return false;
        }
    }

	private String getHostAddress(final String host) throws UnknownHostException {
		if (!hostAddresses.containsKey(host)){
			hostAddresses.put(host, InetAddress.getByName(host).getHostAddress());
		}
		return hostAddresses.get(host);
	}

    private void processAsProxy(HttpRequest requestFromBrowser) throws Exception {
    	final String fileName = requestFromBrowser.fileName();
    	Date time = new Date();
    	TrafficLogger.createLoggerForThread(fileName, "unmodified", Configuration.isUnmodifiedTrafficLoggingOn(), time);
        TrafficLogger.createLoggerForThread(fileName, "modified", Configuration.isModifiedTrafficLoggingOn(), time);

        if (requestFromBrowser.isConnect()) {
        	TrafficLogger.storeRequestHeader(requestFromBrowser.rawHeaders(), "unmodified");
        	TrafficLogger.storeRequestBody(requestFromBrowser.data(), "unmodified");
            processConnect(requestFromBrowser);
        } else {
            if (handleDifferently(requestFromBrowser)) {
                return;
            }
            HttpResponse responseFromHost = null;
            try {
                responseFromHost = remoteRequestProcessor.processHttp(requestFromBrowser);
            } catch (Exception e) {
                e.printStackTrace();
                responseFromHost = new SimpleHttpResponse("");
            }
            if (responseFromHost == null) {
                responseFromHost = new SimpleHttpResponse("");
            }
//          System.out.println("Fetching >> :" + new String(requestFromBrowser.url()));
            sendResponseToBrowser(responseFromHost);
        }
    }

    private boolean handleDifferently(final HttpRequest request) throws Exception {
        final MockResponder mockResponder = request.session().mockResponder();
        HttpResponse response = mockResponder.getResponse(request);
        if (response == null) {
            return false;
        }
        sendResponseToBrowser(response);
        return true;
    }

    private void processConnect(HttpRequest requestFromBrowser) {
        try {
        	if (isBlockableDomain(requestFromBrowser.host())){
        		client.getOutputStream().write(("HTTP/1.0 404 NOT FOUND\r\n\r\n").getBytes());
        		client.close();
        		return;
        	}
            client.getOutputStream().write(("HTTP/1.0 200 OK\r\n\r\n").getBytes());
            SSLSocket sslSocket = new SSLHelper().convertToSecureServerSocket(client,
                    requestFromBrowser.host());
            ProxyProcessor delegatedProcessor = new ProxyProcessor(sslSocket);
            delegatedProcessor.run();
        } catch (IOException e) {
        	try {
				client.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
            e.printStackTrace();
        }
    }

	private boolean isBlockableDomain(String host) {
        String[] list = Configuration.getBlockableSSLDomainsList();
        for (int i = 0; i < list.length; i++) {
            String pattern = list[i];
            if (host.matches(pattern.trim())) {
                return true;
            }
        }
        return false;
	}


    private void processLocally(String uri, final HttpRequest requestFromBrowser) throws IOException {
    	HttpResponse httpResponse;
    	try {
    		httpResponse = new LocalRequestProcessor().getLocalResponse(uri, requestFromBrowser);
    	} catch (Exception e) {
    		Properties props = new Properties();
    		props.put("responseCode", "500");
    		props.put("time", "" + (new Date()));
    		props.put("message", Utils.getStackTraceString(e, true));
    		
    		httpResponse = new HttpFileResponse(
    				Configuration.getHtdocsRoot() + "spr/5xx.htm",
    				props, false, true);
    	}
        sendResponseToBrowser(httpResponse);
    }


    private HttpRequest getRequestFromBrowser() throws IOException {
        InputStream in = client.getInputStream();
        return new HttpRequest(in, isSSLSocket);
    }

    protected void sendResponseToBrowser(final HttpResponse responseFromHost) throws IOException {
    	OutputStream outputStreamToBrowser = client.getOutputStream();
		responseFromHost.sendHeaders(outputStreamToBrowser, isKeepAlive());
		responseFromHost.sendBody(outputStreamToBrowser);
        if (!isKeepAlive()) {
            outputStreamToBrowser.close();
            client.close();
        }
        responseFromHost.cleanUp();
    }

    private boolean isKeepAlive() {
        return Configuration.isKeepAliveEnabled() && !isSSLSocket;
    }

    protected Socket client() {
        return client;
    }
}
