package net.sf.sahi.test;

import junit.framework.TestCase;

public class SingleSessionTestRunnerTest extends TestCase {
	private static final long serialVersionUID = 4687325716286230955L;

	public void testSingleBrowserSession() throws Exception {
		String browserType = "firefox4";
		String base = "http://gramam/demo/";

		SingleSessionTestRunner testRunner = new SingleSessionTestRunner("my_session", browserType, base);
		testRunner.start();
		
		assertEquals("FAILURE", testRunner.executeSingleTest("D:/Dev/Sahi/sahi_os/userdata/scripts/demo/clicksTest_1.sah"));
//		assertEquals("SUCCESS", testRunner.executeSingleTest("D:/Dev/Sahi/sahi_os/userdata/scripts/demo/clicksTest_2.sah"));
		assertEquals("SUCCESS", testRunner.executeSingleTest("D:/Dev/Sahi/sahi_os/userdata/scripts/demo/label.sah"));
		
		String suiteStatus = testRunner.stop();
		System.out.println(suiteStatus);
	}
}
