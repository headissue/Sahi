package com.headissue.demopages;

import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.bio.SocketConnector;
import org.mortbay.jetty.webapp.WebAppContext;

/**
 * Provides the demopages for the integration tests
 */
public class DemoPageServer implements Runnable {
  public Server server;
  public int port;

  @Override
  public void run() {
    server = new Server();
    SocketConnector _socketConnector = new SocketConnector();
    _socketConnector.setHost("127.0.1.1");
    _socketConnector.setPort(0); // finds automagically a free port
    server.setConnectors(new Connector[]{_socketConnector});
    WebAppContext context = new WebAppContext();
    context.setDescriptor(WebAppContext.WEB_DEFAULTS_XML);
    context.setResourceBase("./src/main/webapp");
    context.setContextPath("/");
    context.setParentLoaderPriority(true);
    server.setHandler(context);
    try {
      server.start();
      port = _socketConnector.getLocalPort();
      synchronized (this) {
        notifyAll();
      }
      server.join();
    } catch (InterruptedException e) {
      try {
        server.stop();
      } catch (Exception e1) {
        throw new RuntimeException(e1);
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
