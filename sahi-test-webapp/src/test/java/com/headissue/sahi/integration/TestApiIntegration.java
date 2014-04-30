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
import org.junit.Test;

import java.net.URISyntaxException;
import java.util.Date;

import static org.junit.Assert.assertEquals;

/**
 * Created by wormi on 25.04.14.
 */

public class TestApiIntegration {

  Thread server = new Thread(new DemoPageServer());
  Proxy proxy = new Proxy();

  @Before
  public void setup() throws Exception {
    Configuration.init("../sahi-core", "./userdata");
    proxy.start(true);
    server.start();

  }

  @Test
  public void runTestSuite(){
    SahiTestSuite suite = null;
    BrowserTypesLoader browserLoader = new BrowserTypesLoader();

    String userdata = Configuration.getUserDataDir();

    try {
      Utils.executeCommand(Utils.getCommandTokens("certutil -A -n Sahi_Root -t \"C,,\" -i "+userdata+"/certs/ca.crt -d "+ userdata +"/browser/ff/profiles/sahi0"));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    BrowserType browserType = browserLoader.getBrowserType("firefox");
    Session session = Session.getInstance("1");
    String suitePath = null;
    try {
      suitePath = this.getClass().getResource("/integration").toURI().getPath();
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
    String base = "http://localhost:7733";
    boolean isSingleSession = true;
    final int threads = 1;
    // launches browser with pre configured browser settings
    suite = new Suite().prepareSuite(suitePath, base, browserType.path(), session.id(), browserType.options(), browserType.processName(), ("" + threads), browserType.useSystemProxy(), isSingleSession, null);
    Date d = new Date();
    suite.addReporter(new JunitReporter("./target/junitReporter/"));
    suite.addReporter(new HtmlReporter("./target/htmlReporter/"));
    suite.loadScripts();
    suite.run();
    // to make sure the status is set
    suite.finishCallBack();
    assertEquals(Status.SUCCESS, session.getStatus());
  }

  @After
  public void teardown() throws Exception {
    server.interrupt();
    proxy.stop();
  }

}
