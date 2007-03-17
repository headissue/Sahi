package net.sf.sahi.test;

import junit.framework.TestCase;

/**
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

public class TestLauncherTest extends TestCase {
    public TestLauncher testLauncher;
    public String launchURL;

    protected void setUp() throws Exception {
        testLauncher = new TestLauncher("script.sah", "http://www.starturl.com");
        testLauncher.setSessionId("sessionId");
        launchURL = "http://auto?startUrl=http://www.starturl.com&sessionId=123";
    }

    public void testEscapeForWindows() {
        testLauncher.setBrowser("C:\\ie.exe");
        assertEquals("\"C:\\ie.exe\"  \"http://auto?startUrl=http://www.starturl.com&sessionId=123\"",
                testLauncher.buildCommandForWindows(launchURL));
    }

    public void testEscapeForNonWindows() {
        testLauncher.setBrowser("/usr/programs/firefox");
        assertEquals("/usr/programs/firefox http://auto?startUrl=http://www.starturl.com&sessionId=123",
                testLauncher.buildCommandForNonWindows(launchURL));
    }


}
