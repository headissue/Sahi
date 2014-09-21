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
import org.junit.Ignore;
import org.junit.Test;

import java.net.URISyntaxException;
import java.util.Date;

import static org.junit.Assert.assertEquals;

/**
 * Created by wormi on 25.04.14.
 */

public class TestApiIntegration extends SahiIntegrationTest {

  @Test
  public void allIntegrationTests(){
    String suitePath = getSuitePath("/integration");
    runSuite(suitePath);
  }

  /* just for dev use /
  @Test
  public void singletest() {
    String suitePath = getSuitePath("/integration/language.sah");
    runSuite(suitePath);
  }
  //*/
}
