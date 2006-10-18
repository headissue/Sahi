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

import net.sf.sahi.config.Configuration;
import net.sf.sahi.request.HttpRequest;
import net.sf.sahi.response.HttpFileResponse;
import net.sf.sahi.response.HttpModifiedResponse;
import net.sf.sahi.response.HttpResponse;
import net.sf.sahi.ssl.SSLHelper;
import net.sf.sahi.util.SocketPool;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Properties;
import java.util.logging.Logger;

public class RemoteRequestProcessor {
    private static Logger logger = Configuration.getLogger("net.sf.sahi.RemoteRequestProcessor");
    private static SocketPool socketPool = new SocketPool(20);

    private static boolean externalProxyEnabled = Configuration.isExternalProxyEnabled();

    private static String externalProxyHost = null;

    private static int externalProxyPort = 80;


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

    public HttpResponse processHttp(HttpRequest requestFromBrowser) {
        return processHttp(requestFromBrowser, true);
    }

    public HttpResponse processHttp(HttpRequest requestFromBrowser, boolean modify) {
        Socket socketToHost;
        try {
            try {
                socketToHost = getSocketToHost(requestFromBrowser);
            } catch (IOException e) {
                e.printStackTrace();
                return getCannotConnectResponse(e);
            }
            socketToHost.setSoTimeout(120000);
            OutputStream outputStreamToHost = socketToHost.getOutputStream();
            InputStream inputStreamFromHost = socketToHost.getInputStream();
            HttpResponse responseFromHost = getResponseFromHost(inputStreamFromHost,
                    outputStreamToHost, requestFromBrowser, modify);
            socketPool.release(socketToHost);
            return responseFromHost;
        } catch (Exception ioe) {
        }
        return null;
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
        if (modify && !excluded(request)) {
            response = new HttpModifiedResponse(new HttpResponse(inputStreamFromHost), request
                    .isSSL(), request.fileExtension());
        } else {
            response = new HttpResponse(inputStreamFromHost);
        }
        logger.finest(new String(response.rawHeaders()));
        return response;
    }

    private boolean excluded(HttpRequest request) {
        String url = request.url();
        String[] exclusionList = Configuration.getExclusionList();
        for (int i = 0; i < exclusionList.length; i++) {
            String pattern = exclusionList[i];
            if (url.matches(pattern.trim())) {
                return true;
            }
        }
        return false;
    }

    private Socket getSocketToHost(HttpRequest request) throws IOException {
        if (request.isSSL()) {
            return getSSLSocketToHost(request);
        } else {
            return getNormalSocket(request);
        }
    }

    private Socket getSSLSocketToHost(HttpRequest request) throws IOException {
        if (externalProxyEnabled) {
            return getTunnelledSecureSocket(request);
        } else {
            return new SSLHelper().getSocket(request, InetAddress.getByName(request.host()), request.port());
        }
    }

    private Socket getNormalSocket(HttpRequest request) throws IOException {
        if (externalProxyEnabled) {
            return getReusableSocket(externalProxyHost, externalProxyPort, true);
        } else {
            return getReusableSocket(request.host(), request.port(), true);
        }
    }

    private Socket getReusableSocket(String externalProxyHost, int externalProxyPort, boolean reuse) throws IOException {
        Socket socket = socketPool.get(externalProxyHost, externalProxyPort);
        socket.setReuseAddress(reuse);
        return socket;
    }

    private Socket getTunnelledSecureSocket(HttpRequest request) throws IOException {
        Socket tunnel = new Socket(externalProxyHost, externalProxyPort);
        handShakeExternalProxy(tunnel, request);
        return new SSLHelper().convertToSecureSocket(tunnel, externalProxyHost);
    }

    private void handShakeExternalProxy(Socket tunnel, HttpRequest request) throws IOException {
        tunnel.getOutputStream().write(createConnectRequest(request));
        InputStream inputStream = tunnel.getInputStream();
        char c;
        boolean EOF = true;
        StringBuffer sb = new StringBuffer();
        while ((c = (char) inputStream.read()) != -1) {
            sb.append(c);
            if (c == '\n') {
                if (EOF) break;
                EOF = true;
            } else if (c != '\r') {
                EOF = false;
            }
        }
        if (sb.toString().indexOf("200 Connection established") == -1) {
            System.out.println(sb.toString());
        }
    }

    private byte[] createConnectRequest(HttpRequest request) {
        String s = "CONNECT " + request.host() + ":" + request.port() + " HTTP/1.0\r\n";
        s += "User-agent: Mozilla/1.1N\r\n";
        s += "\r\n";
//        System.out.println(s);
        return s.getBytes();
    }


    private HttpResponse getCannotConnectResponse(IOException e) throws IOException {
        Properties props = new Properties();
        props.put("message", e.getMessage());
        final HttpFileResponse httpFileResponse = new HttpFileResponse(Configuration
                .getHtdocsRoot()
                + "spr/cannotConnect.htm", props, false, true);
        final HttpResponse modifiedResponse = HttpModifiedResponse.modify(httpFileResponse);
        return modifiedResponse;
    }
}
