package com.sahi;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.logging.Logger;

import com.sahi.config.Configuration;

/**
 * User: nraman Date: May 13, 2005 Time: 6:52:31 PM
 * Starts HTTP Proxy
 */
public class Proxy {
	private int port = 9999;
	private static Logger logger = Configuration
	.getLogger("com.sahi.Proxy");

	public Proxy(int port) {
		this.port = port;
	}

	public static void main(String[] args) {
		try {
			new Proxy(Configuration.getPort()).startProxies();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void startProxies() {
		new Thread(new ProxyServer()).start();
	}

	class ProxyServer implements Runnable {
		public void run() {
			try {
				startProxy();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		private void startProxy() throws IOException {
			ServerSocket server = null;
			try {
				server = new ServerSocket(port);
				System.out.println(">>>> Sahi started. Listening on port:"+port);
				while (true) {
					try {
						Socket client = server.accept();
						new Thread(new ProxyProcessor(client)).start();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			} 
			catch(Exception e) {
				e.printStackTrace();
			}
			finally {
				if (server != null)
					server.close();
			}
		}
	}
}