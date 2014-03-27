package net.sf.sahi.test;

import net.sf.sahi.config.Configuration;
import net.sf.sahi.util.Utils;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Sahi - Web Automation and Test Tool
 *
 * Copyright  2006  V Narayan Raman
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * User: dlewis
 * Date: Dec 8, 2006
 * Time: 3:35:44 PM
 */


public class SuiteLoaderTest {

  private File dir;

  @Before
  public void setup() {
    Configuration.init();
    dir = new File(Configuration.getUserDataDir() + "/scripts/junit");
    Utils.deleteDir(dir);
    dir.mkdirs();
  }

  @Test
  public void testProcessSuiteDir() throws IOException {
    new File(dir, "script3.sah").createNewFile();
    new File(dir, "script2.sahi").createNewFile();
    new File(dir, "dummy.txt").createNewFile();
    File subDir = new File(dir, "subDir");
    subDir.mkdirs();
    new File(subDir, "script1.sah").createNewFile();

    List<TestLauncher> listTest = new SuiteLoader(Utils.getAbsolutePath(dir), "testBase").getListTest();
    assertEquals(3, listTest.size());
    TestLauncher test = listTest.get(0);
    assertEquals("testBase", test.getStartURL());
  }

  @Test
  public void testProcessSuiteFile() throws IOException {
    File suiteFile = createSuiteFile();
    List<TestLauncher> listTest = new SuiteLoader(Utils.getAbsolutePath(suiteFile), "http://testBase").getListTest();
    assertEquals(3, listTest.size());
    TestLauncher test = (TestLauncher) listTest.get(0);
    assertEquals("http://testBase", test.getStartURL());
    test = (TestLauncher) listTest.get(1);
    assertEquals("http://testBase/index.htm", test.getStartURL());
    test = (TestLauncher) listTest.get(2);
    assertEquals("https://test2", test.getStartURL());
  }

  private File createSuiteFile() throws IOException {
    File suiteFile = new File(dir, "test.suite");
    BufferedWriter buf = new BufferedWriter(new FileWriter(suiteFile));
    buf.write("script3.sah");
    buf.newLine();
    buf.write("//commented1.sah");
    buf.newLine();
    buf.write("script1.sah index.htm");
    buf.newLine();
    buf.write("#commented2.sah");
    buf.newLine();
    buf.write("script2.sah https://test2");
    buf.flush();
    buf.close();

    return suiteFile;
  }
}
