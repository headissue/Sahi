package com.headissue.sahi.integration;

import com.headissue.demopages.DemoPageServer;
import net.sf.sahi.Proxy;
import net.sf.sahi.command.Suite;
import net.sf.sahi.config.Configuration;
import net.sf.sahi.report.HtmlReporter;
import net.sf.sahi.report.JunitReporter;
import net.sf.sahi.session.Session;
import net.sf.sahi.session.Status;
import net.sf.sahi.test.SahiTestSuite;
import net.sf.sahi.util.BrowserType;
import net.sf.sahi.util.BrowserTypesLoader;
import net.sf.sahi.util.Utils;
import org.junit.After;
import org.junit.Before;

import java.net.URISyntaxException;
import java.util.Date;

import static org.junit.Assert.assertEquals;

/**
 * Created by wormi on 10/06/14.
 */
public class SahiIntegrationTest {

  final protected DemoPageServer demoPageServer = new DemoPageServer();
  final protected Thread server = new Thread(demoPageServer);
  final protected Proxy proxy = new Proxy();
  final protected String base = "http://127.0.1.1";
  final protected boolean isSingleSession = true;
  final protected int threads = 1;

  protected Session session;
  protected BrowserType browserType;
  protected String userdata;
  protected BrowserTypesLoader browserLoader;

  private void createSession() {
    // more random, no while loop
    String sessionId = random();
    if (Session.hasInstance(sessionId)) sessionId = random();
    session=Session.getInstance(sessionId);
  }

  private static String random() {
    double d= Math.random();
    String number = String.valueOf(d);
    number = number.substring(number.indexOf( "." ) + 1);
    return number;
  }

  private void addCertToFirefox(String userdata) {
    try {
      Utils.executeCommand(Utils.getCommandTokens("certutil -A -n Sahi_Root -t \"C,,\" -i " + userdata + "/certs/ca.crt -d " + userdata + "/browser/ff/profiles/sahi0"));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  protected void runSuite(String suitePath) {
    createSession();
    String url = base + ":" + demoPageServer.port;
    SahiTestSuite suite = new Suite().prepareSuite(suitePath, url, browserType.path(), session.id(), browserType.options(), browserType.processName(), ("" + threads), browserType.useSystemProxy(), isSingleSession, null);
    Date d = new Date();
    suite.addReporter(new JunitReporter("./target/junitReporter/"));
    suite.addReporter(new HtmlReporter("./target/htmlReporter/"));
    suite.loadScripts();
    suite.run();
    // to make sure the status is set
    suite.finishCallBack();
    assertEquals(Status.SUCCESS, session.getStatus());
  }

  protected String getSuitePath(String resource) {
    String suitePath;
    try {
      suitePath = this.getClass().getResource(resource).toURI().getPath();
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
    return suitePath;
  }



  @Before
  public void setup() throws Exception {
    Configuration.init("../sahi-core", "./userdata");
    browserLoader = new BrowserTypesLoader();
    //browserType = browserLoader.getBrowserType("firefox");
    browserType = browserLoader.getBrowserType("phantomjs");
    userdata = Configuration.getUserDataDir();
    proxy.start(true);
    server.start();
    System.out.println("°!°!°!°!°!!");
    System.out.println(demoPageServer.port);
    System.out.println("°!°!°!°!°!!");
    //addCertToFirefox(userdata);
  }

  @After
  public void teardown() throws Exception {
    server.interrupt();
    proxy.stop();
  }

}
