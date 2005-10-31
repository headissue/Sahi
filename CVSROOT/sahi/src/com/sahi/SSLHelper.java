package com.sahi;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.security.KeyStore;
import java.security.SecureRandom;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import com.sahi.request.HttpRequest;

class SSLHelper {

	private SSLSocketFactory getSSLClientSocketFactory() throws IOException {
		SSLSocketFactory factory = null;
		try {
			SSLContext sslContext;
			KeyManagerFactory keyManagerFactory;
			KeyStore keyStore;
			char[] passphrase = "sahipassword".toCharArray();
			sslContext = SSLContext.getInstance("SSLv3");
			keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
			keyStore = KeyStore.getInstance("JKS");
			keyStore.load(new FileInputStream("../certs/keystore"), passphrase);
			keyManagerFactory.init(keyStore, passphrase);
			sslContext.init(keyManagerFactory.getKeyManagers(),
					getAllTrustingManager(), new SecureRandom());
			factory = sslContext.getSocketFactory();
			return factory;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return (SSLSocketFactory) SSLSocketFactory.getDefault();
	}
	

	private TrustManager[] getAllTrustingManager() {
		TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
				return null;
			}
			public void checkClientTrusted(
					java.security.cert.X509Certificate[] certs, String authType) {
			}
			public void checkServerTrusted(
					java.security.cert.X509Certificate[] certs, String authType) {
			}
		}};
		return trustAllCerts;
	}
	
	public Socket getSocket(HttpRequest request, InetAddress addr) throws IOException {
		SSLSocketFactory sslFact = getSSLClientSocketFactory();
		SSLSocket socket = (SSLSocket) sslFact.createSocket(addr, request
				.port());
		socket.setUseClientMode(true);
		socket.setEnabledCipherSuites(socket.getSupportedCipherSuites());
		return socket;
	}	
	
	private SSLSocket convertToSecureSocket(Socket plainSocket) {
		try {
			return (SSLSocket) getSSLClientSocketFactory().createSocket(plainSocket,
					plainSocket.getInetAddress().getHostName(),
					plainSocket.getPort(), true);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}	
	
	public SSLSocket convertToSecureServerSocket(Socket socket) {
		SSLSocket sslSocket = convertToSecureSocket(socket);
		sslSocket.setUseClientMode(false);
		return sslSocket;
	}	
}
