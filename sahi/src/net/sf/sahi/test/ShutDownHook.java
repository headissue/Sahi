package net.sf.sahi.test;

import java.io.IOException;
import java.net.URL;

public class ShutDownHook implements Runnable {

    private String sahiHost;
    private String port;
    private String sessionId;

    public ShutDownHook(final String sahiHost, final String port, final String sessionId) {
        this.sahiHost = sahiHost;
        this.port = port;
        this.sessionId = sessionId;
    }

    public void run() {
        try {
            System.out.println("Shutting down ...");
            String urlStr = "http://" + this.sahiHost + ":" + this.port + "/_s_/dyn/Suite_kill/?sahisid=" + sessionId;
            URL url = new URL(urlStr);
            url.getContent();
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }
}
