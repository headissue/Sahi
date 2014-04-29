package net.sf.sahi.util;

import net.sf.sahi.config.Configuration;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Sahi - Web Automation and Test Tool
 * <p/>
 * Copyright  2006  V Narayan Raman
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


public class OSUtils {

  private static Properties properties = new Properties();

  private static String osName = System.getProperty("os.name");

  static {
    init();
  }

  public static void init() {
    try {

      Configuration.loadProperties(new FileInputStream(Configuration.getOSPropertiesFile()), properties);
    } catch (IOException e) {
      e.printStackTrace();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static String identifyOS() {
    return properties.getProperty(osName, "xp");
  }

  public static String getPIDListCommand() {
    return properties.getProperty(identifyOS() + ".processhelper.pid_list_cmd");
  }

  public static String getPIDKillCommand() {
    return properties.getProperty(identifyOS() + ".processhelper.pid_kill_cmd");
  }

  public static int getPIDListColumnNo() {
    return Integer.parseInt(properties.getProperty(identifyOS() + ".processhelper.pid_list_pid_column_no"));
  }

}
