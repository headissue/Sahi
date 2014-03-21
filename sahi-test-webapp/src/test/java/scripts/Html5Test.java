package scripts;

import init.ProxyStarter;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Created by wormi on 20.03.14.
 */
public class Html5Test {

  @Test
  public void testBlockElements() {
    ProxyStarter proxy = new ProxyStarter();
    proxy.start("testConfig.properties");

    assertTrue(false);
  }
}
