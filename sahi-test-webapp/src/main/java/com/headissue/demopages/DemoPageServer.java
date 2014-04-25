package com.headissue.demopages;

import org.mortbay.jetty.Server;
import org.mortbay.jetty.webapp.WebAppContext;

/**
 * Provides the demopages for the integration tests
 */
public class DemoPageServer implements Runnable {
  public Server server;

  @Override
  public void run() {
    server = new Server(7733);
    WebAppContext context = new WebAppContext();
    context.setDescriptor(WebAppContext.WEB_DEFAULTS_XML);
    context.setResourceBase("./src/main/webapp");
    context.setContextPath("/");
    context.setParentLoaderPriority(true);
    server.setHandler(context);
    try {
      server.start();
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
