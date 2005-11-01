package com.sahi;

import com.sahi.config.Configuration;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * User: nraman
 * Date: May 13, 2005
 * Time: 6:52:31 PM
 */
public class WebServer {
    private int port = 10000;

    public WebServer(int port) {
        this.port = port;
    }

    public static void main(String[] args) {
        try {
            new WebServer(Configuration.getPort() + 1).startProxy();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startProxy() throws IOException {
        ServerSocket server = null;
        try {
            server = new ServerSocket(port);
			System.out.println(">>>> Sahi demo web server started. Listening on port:"+port);            
            while (true) {
                Socket client = server.accept();
                new Thread(new WebProcessor(client)).start();
            }
        } finally {
            if (server != null) server.close();
        }
    }
}