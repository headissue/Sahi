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

package net.sf.sahi;

import net.sf.sahi.command.MockResponder;
import net.sf.sahi.config.Configuration;
import net.sf.sahi.request.HttpRequest;
import net.sf.sahi.response.HttpFileResponse;
import net.sf.sahi.response.HttpResponse;
import net.sf.sahi.ssl.SSLHelper;

import javax.net.ssl.SSLSocket;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.logging.Logger;

/**
 * User: nraman Date: May 13, 2005 Time: 7:06:11 PM To
 */
public class ProxyProcessor implements Runnable {
    private Socket client;

    private boolean isSSLSocket = false;

    private static Logger logger = Configuration.getLogger("net.sf.sahi.ProxyProcessor");
    public RemoteRequestProcessor remoteRequestProcessor = new RemoteRequestProcessor();


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

    private boolean isHostTheProxy(String host) {
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
            if (handleDifferently(requestFromBrowser)) return;
            sendResponseToBrowser(remoteRequestProcessor.processHttp(requestFromBrowser), true);
        }
    }

    private boolean handleDifferently(HttpRequest request) throws IOException {
        final MockResponder mockResponder = request.session().mockResponder();
        HttpResponse response = mockResponder.getResponse(request);
        if (response == null) return false;
        sendResponseToBrowser(response, true);
        return true;
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


    private void processLocally(String uri, HttpRequest requestFromBrowser) throws IOException {
        HttpResponse httpResponse = new LocalRequestProcessor().getLocalResponse(uri,
                requestFromBrowser);
        sendResponseToBrowser(httpResponse, false);
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
        hackyWaitForIE(wait);
        outputStreamToBrowser.close();
    }

    private void hackyWaitForIE(boolean wait) {
        if (wait) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    protected Socket client() {
        return client;
    }
}
