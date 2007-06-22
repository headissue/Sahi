package net.sf.sahi.ssl;

import java.util.ArrayList;

import net.sf.sahi.util.Utils;

import junit.framework.TestCase;

public class SSLHelperTest extends TestCase {
    public void xtestSSLCommand() {
        SSLHelper helper = new SSLHelper();
        assertEquals("keytool.exe -genkey -alias www.sahi.co.in -keypass pwd -storepass pwd -keyalg RSA -keystore filekarasta -dname \"CN=www.sahi.co.in, OU=Sahi, O=Sahi, L=Bangalore, S=Karnataka, C=IN\"" , helper.getPrintableSSLCommand(helper.getSSLCommand("www.sahi.co.in", "filekarasta", "pwd", "keytool.exe")).trim());
    }

    public void testTokenizer() {
        String s = "keytool.exe -dname \"CN=www.sahi.co.in, OU=Sahi\"";
        String[] commandTokens = Utils.getCommandTokens(s);
        for (int i=0; i<commandTokens.length; i++){
            System.out.println(commandTokens[i]);
        }
    }

}
