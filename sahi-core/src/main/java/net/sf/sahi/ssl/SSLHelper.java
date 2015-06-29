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
import net.sf.sahi.util.Utils;
import org.apache.log4j.Logger;
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
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

import javax.net.ssl.*;
import java.io.*;
import java.math.BigInteger;
import java.net.Socket;
import java.security.*;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.HashMap;


public class SSLHelper {

  private static final SSLHelper instance = new SSLHelper();
  private static final String KEYSTORE_PASSWORD = "DUMMYPASSWORD";
  private static final Logger logger = Logger.getLogger(SSLHelper.class);
  static HashMap<String, SSLSocketFactory> sslSocketFactories = new HashMap<>();
  public final String rootCAPath = Configuration.getRootCaPath();
  private final Provider bouncyCastleProvider = new BouncyCastleProvider();
  private KeyStore keystore;
  private X509Certificate rootCA;
  private KeyStore clientCertKeystore;
  private SecureRandom secureRandom = new SecureRandom();

  public static SSLHelper getInstance() {
    return instance;
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

  private SSLSocketFactory getSSLClientSocketFactory(String domain) throws Exception {
    if (!sslSocketFactories.containsKey(domain)) {
      generateKeyAndPutIntoKeyStore(domain);
      final SSLSocketFactory socketFactory = createSocketFactory(domain);
      sslSocketFactories.put(domain, socketFactory);
      return socketFactory;
    }
    return sslSocketFactories.get(domain);
  }

  public void checkRootCA() throws Exception {
    createKeystore();
    createClientKeyStore();
    if (!new File(rootCAPath).exists() || !new File(Configuration.getRootKeyPath()).exists()) {
      createRootCA();
    } else {
      readRootCA();
    }
  }

  /**
   * Read certificate and adds it to the keystore.
   *
   * @throws IOException
   * @throws CertificateException
   * @throws KeyStoreException
   */
  private void readRootCA() throws IOException, CertificateException, KeyStoreException {
    Key _privateKey = readPrivateKey(Configuration.getRootKeyPath());
    X509CertificateHolder holder =
      (X509CertificateHolder) readWithPemParser(Configuration.getRootCaPath());
    rootCA =
      new JcaX509CertificateConverter().setProvider(BouncyCastleProvider.PROVIDER_NAME).getCertificate(holder);
    keystore.setKeyEntry(Configuration.getRootCaName(), _privateKey, KEYSTORE_PASSWORD.toCharArray(), new X509Certificate[]{rootCA});
  }

  private PrivateKey readPrivateKey(String privateKeyPath) throws IOException {
    PEMKeyPair keyPair = (PEMKeyPair) new PEMParser(new FileReader(privateKeyPath)).readObject();
    return new JcaPEMKeyConverter().getKeyPair(keyPair).getPrivate();
  }

  private void createClientKeyStore() throws Exception {
    clientCertKeystore = KeyStore.getInstance(Configuration.getSSLClientKeyStoreType());
    String clientCertPath = Configuration.getSSLClientCertPath();
    FileInputStream fileInputStream = null;
    if (clientCertPath != null) {
      try {
        fileInputStream = new FileInputStream(clientCertPath);
      } catch (IOException ioe) {
        logger.error("\n----\nCertificate not found: " + clientCertPath + "\n----");
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

  protected KeyStore getKeyStore() {
    return keystore;
  }

  private SSLSocketFactory createSocketFactory(String domain) throws Exception {
    SSLSocketFactory factory;
    KeyManagerFactory keyManagerFactory = getKeyManagerFactory(keystore, KEYSTORE_PASSWORD);
    SSLContext sslContext = SSLContext.getInstance("TLS");
    KeyManager[] keyManagers = keyManagerFactory.getKeyManagers();
    keyManagers = wrapKeyManagers(keyManagers, domain);
    sslContext.init(keyManagers, getAllTrustingManager(), secureRandom);
    factory = sslContext.getSocketFactory();
    return factory;
  }

  private KeyManager[] wrapKeyManagers(KeyManager[] kma, String domain) {
    KeyManager[] kma2 = new KeyManager[kma.length];
    for (int i = 0; i < kma2.length; i++) {
      kma2[i] = new X509KeyMangerWrapper((X509KeyManager) kma[i], domain);
    }
    return kma2;
  }

  public KeyManagerFactory getKeyManagerFactoryForRemoteFetch() throws UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException, CertificateException, IOException {
    String fileWithPath = Configuration.getSSLClientCertPath();
    if (fileWithPath != null) logger.info("Using SSL Cient Cert path at: " + fileWithPath);
    return getKeyManagerFactory(clientCertKeystore, Configuration.getSSLClientCertPassword());
  }

  public KeyManagerFactory getKeyManagerFactory(KeyStore keystore, String password) throws NoSuchAlgorithmException, KeyStoreException, IOException, CertificateException, UnrecoverableKeyException {
    char[] passphrase = password == null ? null : password.toCharArray();
    KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(Configuration.getSSLAlgorithm());

    keyManagerFactory.init(keystore, passphrase);
    return keyManagerFactory;
  }

  protected void generateKeyAndPutIntoKeyStore(final String domain) throws Exception {
    KeyPair keyPair = newKeyPair();
    X509Certificate cert = buildSignedCert(domain, keyPair);
    addCertToKeystore(cert, keyPair.getPrivate(), domain);
  }

  /**
   * Creates an empty keystore
   * @throws Exception
   */
  private void createKeystore() throws Exception {
    if (keystore == null) {
      Security.addProvider(bouncyCastleProvider);
      keystore = KeyStore.getInstance(Configuration.getSSLClientKeyStoreType(), bouncyCastleProvider);
      keystore.load(null, KEYSTORE_PASSWORD.toCharArray());
    }
  }

  /**
   * Private/Public Keys for a certificate
   * @return
   * @throws NoSuchProviderException
   * @throws NoSuchAlgorithmException
   */
  private KeyPair newKeyPair() throws NoSuchProviderException, NoSuchAlgorithmException {
    KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA", bouncyCastleProvider.getName());
    keyPairGenerator.initialize(2048, secureRandom);
    return keyPairGenerator.generateKeyPair();
  }

  private X500Name buildRootIssuer() {
    return buildName(Configuration.getRootCaName());
  }

  /**
   * used to write certificates and keys to a file
   * @param target
   * @param o
   * @throws IOException
   * @throws CertificateEncodingException
   */
  public void writePEMObject(String target, Object o) throws IOException, CertificateEncodingException {
    new File(target).delete();
    PEMWriter writer = new PEMWriter(new FileWriter(target));
    writer.writeObject(o);
    writer.close();
  }

  /**
   * We need a root CA as a file to add to the browser under which all certificates will be trusted.
   *
   * @throws Exception
   */
  private void createRootCA() throws Exception {
    KeyPair _keyPair = newKeyPair();
    rootCA = buildRootCert(Configuration.getRootCaName(), _keyPair);
    writePEMObject(rootCAPath, rootCA);
    writePEMObject(Configuration.getRootKeyPath(), _keyPair.getPrivate());
    keystore.setKeyEntry(Configuration.getRootCaName(), _keyPair.getPrivate(), KEYSTORE_PASSWORD.toCharArray(), new X509Certificate[]{rootCA});
  }

  private X509Certificate buildRootCert(String domain, KeyPair _keyPair) throws Exception {
    X509v3CertificateBuilder certificateBuilder = createX509v3CertificateBuilder(domain, _keyPair);
    certificateBuilder.addExtension(Extension.basicConstraints, true, new BasicConstraints(true));
    return createX509Certificate(certificateBuilder, _keyPair.getPrivate());
  }

  private X509Certificate buildSignedCert(String domain, KeyPair _keyPair) throws Exception {
    PrivateKey privateRootKey = (PrivateKey) keystore.getKey(Configuration.getRootCaName(), KEYSTORE_PASSWORD.toCharArray());
    X509v3CertificateBuilder certificateBuilder = createX509v3CertificateBuilder(domain, _keyPair);
    return createX509Certificate(certificateBuilder, privateRootKey);
  }

  private X509Certificate createX509Certificate(X509v3CertificateBuilder certificateBuilder, PrivateKey privateRootKey) throws OperatorCreationException, CertificateException {
    ContentSigner signer = new JcaContentSignerBuilder("SHA256WithRSAEncryption").setProvider(bouncyCastleProvider).build(privateRootKey);
    return new JcaX509CertificateConverter().setProvider(bouncyCastleProvider)
      .getCertificate(certificateBuilder.build(signer));
  }

  private X509v3CertificateBuilder createX509v3CertificateBuilder(String domain, KeyPair _keyPair) {
    X500Name issuer = buildRootIssuer();
    X500Name subject = buildName(domain);
    Date startDate = new Date();
    long aYear = (long) 1000 * 60 * 60 * 24 * 365;
    Date expiryDate = new Date(startDate.getTime() + aYear);
    SubjectPublicKeyInfo subjectPublicKeyInfo = getSubjectPublicKeyInfo(_keyPair);
    return new X509v3CertificateBuilder(issuer, BigInteger.valueOf(new Date().getTime()), new Date(), expiryDate, subject, subjectPublicKeyInfo);
  }

  private X500Name buildName(String domain) {
    X500NameBuilder builder = new X500NameBuilder(BCStyle.INSTANCE);
    builder.addRDN(BCStyle.O, "*** Sahi Browser Testing Dummy Certificate ***");
    builder.addRDN(BCStyle.CN, domain);
    return builder.build();
  }

  private void addCertToKeystore(X509Certificate _cert, Key _privateKey, String alias) throws KeyStoreException {
    X509Certificate[] chain = new X509Certificate[2];
    chain[0] = _cert;
    chain[1] = rootCA;
    keystore.setKeyEntry(alias, _privateKey, KEYSTORE_PASSWORD.toCharArray(), chain);
  }

  private SubjectPublicKeyInfo getSubjectPublicKeyInfo(KeyPair _keyPair) {
    byte[] encodedPublicKey = _keyPair.getPublic().getEncoded();
    return new SubjectPublicKeyInfo(
      ASN1Sequence.getInstance(encodedPublicKey));
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
}
