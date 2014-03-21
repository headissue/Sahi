package init;

import net.sf.sahi.Proxy;
import net.sf.sahi.config.Configuration;

/**
 * Created by wormi on 20.03.14.
 */
public class ProxyStarter {

  public void start(String properties) {
    String config = this.getClass().getResource(properties).getPath();
    Configuration.init(config);
    Proxy proxy = new Proxy(Configuration.getPort());
    proxy.start(true);
  }
}
