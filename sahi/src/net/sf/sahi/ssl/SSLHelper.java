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

	private String defaultFilePath = "../certs/www_sahidomain_com";
	static HashMap sslSocketFactories = new HashMap();
	private SSLSocketFactory getSSLClientSocketFactory(String domain) throws IOException {
		if (domain == null)
			domain = "www.sahidomain.com";
		if (!sslSocketFactories.containsKey(domain)) {
			String sslPassword = Configuration.getSSLPassword();
			String fileWithPath = getTrustStoreFilePath(domain);
			final SSLSocketFactory socketFactory = createSocketFactory(fileWithPath, sslPassword);
			if (socketFactory != null)
				sslSocketFactories.put(domain, socketFactory);
		}
		return (SSLSocketFactory) sslSocketFactories.get(domain);
	}

	private SSLSocketFactory createSocketFactory(String fileWithPath, String password) {
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

	private String getTrustStoreFilePath(String domain) {
		String fileWithPath = Utils.concatPaths(Configuration.getCertsPath(), domain.replace('.', '_'));
		if ((new File(fileWithPath)).exists())
			return fileWithPath;
		if (!Configuration.autoCreateSSLCertificates())
			return defaultFilePath;
		try {
			createKeyStore(domain, fileWithPath);
			return fileWithPath;
		} catch (Exception e) {
			return defaultFilePath;
		}
	}

	private void createKeyStore(String domain, String keyStoreFilePath) throws IOException, InterruptedException {
		String command = getSSLCommand(domain, keyStoreFilePath, Configuration.getSSLPassword(), Configuration.getKeytoolPath());
		System.out.println(command);
		Process p = Runtime.getRuntime().exec(command);
		p.waitFor();
	}

	String getSSLCommand(String domain, String keyStoreFilePath, String password, String keytool) {
		String contents = new String(Utils.readCachedFile(Utils.concatPaths(Configuration.getConfigPath(), "ssl.txt")));
		Properties props = new Properties();
		props.put("domain", domain);
		props.put("keystore", keyStoreFilePath);
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
		}};
		return trustAllCerts;
	}

	public Socket getSocket(HttpRequest request, InetAddress addr) throws IOException {
		SSLSocketFactory sslFact = getSSLClientSocketFactory(addr.getHostName());
		SSLSocket socket = (SSLSocket) sslFact.createSocket(addr, request.port());
		socket.setUseClientMode(true);
		socket.setEnabledCipherSuites(socket.getSupportedCipherSuites());
		return socket;
	}

	private SSLSocket convertToSecureSocket(Socket plainSocket, String domain) {
		try {
			return (SSLSocket) getSSLClientSocketFactory(domain).createSocket(plainSocket,
					plainSocket.getInetAddress().getHostName(), plainSocket.getPort(), true);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public SSLSocket convertToSecureServerSocket(Socket socket, String domain) {
		SSLSocket sslSocket = convertToSecureSocket(socket, domain);
		sslSocket.setUseClientMode(false);
		return sslSocket;
	}
}
