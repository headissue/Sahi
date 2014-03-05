package net.sf.sahi.playback;

import net.sf.sahi.config.Configuration;
import net.sf.sahi.util.Utils;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
public class FileScriptTest {

  @Before
  public void setup() {
    Configuration.init();
  }

  @Test
  public void testFQN() {
    if (Utils.isWindows()) {
      FileScript fileScript = new MockFileScript("c:/abc/def/a.sah");
      assertEquals(new File("c:/abc/def/b.sah"), new File(fileScript.getFQN("b.sah")));
    }
  }

  private class MockFileScript extends FileScript {
    public MockFileScript(String fileName) {
      super(fileName);
    }

    protected void loadScript(String fileName) {
    }
  }

  @Test
  public void testDirCreation() {
    if (Utils.isWindows()) {
      File file = new File(new File("D:\\my\\sahi\\logs\\"), "D:\\my");
//        file = new File("D:\\my");
      boolean b = file.isAbsolute();
      assertTrue(b);
    }
  }
}
