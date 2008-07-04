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
package net.sf.sahi.ssl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Properties;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import net.sf.sahi.config.Configuration;
import net.sf.sahi.request.HttpRequest;
import net.sf.sahi.util.Utils;

public class SSLHelper {

    private String defaultFilePath = "../certs/sahi_example_com";
    static HashMap sslSocketFactories = new HashMap();

    private SSLSocketFactory getSSLClientSocketFactory(String domain) throws IOException {
        if (domain == null) {
            domain = "sahi.example.com";
        }
        if (!sslSocketFactories.containsKey(domain)) {
            String sslPassword = Configuration.getSSLPassword();
            String fileWithPath = getTrustStoreFilePath(domain);
            final SSLSocketFactory socketFactory = createSocketFactory(fileWithPath, sslPassword);
            if (socketFactory != null) {
                sslSocketFactories.put(domain, socketFactory);
            }
        }
        return (SSLSocketFactory) sslSocketFactories.get(domain);
    }

    private SSLSocketFactory createSocketFactory(final String fileWithPath, final String password) {
        SSLSocketFactory factory = null;
        try {
            SSLContext sslContext;
            KeyManagerFactory keyManagerFactory;
            KeyStore keyStore;
            char[] passphrase = password.toCharArray();
            sslContext = SSLContext.getInstance("SSLv3");
            keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
            keyStore = KeyStore.getInstance("JKS");

            final FileInputStream fileInputStream = new FileInputStream(fileWithPath);
            keyStore.load(fileInputStream, passphrase);
            keyManagerFactory.init(keyStore, passphrase);
            sslContext.init(keyManagerFactory.getKeyManagers(), getAllTrustingManager(),
                    new SecureRandom());
            factory = sslContext.getSocketFactory();
            return factory;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return (SSLSocketFactory) SSLSocketFactory.getDefault();
    }

    private String getTrustStoreFilePath(final String domain) {
        String fileWithPath = Utils.concatPaths(Configuration.getCertsPath(), getCertsFileName(domain));
        if ((new File(fileWithPath)).exists()) {
            return fileWithPath;
        }
        if (!Configuration.autoCreateSSLCertificates()) {
            return defaultFilePath;
        }
        try {
            createKeyStore(domain, fileWithPath);
            return fileWithPath;
        } catch (Exception e) {
            return defaultFilePath;
        }
    }

    private String getCertsFileName(final String domain) {
        return domain.replace('.', '_');
    }

    private void createKeyStore(String domain, String keyStoreFilePath) throws IOException, InterruptedException {
        String command = getSSLCommand(domain, keyStoreFilePath, Configuration.getSSLPassword(), Configuration.getKeytoolPath());
        System.out.println("\n\n\n--------------------HTTPS/SSL START--------------------" +
                "\n\nSahi is trying to create a certificate for domain: \n" + domain +
                "\n\nIf you are unable to connect to this HTTPS site, do the following:" +
                "\nCheck on your filesystem to see if a file like " +
                "\n" + keyStoreFilePath +
                "\nhas been created." +
                "\n\nIf not, then create it by running the command below on a command prompt." +
                "\nNote that you need 'keytool' to be in your path. " +
                "\nkeytool comes with the JDK by default and is present in <JAVA_HOME>/bin." +
                "\n\nOnce you create that file, SSL/HTTPS should work properly for that site." +
                "\n\nYou may encounter this problem mostly on Linux." +
                "\nIf you find a solution for this on Linux, " +
                "\nplease mail it to narayanraman@users.sourceforge.net" +
                "\n\n\n-------COMMAND START-------\n\n" +
                getPrintableSSLCommand(command) +
                "\n\n-------COMMAND END-------" +
                "\n\nThe files in certs can be copied over to other systems to make ssl/https work there." +
                "\n\n--------------------HTTPS/SSL END--------------------\n\n\n");
        Process p = Runtime.getRuntime().exec(Utils.getCommandTokens(command));
        p.waitFor();
    }

    String getPrintableSSLCommand(final String command) {
        return command.replace('\n', ' ').replaceAll("\r", "");
    }

    String getSSLCommand(final String domain, final String keyStoreFilePath, final String password, final String keytool) {
        String contents = new String(Utils.readCachedFile(Utils.concatPaths(Configuration.getConfigPath(), "ssl.txt"))).trim();
        Properties props = new Properties();
        props.put("domain", domain);
        props.put("keystore", Utils.escapeDoubleQuotesAndBackSlashes(keyStoreFilePath));
        props.put("password", password);
        props.put("keytool", keytool);
        String command = Utils.substitute(contents, props);
        return command;
    }

    private TrustManager[] getAllTrustingManager() {
        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {

        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
            return null;
        }

        public void checkClientTrusted(java.security.cert.X509Certificate[] certs,
                String authType) {
        }

        public void checkServerTrusted(java.security.cert.X509Certificate[] certs,
                String authType) {
        }
    }
        };
        return trustAllCerts;
    }

    public Socket getSocket(final HttpRequest request, final InetAddress addr, final int port) throws IOException {
        SSLSocketFactory sslFact = getSSLClientSocketFactory(addr.getHostName());
        SSLSocket socket = (SSLSocket) sslFact.createSocket(addr, port);
        socket.setUseClientMode(true);
        socket.setEnabledCipherSuites(socket.getSupportedCipherSuites());
        return socket;
    }

    public SSLSocket convertToSecureSocket(final Socket plainSocket, final String domain) {
        try {
            return (SSLSocket) getSSLClientSocketFactory(domain).createSocket(plainSocket,
                    plainSocket.getInetAddress().getHostName(), plainSocket.getPort(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public SSLSocket convertToSecureServerSocket(final Socket socket, final String domain) {
        SSLSocket sslSocket = convertToSecureSocket(socket, domain);
        sslSocket.setUseClientMode(false);
        return sslSocket;
    }
}
