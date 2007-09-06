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

import net.sf.sahi.command.MockResponder;
import net.sf.sahi.config.Configuration;
import net.sf.sahi.request.HttpRequest;
import net.sf.sahi.response.HttpFileResponse;
import net.sf.sahi.response.HttpResponse;
import net.sf.sahi.response.SimpleHttpResponse;
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
                int _s_ = uri.indexOf("/_s_/");
                int q = uri.indexOf("?");
                if (_s_ != -1 && (q == -1 || (q > _s_))) {
                    processLocally(uri, requestFromBrowser);
                } else {
                    if (isHostTheProxy(requestFromBrowser.host())
                            && requestFromBrowser.port() == Configuration.getPort()) {
                        processLocally(uri, requestFromBrowser);
                    } else if (uri.indexOf("favicon.ico") != -1) {
                        sendResponseToBrowser(new HttpFileResponse(Configuration.getHtdocsRoot()
                                + "spr/favicon.ico"));
                    } else {
                        processAsProxy(requestFromBrowser);
                    }
                }
            } else {
                sendResponseToBrowser(new SimpleHttpResponse(""));
            }
            if (isKeepAlive()) new Thread(new ProxyProcessor(client)).start();
        } catch (Exception e) {
            logger.fine(e.getMessage());
            try {
                client.close();
            } catch (IOException e2) {
                logger.warning(e2.getMessage());
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
            HttpResponse responseFromHost = null;
            try {
                responseFromHost = remoteRequestProcessor.processHttp(requestFromBrowser);
            } catch (Exception e) {
                e.printStackTrace();
                responseFromHost = new SimpleHttpResponse("");
            }
            if (responseFromHost == null) responseFromHost = new SimpleHttpResponse("");
            sendResponseToBrowser(responseFromHost);
        }
    }

    private boolean handleDifferently(HttpRequest request) throws IOException {
        final MockResponder mockResponder = request.session().mockResponder();
        HttpResponse response = mockResponder.getResponse(request);
        if (response == null) return false;
        sendResponseToBrowser(response);
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
        sendResponseToBrowser(httpResponse);
    }


    private HttpRequest getRequestFromBrowser() throws IOException {
        InputStream in = client.getInputStream();
        return new HttpRequest(in, isSSLSocket);
    }

    protected void sendResponseToBrowser(HttpResponse responseFromHost) throws IOException {
        OutputStream outputStreamToBrowser = new BufferedOutputStream(client.getOutputStream());
        responseFromHost.proxyKeepAlive(isKeepAlive());
        outputStreamToBrowser.write(responseFromHost.rawHeaders());
        outputStreamToBrowser.flush();
        final byte[] data = responseFromHost.data();
        outputStreamToBrowser.write(data);
        outputStreamToBrowser.flush();
        if (!isKeepAlive()){
            outputStreamToBrowser.close();
            client.close();
        }
    }

    private boolean isKeepAlive() {
        return Configuration.isKeepAliveEnabled() && !isSSLSocket;
    }

    protected Socket client() {
        return client;
    }
}
