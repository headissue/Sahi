package net.sf.sahi.ssl;

import junit.framework.TestCase;

public class SSLHelperTest extends TestCase {
	public void testSSLCommand() {
		assertEquals("keytool.exe -genkey -alias www.sahi.co.in  -keypass pwd -storepass pwd -keyalg RSA -keystore filekarasta -dname \"CN=www.sahi.co.in, OU=Sahi, O=Sahi, L=Bangalore, S=Karnataka, C=IN\"" ,new SSLHelper().getSSLCommand("www.sahi.co.in", "filekarasta", "pwd", "keytool.exe").trim());
	}
}
