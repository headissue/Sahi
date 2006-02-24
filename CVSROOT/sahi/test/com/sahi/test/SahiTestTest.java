package com.sahi.test;

import java.io.IOException;

import junit.framework.TestCase;

public class SahiTestTest extends TestCase {
	public void testProcess() throws IOException, InterruptedException {
		Process p = Runtime.getRuntime().exec("C:\\Program Files\\Mozilla Firefox\\firefox.exe");
		Thread.sleep(2000);
		System.out.println(p.toString());
		p.destroy();
	}

}
