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
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.PEMWriter;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

import javax.net.ssl.*;
import java.io.*;
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
  private final Provider bouncyCastleProvider = new BouncyCastleProvider();
  private final String keystorePassword = Configuration.getSSLPassword();
  private final String rootCAHost = Configuration.getCommonDomain();
  private X509Certificate rootCA;

  private static final Logger logger = Logger.getLogger("net.sf.sahi.ssl.SSLHelper");
  static HashMap<String, SSLSocketFactory> sslSocketFactories = new HashMap<>();
  public final String rootCAPath = Configuration.getRootCaPath();
  private KeyStore clientCertKeystore;

  public static SSLHelper getInstance() {
    return instance;
  }

  private SSLSocketFactory getSSLClientSocketFactory(String domain) throws Exception {
    if (!sslSocketFactories.containsKey(domain)) {
      putKeyInKeyStore(domain);
      final SSLSocketFactory socketFactory = createSocketFactory(domain);
      if (socketFactory != null) {
        sslSocketFactories.put(domain, socketFactory);
      }
    }
    return sslSocketFactories.get(domain);
  }

  public void checkRootCA() throws Exception {
    createKeystore();
    createClientKeyStore();
    KeyPair _keyPair;
    Key _privateKey;
    String privateKeyPath = Configuration.getRootKeyPath();

    if (!new File(rootCAPath).exists() || !new File(privateKeyPath).exists()) {
      System.out.println("rootCA missing, creating new one in " + rootCAPath);
      _keyPair = newKeyPair();
      _privateKey = _keyPair.getPrivate();
      createRootCA(_keyPair);
      writePEMObject(rootCAPath, rootCA);
      System.out.println("Created new root CA in " + rootCAPath);
      writePEMObject(Configuration.getRootKeyPath(), _keyPair.getPrivate());
    } else {
      _privateKey = readPrivateKey(privateKeyPath);
      Object object = readWithPemParser(Configuration.getRootCaPath());
      rootCA = new JcaX509CertificateConverter().setProvider(BouncyCastleProvider.PROVIDER_NAME)
        .getCertificate((X509CertificateHolder) object);
    }

    addCertToKeystore(rootCA, _privateKey, Configuration.getRootCaName());
  }

  private PrivateKey readPrivateKey(String privateKeyPath) throws IOException {
    return (new JcaPEMKeyConverter().getKeyPair((PEMKeyPair) new PEMParser(new FileReader(privateKeyPath)).readObject())).getPrivate();
  }

  private void createClientKeyStore() throws Exception {
    clientCertKeystore = KeyStore.getInstance(Configuration.getSSLClientKeyStoreType());
    String clientCertPath = Configuration.getSSLClientCertPath();
    FileInputStream fileInputStream = null;
    if (clientCertPath != null) {
      try {
        fileInputStream = new FileInputStream(clientCertPath);
      } catch (IOException ioe) {
        //ioe.printStackTrace();
        logger.warning("\n----\nCertificate not found: " + clientCertPath + "\n----");
      }
    }
    String password = Configuration.getSSLClientCertPassword();
    char[] passphrase = password == null ? null : password.toCharArray();
    clientCertKeystore.load(fileInputStream, passphrase);
  }

  private Object readWithPemParser(String source) throws IOException {
    PEMParser parser = new PEMParser(new FileReader(source));
    return parser.readObject();
  }

  public KeyStore getKeyStore() {
    return keystore;
  }

  private SSLSocketFactory createSocketFactory(String domain) {
    SSLSocketFactory factory;
    try {
      KeyManagerFactory keyManagerFactory = getKeyManagerFactory(keystore, keystorePassword);

      SSLContext sslContext = SSLContext.getInstance("SSLv3");
      KeyManager[] keyManagers = keyManagerFactory.getKeyManagers();
      keyManagers = wrapKeyManagers(keyManagers, domain);
      sslContext.init(keyManagers, getAllTrustingManager(), new SecureRandom());
      factory = sslContext.getSocketFactory();
      return factory;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return (SSLSocketFactory) SSLSocketFactory.getDefault();
  }

  KeyManager[] wrapKeyManagers(KeyManager[] kma, String domain) {
    KeyManager[] kma2 = new KeyManager[kma.length];
    for (int i = 0; i < kma2.length; i++) {
      kma2[i] = new X509KeyMangerWrapper((X509KeyManager) kma[i], domain);
    }
    return kma2;
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


  public KeyManagerFactory getKeyManagerFactoryForRemoteFetch() throws UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException, CertificateException, IOException {
    String fileWithPath = Configuration.getSSLClientCertPath();
    logger.info(fileWithPath == null ? "No SSL Client Cert specified" : ("\n----\nSSL Client Cert Path = " + fileWithPath + "\n----"));
    return getKeyManagerFactory(clientCertKeystore, Configuration.getSSLClientCertPassword());
  }

  public KeyManagerFactory getKeyManagerFactory(KeyStore keystore, String password) throws NoSuchAlgorithmException, KeyStoreException, IOException, CertificateException, UnrecoverableKeyException {
    char[] passphrase = password == null ? null : password.toCharArray();
    KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(Configuration.getSSLAlgorithm());

    keyManagerFactory.init(keystore, passphrase);
    return keyManagerFactory;
  }

  protected void putKeyInKeyStore(final String domain) throws Exception {
    System.out.println("Generate key for: " + domain);
    KeyPair _keyPair = newKeyPair();
    X509Certificate _cert = buildCert(domain, _keyPair, false);
    writePEMObject(Configuration.getCertsPath()+"/"+ domain, _cert);
    addCertToKeystore(_cert, _keyPair.getPrivate(), domain);
  }

  private void createKeystore() throws Exception {
    if (keystore == null) {
      Security.addProvider(new BouncyCastleProvider());
      keystore = KeyStore.getInstance(Configuration.getSSLClientKeyStoreType(), bouncyCastleProvider);
      keystore.load(null, keystorePassword.toCharArray());
    }
  }

  private KeyPair newKeyPair() {
    KeyPairGenerator keyPairGenerator = null;
    try {
      keyPairGenerator = KeyPairGenerator.getInstance("RSA", bouncyCastleProvider.getName());
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    } catch (NoSuchProviderException e) {
      throw new RuntimeException(e);
    }
    keyPairGenerator.initialize(2048, new SecureRandom());
    return keyPairGenerator.generateKeyPair();
  }

  private X500Name buildRootIssuer() {
    return buildName(Configuration.getRootCaName());

  }

  public void writePEMObject(String target, Object o) throws IOException, CertificateEncodingException {
    // when writing new cert or key or anything, make sure it does not exist.. by deleting
    try {
      new File(target).delete();
    } catch (Exception e) {
    }

    PEMWriter writer = new PEMWriter(new FileWriter(target));
    writer.writeObject(o);
    writer.flush();
    writer.close();
  }

  /**
   * We need a root CA to add to the browser under which all certificates will be trusted.
   *
   * @throws Exception
   */
  private void createRootCA(KeyPair _keyPair) throws Exception {
    rootCA = buildCert(Configuration.getRootCaName(), _keyPair, true);
  }

  public X509Certificate buildCert(String domain, KeyPair _keyPair, boolean isRoot) throws Exception {
    X500Name issuer = buildRootIssuer();
    X500Name subject = buildName(domain);
    Date startDate = new Date();
    long aYear = 1000 * 60 * 60 * 24 * 365;
    Date expiryDate = new Date(startDate.getTime() + aYear);
    SubjectPublicKeyInfo subjectPublicKeyInfo = getSubjectPublicKeyInfo(_keyPair);
    X509v3CertificateBuilder certificateBuilder = new X509v3CertificateBuilder(issuer, BigInteger.valueOf(new Date().getTime()), new Date(), expiryDate, subject, subjectPublicKeyInfo);
    if (isRoot) {
      certificateBuilder.addExtension(Extension.basicConstraints, true, new BasicConstraints(true));
    }

    ContentSigner signer;
    if (isRoot) {
      signer = new JcaContentSignerBuilder("SHA256WithRSAEncryption").setProvider(bouncyCastleProvider).build(_keyPair.getPrivate());
    } else {
      PrivateKey privateRootKey;
      privateRootKey = (PrivateKey) keystore.getKey(Configuration.getRootCaName(), keystorePassword.toCharArray());
      signer = new JcaContentSignerBuilder("SHA256WithRSAEncryption").setProvider(bouncyCastleProvider).build(privateRootKey);
    }
    X509Certificate thisCert = new JcaX509CertificateConverter().setProvider(bouncyCastleProvider)
      .getCertificate(certificateBuilder.build(signer));
    return thisCert;
  }

  private X500Name buildName(String domain) {
    X500NameBuilder builder = new X500NameBuilder(BCStyle.INSTANCE);
    builder.addRDN(BCStyle.OU, "infrastructure");
    builder.addRDN(BCStyle.O, "headissue GmbH");
    builder.addRDN(BCStyle.CN, domain);
    builder.addRDN(BCStyle.L, "Munich");
    builder.addRDN(BCStyle.ST, "Bavaria");
    builder.addRDN(BCStyle.C, "DE");
    return builder.build();
  }

  private void addCertToKeystore(X509Certificate _cert, Key _privateKey, String domain) throws KeyStoreException {
    X509Certificate[] chain;
    if (_cert != rootCA) {
      chain = new X509Certificate[2];
      chain[0] = _cert;
      chain[1] = rootCA;
    } else {
      chain = new X509Certificate[]{_cert};
    }
    keystore.setKeyEntry(domain, _privateKey, keystorePassword.toCharArray(), chain);
  }

  private SubjectPublicKeyInfo getSubjectPublicKeyInfo(KeyPair _keyPair) {
    byte[] encodedPublicKey = _keyPair.getPublic().getEncoded();
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

  public Socket getSocket(final HttpRequest request, final InetAddress addr, final int port) throws Exception {
    SSLSocketFactory sslFact = getSSLClientSocketFactory(addr.getHostName());
    SSLSocket socket = (SSLSocket) sslFact.createSocket(addr, port);
    socket.setUseClientMode(true);
    socket.setEnabledCipherSuites(socket.getSupportedCipherSuites());
    return socket;
  }

  public SSLSocket convertToSecureSocket(final Socket plainSocket, final String domain) throws Exception {
    try {
      return (SSLSocket) getSSLClientSocketFactory(domain).createSocket(plainSocket,
        plainSocket.getInetAddress().getHostName(), plainSocket.getPort(), true);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  public SSLSocket convertToSecureServerSocket(final Socket socket, final String domain) throws Exception {
    SSLSocket sslSocket = convertToSecureSocket(socket, domain);
    sslSocket.setUseClientMode(false);
    return sslSocket;
  }
}
