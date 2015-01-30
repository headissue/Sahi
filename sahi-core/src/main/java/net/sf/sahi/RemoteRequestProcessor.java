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

package net.sf.sahi;

import net.sf.sahi.config.Configuration;
import net.sf.sahi.config.SahiAuthenticator;
import net.sf.sahi.request.HttpRequest;
import net.sf.sahi.response.*;
import net.sf.sahi.session.Session;
import net.sf.sahi.ssl.SSLHelper;
import net.sf.sahi.stream.filter.ChunkedFilter;
import net.sf.sahi.util.ThreadLocalMap;
import net.sf.sahi.util.TrafficLogger;
import net.sf.sahi.util.Utils;
import org.apache.log4j.Logger;

import javax.net.ssl.*;
import java.io.*;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.logging.Level;

import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

public class RemoteRequestProcessor {
  private boolean useStreaming = false;
  private static final Logger logger = Logger.getLogger(RemoteRequestProcessor.class);

  static {
    try {
      SSLContext sslContext = SSLContext.getInstance("SSLv3");
      sslContext.init(SSLHelper.getInstance().getKeyManagerFactoryForRemoteFetch().getKeyManagers(), SSLHelper.getAllTrustingManager(), new java.security.SecureRandom());
      HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
      HostnameVerifier hostnameVerifier = new HostnameVerifier() {
        public boolean verify(String urlHostName, SSLSession session) {
          return true;
        }
      };
      HttpsURLConnection.setDefaultHostnameVerifier(hostnameVerifier);
    } catch (Exception e) {
      logger.warn(Utils.getStackTraceString(e));
    }
    Configuration.setProxyProperties();
    Authenticator.setDefault(new SahiAuthenticator());
  }

  public HttpResponse processHttp(HttpRequest requestFromBrowser) {
    try {
      Session session = requestFromBrowser.session();
      ThreadLocalMap.put("session", session);

      TrafficLogger.storeRequestHeader(requestFromBrowser.rawHeaders(), "unmodified");
      TrafficLogger.storeRequestBody(requestFromBrowser.data(), "unmodified");
      requestFromBrowser.modifyForFetch();
      TrafficLogger.storeRequestHeader(requestFromBrowser.rawHeaders(), "modified");
      String urlStr = requestFromBrowser.url();
      URL url = new URL(urlStr);
      logger.debug(url.toString());
      HttpURLConnection connection = (HttpURLConnection) url.openConnection();
      connection.setDefaultUseCaches(true);
      connection.setUseCaches(true);
      HttpURLConnection.setFollowRedirects(false);
      logger.debug("requestFromBrowser.headers(): " + requestFromBrowser.headers().toString());
      setConnectionRequestHeaders(requestFromBrowser, connection);
      logger.debug("Request headers set on connection: " + getReqHeaders(connection));

      HttpResponse response = null;
      int responseCode = -1;
      try {
        connection.setRequestMethod(requestFromBrowser.method().toUpperCase());
        if (requestFromBrowser.isPost() || requestFromBrowser.isPut()) {
          logger.debug("In post requestFromBrowser.data(): " + requestFromBrowser.data());
          connection.setDoOutput(true);
          OutputStream outputStreamToHost = connection.getOutputStream();
          outputStreamToHost.write(requestFromBrowser.data());
          outputStreamToHost.close();
        }
        InputStream inputStreamFromHost = null;
        responseCode = connection.getResponseCode();
        logger.debug("responseCode: " + responseCode);
        if (responseCode < 400) {
          inputStreamFromHost = connection.getInputStream();
        } else {
          logger.debug("Fetching error stream");
          inputStreamFromHost = connection.getErrorStream();
        }
        boolean isGZIP = "gzip".equals(connection.getContentEncoding());
        logger.debug("isGZIP: " + isGZIP + "; connection.getContentEncoding(): " + connection.getContentEncoding());
        if (isGZIP) {
          logger.debug("Using GZIPInputStream");
          try {
            inputStreamFromHost = new GZIPInputStream(inputStreamFromHost);
          } catch (IOException ioe) {
            // happens for redirects etc. where there is no body. Ignore
          }
        }
        if (responseCode >= 500 && !requestFromBrowser.isAjax()) {
          response = getWrappedResponse(get5xxResponse(responseCode, inputStreamFromHost));
          logger.warn("got 5xxResponse\n request: " + requestFromBrowser + "\n response: " + response);
        } else if (responseCode == 401) {
          response = process401(connection, inputStreamFromHost);
        } else {
          response = getResponse(inputStreamFromHost, connection);
          TrafficLogger.storeResponseHeader(response.headers().toString().getBytes(), "unmodified");
          TrafficLogger.storeResponseBody(response.data(), "unmodified");
        }

        if (requestFromBrowser.isAjax() && responseCode > 300 && responseCode < 308) {
          String redirectedTo = response.getLastSetValueOfHeader("Location");
          if (redirectedTo != null) session.addAjaxRedirect(redirectedTo);
        }

        if (isGZIP) {
          response.removeHeader("Content-Encoding", "gzip");
          if (response instanceof StreamingHttpResponse) response.setContentLength(-1);
        }

      } catch (IOException uhe) {
        if (uhe instanceof SSLHandshakeException)
          uhe.printStackTrace();
          logger.warn("Returning CannotConnectResponse for: " + urlStr);
          logger.debug(Utils.getStackTraceString(uhe));
      }

      if (response == null || responseCode == -1) {
        logger.warn("No response or response code not set");
        response = getNoConnectionResponse(requestFromBrowser);
      }

      if (responseCode != 204 && responseCode != 304) {

        boolean attachment;
        String contentTypeHeader;
        String contentDisposition;

        contentTypeHeader = response.contentTypeHeader();
        attachment = response.isAttachment();
        contentDisposition = response.getLastSetValueOfHeader("Content-Disposition");

        boolean downloadContentType = isDownloadContentType(contentTypeHeader);
        String fileName = null;
        boolean contentDispositionForcesDownload = false;

        if (attachment) {
          fileName = response.getAttachmentFileName();
          contentDispositionForcesDownload = !isMatchingContentTypes(contentTypeHeader, Configuration.attachmentOverrideContentTypes());
        }
        if (fileName == null) {
          fileName = requestFromBrowser.fileName();
        }

        boolean downloadURL = isDownloadURL(urlStr);
        logger.debug("downloadURL = " + downloadURL);
        logger.debug("response.isAttachment() = " + attachment);
        logger.debug("fileName = " + fileName);
        logger.debug("contentTypeHeader = " + contentTypeHeader);
        logger.debug("downloadContentType = " + downloadContentType);
        logger.debug("Content-Disposition=" + contentDisposition);
        if (responseCode == 200 && (downloadContentType || downloadURL)) {
          StringBuilder sb = new StringBuilder();
          sb.append("\n-- Calling downloadFile --\n");
          sb.append(requestFromBrowser.url());
          sb.append("\ndownloadURL = ").append(downloadURL);
          sb.append("\nresponse.isAttachment() = ").append(attachment);
          sb.append("\nfileName = ").append(fileName);
          sb.append("\ncontentTypeHeader = ").append(contentTypeHeader);
          sb.append("\ndownloadContentType = ").append(downloadContentType);
          sb.append("\nContent-Disposition=").append(contentDisposition);
          sb.append("\ncontentDispositionForcesDownload = " + false);
          sb.append("\n--");
          logger.info(sb.toString());
          downloadFile(requestFromBrowser, response, fileName);
          if (session.sendHTMLResponseAfterFileDownload()) {
            response = getWrappedResponse(getFileDownloadedResponse(fileName));
          } else {
            session.set204(true);
            return new NoContentResponse();
          }
        }
        response = addFilters(requestFromBrowser, response, responseCode);
      }
      if (responseCode == 204) {
        session.set204(true);
      }
//			Can be used to induce a delay in playback for testing Sahi
//			if (requestFromBrowser.url().contains("DropDownListExample.swf")) {
//				Thread.sleep(10000);
//			}
      return response;
    } catch (Exception e) {
      logger.warn(Utils.getStackTraceString(e));
      return null;
    }
  }

  private HttpResponse getResponse(InputStream inputStreamFromHost, HttpURLConnection connection) throws IOException {
    final String contentType = connection.getContentType();
    if (useStreaming || (contentType != null && (contentType.contains("video")))) {
      logger.info("Using streaming response for contentType: " + contentType);
      return new StreamingHttpResponse(inputStreamFromHost, connection);
    }
    return new HttpResponse(inputStreamFromHost, connection);
  }

  private HttpResponse getWrappedResponse(HttpResponse response) {
    if (useStreaming) return new StreamingHttpResponse(response);
    return response;
  }

  private HttpResponse addFilters(HttpRequest requestFromBrowser, HttpResponse response, int responseCode) {
    if (response instanceof StreamingHttpResponse) {
      StreamingHttpResponse streamingResponse = (StreamingHttpResponse) response;
      streamingResponse.addFilter(new ChunkedFilter());
      return streamingResponse;
    } else {
      final Session session = requestFromBrowser.session();
      if (!requestFromBrowser.isExcluded() && !session.isAjaxRedirect(requestFromBrowser.url())) {
        return new HttpModifiedResponse2(response, requestFromBrowser.isSSL(), requestFromBrowser.fileExtension(), responseCode);
      }
    }
    return response;
  }

  private HttpResponse process401(HttpURLConnection connection, InputStream inputStreamFromHost)
    throws IOException {
    HttpResponse response;
    response = getResponse(inputStreamFromHost, connection);
    String wwwAuthenticate = response.getLastSetValueOfHeader("WWW-Authenticate");
    List<String> cookieHeaders = response.headers().getHeaders("Set-Cookie");
    logger.info("authentication required: " + wwwAuthenticate);
    if (wwwAuthenticate != null) {
      String scheme = getScheme(wwwAuthenticate);
      String realm = getRealm(wwwAuthenticate);
      logger.info("scheme=" + scheme + "; realm=" + realm);
      Properties props = new Properties();
      props.put("realm", "" + realm);
      props.put("scheme", "" + scheme);
      props.put("authKey", "\"" + Utils.makeString((realm != null) ? realm : scheme) + "\"");

      if (!"ntlm".equals(scheme)) {
        String message = "";
        try {
          message = new String(Utils.getBytes(inputStreamFromHost));
        } catch (Exception e) {
          logger.info(Utils.getStackTraceString(e));
        }
        props.put("message", message);
      }
      response = getWrappedResponse(new HttpFileResponse(Configuration.getHtdocsRoot() + "/spr/401.htm", props, false, false));
      response.headers().addHeaders("Set-Cookie", cookieHeaders);
    }
    return response;
  }

  String getRealm(String wwwAuthenticate) {
    int ix = wwwAuthenticate.indexOf("realm=");
    if (ix == -1) return null;
    ix = ix + 6;
    int ixComma = wwwAuthenticate.indexOf(',', ix + 1);
    if (ixComma == -1) ixComma = wwwAuthenticate.length();
    wwwAuthenticate = wwwAuthenticate.substring(ix, ixComma).trim();
    if (wwwAuthenticate.startsWith("\"")) wwwAuthenticate = wwwAuthenticate.substring(1);
    if (wwwAuthenticate.endsWith("\"")) wwwAuthenticate = wwwAuthenticate.substring(0, wwwAuthenticate.length() - 1);
    return wwwAuthenticate;
  }

  String getScheme(String wwwAuthenticate) {
    int ix = wwwAuthenticate.indexOf(" ");
    if (ix == -1) return wwwAuthenticate.toLowerCase();
    return wwwAuthenticate.substring(0, ix).trim().toLowerCase();
  }

  private void downloadFile(HttpRequest requestFromBrowser, HttpResponse response, String fileName) {
    Session session = requestFromBrowser.session();
    save(response, requestFromBrowser.session().id() + "__" + fileName);
    logger.debug("Setting download_lastFile = " + fileName + "\nSession Id: " + session.id());
    session.setVariable("download_lastFile", fileName);
  }

  public void save(HttpResponse response, final String fileName) {
      logger.info("Downloading " + fileName + " to temp directory:\n" + Configuration.tempDownloadDir());
    try {
      File file = new File(Configuration.tempDownloadDir(), fileName);
      if (file.exists()) {
        file.delete();
      }
      file.createNewFile();
      FileOutputStream out;
      out = new FileOutputStream(file);
      response.sendBody(out);
      out.flush();
      out.close();
    } catch (IOException e) {
        logger.warn("Could not write to file");
        logger.warn(Utils.getStackTraceString(e));
    }
  }

  private boolean isDownloadURL(final String url) {
    String[] list = Configuration.getDownloadURLList();
    for (int i = 0; i < list.length; i++) {
      String pattern = list[i];
      if (url.matches(pattern.trim())) {
        return true;
      }
    }
    return false;
  }

  protected boolean isDownloadContentType(String contentType) {
    Pattern pattern = Configuration.getDownloadContentTypesRegExp();
    return isMatchingContentTypes(contentType, pattern);
  }

  private boolean isMatchingContentTypes(String contentType, Pattern p) {
    if (contentType == null || contentType.equals("")) {
      return false;
    }
    contentType = contentType.toLowerCase();
    logger.debug(Configuration.getDownloadContentTypesRegExp());
    return p.matcher(contentType).matches();
  }


  private HttpResponse getFileDownloadedResponse(String fileName) {
    Properties props = new Properties();
    props.put("fileName", "" + fileName);

    return new HttpFileResponse(
      Configuration.getHtdocsRoot() + "spr/downloaded.htm",
      props, false, true);
  }

  private HttpResponse get5xxResponse(int responseCode, InputStream inputStreamFromHost) {
    Properties props = new Properties();
    props.put("responseCode", "" + responseCode);
    props.put("time", "" + (new Date()));

    String message = "";
    try {
      message = new String(Utils.getBytes(inputStreamFromHost));
    } catch (Exception e) {
      logger.info("Caught: " + Utils.getStackTraceString(e));
      message = "";
    }
    props.put("message", message);

    return new HttpFileResponse(
      Configuration.getHtdocsRoot() + "spr/5xx.htm",
      props, false, true);
  }

  private HttpResponse getNoConnectionResponse(HttpRequest requestFromBrowser) {
    Properties props = new Properties();
    props.put("time", "" + (new Date()));

    String message = "No response for " + requestFromBrowser;
    props.put("message", message);

    return new HttpFileResponse(
      Configuration.getHtdocsRoot() + "spr/cannotConnect.htm",
      props, false, true);
  }

  private String getReqHeaders(HttpURLConnection connection) {
    StringBuilder sb = new StringBuilder();
    Map<String, List<String>> requestProperties = connection.getRequestProperties();
    Iterator<String> iterator = requestProperties.keySet().iterator();
    while (iterator.hasNext()) {
      String key = iterator.next();
      sb.append(key + " = " + requestProperties.get(key) + "\n");
    }
    return sb.toString();
  }

//	private HttpResponse getExceptionResponse(HttpRequest requestFromBrowser, Exception e) {
//		return new HttpModifiedResponse(new SimpleHttpResponse(Utils.getStackTraceString(e, true)),
//				requestFromBrowser.isSSL(), requestFromBrowser.fileExtension());
//	}

  private void setConnectionRequestHeaders(HttpRequest requestFromBrowser, HttpURLConnection connection) {
    HttpHeaders headers = requestFromBrowser.headers();
    Iterator<String> iterator = headers.keysIterator();
    while (iterator.hasNext()) {
      String key = iterator.next();
      String value = headers.getHeader(key);
      connection.addRequestProperty(key, value);
    }
  }

  private HttpResponse getCannotConnectResponse(Exception e) {
    try {
      Properties props = new Properties();
      props.put("message", "" + e.getMessage());
      props.put("exception", "" + Utils.getStackTraceString(e, true));
      final HttpFileResponse httpFileResponse = new HttpFileResponse(
        Configuration.getHtdocsRoot() + "spr/cannotConnect.htm",
        props, false, true);
      return httpFileResponse;
    } catch (Exception e1) {
      logger.warn("Could not send getCannotConnectResponse");
      logger.warn(Utils.getStackTraceString(e1));
      return new SimpleHttpResponse("");
    }
  }
}
