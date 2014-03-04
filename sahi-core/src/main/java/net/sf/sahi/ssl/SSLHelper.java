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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.Properties;
import java.util.logging.Logger;

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
	private static final Logger logger = Logger.getLogger("net.sf.sahi.ssl.SSLHelper");
	private String defaultFilePath = Utils.concatPaths(Configuration.getCertsPath(), "sahi_example_com");
	static HashMap<String, SSLSocketFactory> sslSocketFactories = new HashMap<String, SSLSocketFactory>();

	private SSLSocketFactory getSSLClientSocketFactory(String domain) throws IOException {
		if (domain == null) {
			domain = Configuration.getCommonDomain();
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
			KeyManagerFactory keyManagerFactory = getKeyManagerFactory(fileWithPath, password, "JKS");

			SSLContext sslContext = SSLContext.getInstance("SSLv3");
			sslContext.init(keyManagerFactory.getKeyManagers(), getAllTrustingManager(), new SecureRandom());
			factory = sslContext.getSocketFactory();

			return factory;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return (SSLSocketFactory) SSLSocketFactory.getDefault();
	}

	public static KeyManagerFactory getKeyManagerFactoryForRemoteFetch() throws UnrecoverableKeyException, NoSuchAlgorithmException,
			KeyStoreException, FileNotFoundException, CertificateException, IOException {
		String fileWithPath = Configuration.getSSLClientCertPath();
		logger.info(fileWithPath == null ? "No SSL Client Cert specified" : ("\n----\nSSL Client Cert Path = " + fileWithPath + "\n----"));
		String password = Configuration.getSSLClientCertPassword();
		return getKeyManagerFactory(fileWithPath, password, Configuration.getSSLClientKeyStoreType());
	}

	public static KeyManagerFactory getKeyManagerFactory(final String fileWithPath, final String password, String keyStoreType)
			throws NoSuchAlgorithmException, KeyStoreException, FileNotFoundException, IOException,
			CertificateException, UnrecoverableKeyException {
		char[] passphrase = password == null ? null : password.toCharArray();
		KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(Configuration.getSSLAlgorithm());
		KeyStore keyStore = KeyStore.getInstance(keyStoreType);
		FileInputStream fileInputStream = null;
		if (fileWithPath != null){
			try{
				fileInputStream = new FileInputStream(fileWithPath);
			}catch(IOException ioe){
				//ioe.printStackTrace();
				logger.warning("\n----\nCertificate not found: " + fileWithPath + "\n----");
			}
		}
		keyStore.load(fileInputStream, passphrase);
		keyManagerFactory.init(keyStore, passphrase);
		return keyManagerFactory;
	}

	private String getTrustStoreFilePath(final String domain) {
		String fileWithPath = Utils.concatPaths(Configuration.getCertsPath(), getCertsFileName(domain));
		if ((new File(fileWithPath)).exists()) {
			return fileWithPath;
		}
		if (!Configuration.autoCreateSSLCertificates()) {
			return defaultFilePath;
		}
		String command = getSSLCommand(domain, fileWithPath, Configuration.getSSLPassword(), Configuration
				.getKeytoolPath());
		try {
			executeCommand(command);
			return fileWithPath;
		} catch (Exception e) {
			System.out.println("\n\n\n--------------------HTTPS/SSL START--------------------"
					+ "\n\nSahi is trying to create a certificate for domain: \n" + domain
					+ "\n\nIf you are unable to connect to this HTTPS site, do the following:"
					+ "\nCheck on your filesystem to see if a file like " + "\n" + fileWithPath + "\nhas been created."
					+ "\n\nIf not, then create it by running the command below on a command prompt."
					+ "\nNote that you need 'keytool' to be in your path. "
					+ "\nkeytool comes with the JDK by default and is present in <JAVA_HOME>/bin."
					+ "\n\nOnce you create that file, SSL/HTTPS should work properly for that site."
					+ "\n\n\n-------COMMAND START-------\n\n" + getPrintableSSLCommand(command)
					+ "\n\n-------COMMAND END-------"
					+ "\n\nThe files in certs can be copied over to other systems to make ssl/https work there."
					+ "\n\n--------------------HTTPS/SSL END--------------------\n\n\n");
			return fileWithPath;
		}
	}

	private synchronized static void executeCommand(String command) throws Exception {
		Utils.executeCommand(Utils.getCommandTokens(command));
	}

	private String getCertsFileName(final String domain) {
		return domain.replace('.', '_');
	}
	
	String getPrintableSSLCommand(final String command) {
		return command.replace('\n', ' ').replaceAll("\r", "");
	}

	String getSSLCommand(final String domain, final String keyStoreFilePath, final String password, final String keytool) {
		String contents = new String(Utils.readCachedFile(Configuration.getSSLCommandFile())).trim();
		Properties props = new Properties();
		props.put("domain", domain);
		props.put("keystore", Utils.escapeDoubleQuotesAndBackSlashes(keyStoreFilePath));
		props.put("password", password);
		props.put("keytool", Utils.escapeDoubleQuotesAndBackSlashes(keytool));
		String command = Utils.substitute(contents, props);
		return command;
	}

	public static TrustManager[] getAllTrustingManager() {
		TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {

			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
				return null;
			}

			public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
			}

			public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
			}
		} };
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
