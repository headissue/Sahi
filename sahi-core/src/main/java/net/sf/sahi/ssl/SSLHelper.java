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
package net.sf.sahi.ssl;

import net.sf.sahi.config.Configuration;
import net.sf.sahi.request.HttpRequest;
import net.sf.sahi.util.Utils;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.CertIOException;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMWriter;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

import javax.net.ssl.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.Socket;
import java.security.*;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.HashMap;
import java.util.logging.Logger;

public class SSLHelper {

  private static final SSLHelper instance = new SSLHelper();

  private KeyStore keystore;
  private KeyPair keyPair;
  private final Provider bouncyCastleProvider = new BouncyCastleProvider();
  private final String keystorePassword = Configuration.getSSLPassword();
  private final String rootCAHost = Configuration.getCommonDomain();
  private X509Certificate rootCA;

  private static final Logger logger = Logger.getLogger("net.sf.sahi.ssl.SSLHelper");
  static HashMap<String, SSLSocketFactory> sslSocketFactories = new HashMap<>();
  public final String rootCAPath = Configuration.getRootCaPath();

  public static SSLHelper getInstance() {
    return instance;
  }

  private SSLSocketFactory getSSLClientSocketFactory(String domain) throws IOException {
    if (domain == null) {
      domain = Configuration.getCommonDomain();
    }
   if (!sslSocketFactories.containsKey(domain)) {
      putKeyInKeyStore(domain);
      final SSLSocketFactory socketFactory = createSocketFactory();
      if (socketFactory != null) {
        sslSocketFactories.put(domain, socketFactory);
      }
    }
    return sslSocketFactories.get(domain);
  }

  public void checkRootCA() {
    if (!new File(rootCAPath).exists()) {
      System.out.println("rootCA missing, createing new one in " + rootCAPath);
      try {
        createKeystore();
        createRootCA();
        addCertToKeystore(rootCA, rootCAHost);
        writeCertificateToFile(rootCAPath, rootCA);
        System.out.println("Created new root CA in " + rootCAPath);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
  }

  public KeyStore getKeyStore() {
    return keystore;
  }

  /**
   * Wrapper for the key manager to select the correct server key within a single
   * key store.
   *
   * @author Jens Wilke
   */
  public static class X509KeyMangerWrapper implements X509KeyManager {

    X509KeyManager wrapped;
    String domain;

    public X509KeyMangerWrapper(X509KeyManager wrapped, String domain) {
      this.wrapped = wrapped;
      this.domain = domain;
    }

    public String[] getClientAliases(String s, Principal[] principals) {
      return wrapped.getClientAliases(s, principals);
    }

    public String chooseClientAlias(String[] strings, Principal[] principals, Socket socket) {
      return wrapped.chooseClientAlias(strings, principals, socket);
    }

    public String[] getServerAliases(String s, Principal[] principals) {
      return wrapped.getServerAliases(s, principals);
    }

    /**
     * the socket factory calls this through the ssl context
     * first with EC_EC and then with RSA. The normal key store implementation
     * returns the last certificate. Return the CN for the domain we want to
     * connect. This certificate and key is also existing in the keystore.
     */
    public String chooseServerAlias(String s, Principal[] principals, Socket socket) {
      return domain;
    }

    public X509Certificate[] getCertificateChain(String s) {
      return wrapped.getCertificateChain(s);
    }

    public PrivateKey getPrivateKey(String s) {
      return wrapped.getPrivateKey(s);
    }
  }

  KeyManager[] wrapKeyManagers(KeyManager[] kma, String domain) {
    KeyManager[] kma2 = new KeyManager[kma.length];
    for (int i = 0; i < kma2.length; i++) {
      kma2[i] = new X509KeyMangerWrapper((X509KeyManager) kma[i], domain);
    }
    return kma2;
  }

  private SSLSocketFactory createSocketFactory() {
    SSLSocketFactory factory;
    try {
      KeyManagerFactory keyManagerFactory = getKeyManagerFactory();

      SSLContext sslContext = SSLContext.getInstance("SSLv3");
      KeyManager[] keyManagers = keyManagerFactory.getKeyManagers();
      sslContext.init(keyManagers, getAllTrustingManager(), new SecureRandom());
      factory = sslContext.getSocketFactory();
      return factory;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return (SSLSocketFactory) SSLSocketFactory.getDefault();
  }

  public KeyManagerFactory getKeyManagerFactoryForRemoteFetch() throws UnrecoverableKeyException, NoSuchAlgorithmException,
    KeyStoreException, CertificateException, IOException {
    String fileWithPath = Configuration.getSSLClientCertPath();
    logger.info(fileWithPath == null ? "No SSL Client Cert specified" : ("\n----\nSSL Client Cert Path = " + fileWithPath + "\n----"));
    return getKeyManagerFactory();
  }

  public KeyManagerFactory getKeyManagerFactory()
    throws NoSuchAlgorithmException, KeyStoreException, IOException,
    CertificateException, UnrecoverableKeyException {
    char[] passphrase = keystorePassword == null ? null : keystorePassword.toCharArray();
    KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(Configuration.getSSLAlgorithm());

    keyManagerFactory.init(keystore, passphrase);
    return keyManagerFactory;
  }


  // TODO replace with java keystore object
  private void putKeyInKeyStore(final String domain) {
    // get or create keystore
    if (keystore == null) {
      checkRootCA();
      // has private key for domain
    }
    PrivateKey key = null;
    try {
      key = (PrivateKey) keystore.getKey(domain, keystorePassword.toCharArray());
    } catch (KeyStoreException e) {
      throw new RuntimeException(e);
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    } catch (UnrecoverableKeyException e) {
      throw new RuntimeException(e);
    }
    if (key == null) {
      System.err.println("Generate key for: " + domain);

      newKeyPair();
      X509Certificate _cert = null;
      try {
        _cert = createCert(domain, false);
      } catch (CertificateException e) {
        throw new RuntimeException(e);
      } catch (OperatorCreationException e) {
        throw new RuntimeException(e);
      }
      try {
        addCertToKeystore(_cert, domain);
      } catch (KeyStoreException e) {
        throw new RuntimeException(e);
      }
    }
    // no, create one
  }

  private void createKeystore() throws Exception {

    Security.addProvider(new BouncyCastleProvider());
    keystore = KeyStore.getInstance(Configuration.getSSLClientKeyStoreType(), bouncyCastleProvider);
    newKeyPair();
    keystore.load(null, keystorePassword.toCharArray());
  }

  private void newKeyPair() {
    KeyPairGenerator keyPairGenerator = null;
    try {
      keyPairGenerator = KeyPairGenerator.getInstance("RSA", bouncyCastleProvider.getName());
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    } catch (NoSuchProviderException e) {
      throw new RuntimeException(e);
    }
    keyPairGenerator.initialize(2048, new SecureRandom());
    keyPair = keyPairGenerator.generateKeyPair();
  }

  private X500Name buildIssuer(String domain) {
    X500NameBuilder builder = new X500NameBuilder(BCStyle.INSTANCE);
    builder.addRDN(BCStyle.OU, "infrastructure");
    builder.addRDN(BCStyle.O, "headissue GmbH");
    builder.addRDN(BCStyle.CN, domain);
    builder.addRDN(BCStyle.L, "Munich");
    builder.addRDN(BCStyle.ST, "Bavaria");
    builder.addRDN(BCStyle.C, "DE");
    return builder.build();
  }

  public void writeCertificateToFile(String target, X509Certificate cert) throws IOException, CertificateEncodingException {

    PEMWriter writer = new PEMWriter(new FileWriter(target));
    writer.writeObject(cert);
    writer.flush();
    writer.close();

 /*   final FileOutputStream os = new FileOutputStream(target);
    os.write("-----BEGIN CERTIFICATE-----\n".getBytes("US-ASCII"));
    os.write(Base64.encode(cert.getEncoded()));
    os.write("-----END CERTIFICATE-----\n".getBytes("US-ASCII"));
    os.close(); */
  }

  /**
   * We need a root CA to add to the browser under which all certificates will be trusted.
   *
   * @throws Exception
   */
  private void createRootCA() throws CertificateException, OperatorCreationException {
    rootCA = createCert(rootCAHost, true);
  }

  public X509Certificate createCert(String domain, boolean isRoot) throws CertificateException, OperatorCreationException {
    X500Name issuer = buildIssuer(domain);
    Date startDate = new Date();
    long aYear = 1000 * 60 * 24 * 365;
    Date expiryDate = new Date(startDate.getTime() + aYear);
    SubjectPublicKeyInfo subjectPublicKeyInfo = getSubjectPublicKeyInfo();
    X509v3CertificateBuilder certificateBuilder = new X509v3CertificateBuilder(issuer, BigInteger.valueOf(1), new Date(), expiryDate, issuer, subjectPublicKeyInfo);
    if (isRoot) {
      try {
        certificateBuilder.addExtension(Extension.basicConstraints, true, new BasicConstraints(true));
      } catch (CertIOException e) {
        throw new RuntimeException(e);
      }
    }

    ContentSigner signer = new JcaContentSignerBuilder("SHA256WithRSAEncryption").setProvider(bouncyCastleProvider).build(keyPair.getPrivate());

    return new JcaX509CertificateConverter().setProvider(bouncyCastleProvider)
      .getCertificate(certificateBuilder.build(signer));
  }

  private void addCertToKeystore(X509Certificate _cert, String domain) throws KeyStoreException {
    keystore.setKeyEntry(domain, keyPair.getPrivate(), keystorePassword.toCharArray(), new java.security.cert.Certificate[]{_cert});
  }

  private SubjectPublicKeyInfo getSubjectPublicKeyInfo() {
    byte[] encodedPublicKey = keyPair.getPublic().getEncoded();
    return new SubjectPublicKeyInfo(
      ASN1Sequence.getInstance(encodedPublicKey));
  }

  private synchronized static void executeCommand(String command) throws Exception {
    Utils.executeCommand(Utils.getCommandTokens(command));
  }

  public static TrustManager[] getAllTrustingManager() {
    TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {

      public X509Certificate[] getAcceptedIssuers() {
        return null;
      }

      public void checkClientTrusted(X509Certificate[] certs, String authType) {
      }

      public void checkServerTrusted(X509Certificate[] certs, String authType) {
      }
    }};
    return trustAllCerts;
  }

  public Socket getSocket(final HttpRequest request, final InetAddress addr, final int port) throws IOException {
    SSLSocketFactory sslFact = getSSLClientSocketFactory(addr.getHostName());
    SSLSocket socket = (SSLSocket) sslFact.createSocket(addr, port);
    socket.setUseClientMode(true);
    socket.setEnabledCipherSuites(socket.getSupportedCipherSuites());
    return socket;
  }

  public SSLSocket convertToSecureSocket(final Socket plainSocket, final String domain) {
    try {
      return (SSLSocket) getSSLClientSocketFactory(domain).createSocket(plainSocket,
        plainSocket.getInetAddress().getHostName(), plainSocket.getPort(), true);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  public SSLSocket convertToSecureServerSocket(final Socket socket, final String domain) {
    SSLSocket sslSocket = convertToSecureSocket(socket, domain);
    sslSocket.setUseClientMode(false);
    return sslSocket;
  }
}
