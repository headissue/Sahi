package net.sf.sahi.playback;

import net.sf.sahi.config.Configuration;
import org.junit.Before;
import org.junit.Test;

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
public class URLScriptTest {

  @Before
  public void setup() {
    Configuration.init();
  }

  @Test
  public void testFQN() {
    MockURLScript urlScript = new MockURLScript("http://abc/def/a.sah");
    assertEquals("http://abc/def/b.sah", urlScript.getFQN("b.sah"));
  }

  @Test
  public void testFQNWithFullURL() {
    MockURLScript urlScript = new MockURLScript("http://abc/def/a.sah");
    assertEquals("http://xxx/b.sah", urlScript.getFQN("http://xxx/b.sah"));
  }

  private class MockURLScript extends URLScript {
    public MockURLScript(String fileName) {
      super(fileName);
    }

    protected void loadScript(String fileName) {
    }
  }

}
