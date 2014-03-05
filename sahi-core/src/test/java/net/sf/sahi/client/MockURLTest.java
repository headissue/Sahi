package net.sf.sahi.client;

import net.sf.sahi.request.HttpRequest;
import net.sf.sahi.response.HttpModifiedResponse;
import net.sf.sahi.response.HttpResponse;
import net.sf.sahi.response.SimpleHttpResponse;
import net.sf.sahi.response.StreamingHttpResponse;
import net.sf.sahi.stream.filter.ChunkedFilter;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class MockURLTest extends SahiTestCase {

  @Override
  public void setBrowser() {
    setBrowser("ie");
  }

  @Test
  @Ignore("This may not be a UnitTest. Seems more like it should run in an Integrationtest phase")
  public void testMock() {
    browser.navigateTo("http://sahi.co.in/demo/index.htm");
    assertTrue(browser.link("Link Test").exists());

    browser.addURLMock(".*sahi[.]co[.]in.*");
    browser.navigateTo("http://sahi.co.in/demo/index.htm", true);
    assertFalse(browser.link("Link Test").exists());

    browser.removeURLMock(".*sahi[.]co[.]in.*");
    browser.navigateTo("http://sahi.co.in/demo/index.htm", true);
    assertTrue(browser.link("Link Test").exists());

    browser.addURLMock(".*sahi[.]co[.]in.*", "net.sf.sahi.client.MockURLTest_mockMe");
    browser.navigateTo("http://sahi.co.in/demo/index.htm", true);
    assertTrue(browser.div("Hi there").exists());
  }


  public HttpResponse mockMe(HttpRequest request) {
    HttpResponse response = new SimpleHttpResponse("<div>Hi there</div>");
    StreamingHttpResponse response2 = new HttpModifiedResponse(response, request.isSSL(), request.fileExtension());
    response2.addFilter(new ChunkedFilter());
    return response2;
  }
}
