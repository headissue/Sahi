package net.sf.sahi.test;

import net.sf.sahi.config.Configuration;
import net.sf.sahi.util.FileUtils;
import net.sf.sahi.util.Utils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;

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

public class BrowserLauncherTest {

  public String launchURL;

  @Before
  public void setUp() throws Exception {
    Configuration.init();
    launchURL = "http://auto?startUrl=http://www.starturl.com&sessionId=123";
  }

  @Test
  public void testEscapeForWindows() {
    BrowserLauncher browserLauncher = new BrowserLauncher("C:\\ie.exe", "ie.exe", "", true);
    assertEquals("\"C:\\ie.exe\"  \"http://auto?startUrl=http://www.starturl.com&sessionId=123\"",
      browserLauncher.buildCommandForWindows(launchURL));
  }

  @Test
  public void testEscapeForNonWindows() {
    BrowserLauncher browserLauncher = new BrowserLauncher("/usr/programs/firefox", "firefox", "", false);
    assertEquals("/usr/programs/firefox http://auto?startUrl=http://www.starturl.com&sessionId=123",
      browserLauncher.buildCommandForNonWindows(launchURL));
  }

  @Test
  @Ignore("OS dependent")
  public void ytestFirefoxFirstLaunchAndKill() throws Exception {
    String pathname = "D:/sahi/sf/sahi_993/userdata/browser/ff/profiles/sahi9";
    Utils.deleteDir(new File(pathname));
    FileUtils.copyDir("D:/sahi/sf/sahi_993/config/ff_profile_template", pathname);

    BrowserLauncher browserLauncher = new BrowserLauncher("C:\\Program Files\\Mozilla Firefox\\firefox.exe", "firefox.exe", "-profile " + pathname + " -no-remote", false);
    browserLauncher.openURL("http://narayan:10000/demo/");
//    	Thread.sleep(5000);
    browserLauncher.kill();

    //Thread.sleep(1000);

    browserLauncher.openURL("http://narayan:10000/demo/");
    Thread.sleep(5000);
    browserLauncher.kill();
  }
}
