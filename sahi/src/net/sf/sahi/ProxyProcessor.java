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

import net.sf.sahi.command.MockResponder;
import net.sf.sahi.config.Configuration;
import net.sf.sahi.request.HttpRequest;
import net.sf.sahi.response.HttpFileResponse;
import net.sf.sahi.response.HttpResponse;
import net.sf.sahi.response.NoContentResponse;
import net.sf.sahi.response.SimpleHttpResponse;
import net.sf.sahi.session.Session;
import net.sf.sahi.ssl.SSLHelper;

import javax.net.ssl.SSLSocket;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
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
            logger.fine("---RAW INPUT HEADERS START---");
            logger.fine(new String(requestFromBrowser.rawHeaders()));
            logger.fine("---RAW INPUT HEADERS END---");
            String uri = requestFromBrowser.uri();
//            System.out.println(uri);
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
            if (isKeepAlive() && !client.isClosed()) new Thread(new ProxyProcessor(client)).start();
        } catch (Exception e) {
        	//e.printStackTrace();
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
            	Session session = requestFromBrowser.session();
                responseFromHost = remoteRequestProcessor.processHttp(requestFromBrowser);
                if (responseFromHost.status().indexOf("200") != -1 &&
                		(isDownloadContentType(responseFromHost.contentType()) ||
                		isDownloadURL(requestFromBrowser.url()))
                		){
            		String fileName = requestFromBrowser.fileName();
					save(responseFromHost, fileName);
            		session.setVariable("download_lastFile", fileName);
            		responseFromHost = new NoContentResponse();
                }
            } catch (Exception e) {
                e.printStackTrace();
                responseFromHost = new SimpleHttpResponse("");
            }
            if (responseFromHost == null) responseFromHost = new SimpleHttpResponse("");
            sendResponseToBrowser(responseFromHost);
        }
    }

    public void save(HttpResponse response, String fileName) {
    	System.out.println("Downloading "+fileName+" to temp directory: "+ Configuration.tempDownloadDir());
    	byte[] data = response.data();
        try {
            File file = new File(Configuration.tempDownloadDir(), fileName);
            if (file.exists()) {
            	file.delete();
            }
            file.createNewFile();
            FileOutputStream out;
            out = new FileOutputStream(file, true);
            out.write(data);
            out.close();
        } catch (IOException e) {
            System.out.println("Could not write to file");
        }
    }


    private boolean isDownloadURL(String url) {
        String[] list = Configuration.getDownloadURLList();
        for (int i = 0; i < list.length; i++) {
            String pattern = list[i];
            if (url.matches(pattern.trim())) {
                return true;
            }
        }
        return false;
    }

    private boolean isDownloadContentType(String contentType) {
    	if (contentType == null || contentType.equals("")) return true;
    	contentType = contentType.toLowerCase();
		String[] downloadables = Configuration.getDownloadContentTypes();
		for (int i=0; i<downloadables.length; i++){
			if (contentType.indexOf(downloadables[i]) != -1){
				return true;
			}
		}
		return false;
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
        logger.fine("---------START----------");
        logger.fine(new String(responseFromHost.rawHeaders()));
        logger.fine("---------END----------");
        outputStreamToBrowser.write(responseFromHost.rawHeaders());
        outputStreamToBrowser.flush();
        final byte[] data = responseFromHost.data();
        if (data != null){
	        outputStreamToBrowser.write(data);
	        outputStreamToBrowser.flush();
        }
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
