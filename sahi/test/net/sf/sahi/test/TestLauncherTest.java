package net.sf.sahi.test;

import junit.framework.TestCase;

/**
 * Sahi - Web Automation and Test Tool
 *
 * Copyright  2006  V Narayan Raman
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
