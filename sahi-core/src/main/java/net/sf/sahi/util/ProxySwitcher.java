package net.sf.sahi.util;

import java.util.logging.Level;


import net.sf.sahi.config.Configuration;
import org.apache.log4j.Logger;

public class ProxySwitcher {

  private static String toolsBasePath = Configuration.getToolsPath();
  private static int counter = 0;
  private static Logger logger = Logger.getLogger(ProxySwitcher.class);

  /**
   * Restores System Proxy settings to what was before.
   * Used with configureSahiAsSystemProxy()
   */
  public static void revertSystemProxy() {
    revertSystemProxy(false);
  }

  public synchronized static void revertSystemProxy(boolean force) {
    if (counter > 0) counter--;
    if (force) counter = 0;
    if (counter == 0) execCommand(toolsBasePath + "/proxy_config.exe original");
  }

  /**
   * Sets System proxy settings to localhost 9999
   */
  public synchronized static void setSahiAsProxy() {
    if (counter == 0) {
      execCommand(toolsBasePath + "/backup_proxy_config.exe");
      execCommand(toolsBasePath + "/proxy_config.exe sahi_https");
    }
    counter++;
  }

  private static void execCommand(String cmd) {
    try {
      Utils.executeCommand(Utils.getCommandTokens(cmd));
    } catch (Exception ex) {
      logger.error("Tried to execute: " + cmd, ex);
    }
  }

}
