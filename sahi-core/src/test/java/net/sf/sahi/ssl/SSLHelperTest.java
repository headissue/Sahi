package net.sf.sahi.ssl;

import net.sf.sahi.config.Configuration;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

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
public class SSLHelperTest {

  @Before
  public void setup() {
    Configuration.init();
    try {
      new File(Configuration.getRootCaPath()).delete();
      new File(Configuration.getRootKeyPath()).delete();
    } catch (Exception e) {
    }
  }

  @Test
  public void createKeyStoreWriteRootCaWriteKey() throws Exception {
    // create root ca and keystore
    SSLHelper sslHelper = SSLHelper.getInstance();
    sslHelper.checkRootCA();
    KeyStore _keystore = sslHelper.getKeyStore();
    assertTrue(_keystore != null);
    //is root ca in keystore?
    X509Certificate rootCA;
    try {
      rootCA = (X509Certificate) _keystore.getCertificate(Configuration.getRootCaName());
    } catch (KeyStoreException e) {
      throw new RuntimeException(e);
    }
    assertTrue(rootCA != null);
    assertTrue(new File(Configuration.getRootKeyPath()).exists());
    assertTrue(new File(Configuration.getRootCaPath()).exists());
  }

  @Test
  public void testChainOfTrust() throws Exception {
    SSLHelper sslHelper = SSLHelper.getInstance();
    sslHelper.checkRootCA();
    sslHelper.putKeyInKeyStore("www.test.com");
    KeyStore _keystore = sslHelper.getKeyStore();
    Certificate[] certificateChain = _keystore.getCertificateChain("www.test.com");
    System.out.println(_keystore);
    assertEquals(2, certificateChain.length);
    X509Certificate _cert = (X509Certificate) _keystore.getCertificate("www.test.com");
    System.out.println(_cert);


  }
}
