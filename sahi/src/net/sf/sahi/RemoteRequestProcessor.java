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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLSession;

import net.sf.sahi.config.Configuration;
import net.sf.sahi.config.SahiAuthenticator;
import net.sf.sahi.request.HttpRequest;
import net.sf.sahi.response.HttpFileResponse;
import net.sf.sahi.response.HttpModifiedResponse2;
import net.sf.sahi.response.HttpResponse;
import net.sf.sahi.response.NoContentResponse;
import net.sf.sahi.response.SimpleHttpResponse;
import net.sf.sahi.response.StreamingHttpResponse;
import net.sf.sahi.session.Session;
import net.sf.sahi.ssl.SSLHelper;
import net.sf.sahi.stream.filter.ChunkedFilter;
import net.sf.sahi.util.ThreadLocalMap;
import net.sf.sahi.util.TrafficLogger;
import net.sf.sahi.util.Utils;

public class RemoteRequestProcessor {
	private boolean useStreaming = false;
	private static final Logger logger = Logger.getLogger("net.sf.sahi.RemoteRequestProcessor");
    static {
   	 	try {
			SSLContext sslContext = SSLContext.getInstance("SSL");
			sslContext.init(SSLHelper.getKeyManagerFactoryForRemoteFetch().getKeyManagers(), SSLHelper.getAllTrustingManager(), new java.security.SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
			HostnameVerifier hostnameVerifier = new HostnameVerifier() {
			    public boolean verify(String urlHostName, SSLSession session) {
			        return true;
			    }
			};
			HttpsURLConnection.setDefaultHostnameVerifier(hostnameVerifier);
		} catch (Exception e) {
			logger.warning(Utils.getStackTraceString(e));
		}
    	Configuration.setProxyProperties();
		Authenticator.setDefault(new SahiAuthenticator());
   }

    public HttpResponse processHttp(HttpRequest requestFromBrowser) {
        return processHttp(requestFromBrowser, true);
    }

    public HttpResponse processHttp(HttpRequest requestFromBrowser, boolean modify) {
    	try {
    		ThreadLocalMap.put("session", requestFromBrowser.session());

    		TrafficLogger.storeRequestHeader(requestFromBrowser.rawHeaders(), "unmodified");
    		TrafficLogger.storeRequestBody(requestFromBrowser.data(), "unmodified");
    		requestFromBrowser.modifyForFetch();
    		TrafficLogger.storeRequestHeader(requestFromBrowser.rawHeaders(), "modified");
			String urlStr = requestFromBrowser.url();
			URL url =  new URL(urlStr);
			if (logger.isLoggable(Level.FINE)){
				logger.fine(url.toString());
			}
			HttpURLConnection connection = (HttpURLConnection)url.openConnection();
			connection.setDefaultUseCaches(true);
			connection.setUseCaches(true);
			HttpURLConnection.setFollowRedirects(false);
			if (logger.isLoggable(Level.FINEST)){
				logger.finest("requestFromBrowser.headers():");
				logger.finest(requestFromBrowser.headers().toString());
			}
			setConnectionRequestHeaders(requestFromBrowser, connection);

			if (logger.isLoggable(Level.FINEST)){
				logger.finest("Request headers set on connection:");
				logger.finest(getReqHeaders(connection));
			}			

			HttpResponse response = null;
			int responseCode = -1;
			try{
				connection.setRequestMethod(requestFromBrowser.method().toUpperCase());
				if (requestFromBrowser.isPost() || requestFromBrowser.isPut()){
					if (logger.isLoggable(Level.FINEST)){
						logger.finest("In post requestFromBrowser.data() = " + requestFromBrowser.data());
					}
					connection.setDoOutput(true);
					OutputStream outputStreamToHost = connection.getOutputStream();
					outputStreamToHost.write(requestFromBrowser.data());
					outputStreamToHost.close();
				}
				InputStream inputStreamFromHost = null;
				responseCode = connection.getResponseCode();
				if (logger.isLoggable(Level.FINE)){
					logger.fine("responseCode  = " + responseCode);
				}
				if (responseCode < 400){
					inputStreamFromHost = connection.getInputStream();
		        }else{
		        	if (logger.isLoggable(Level.FINE)){
						logger.fine("Fetching error stream");
					}
		        	inputStreamFromHost = connection.getErrorStream();
		        }
				boolean isGZIP = "gzip".equals(connection.getContentEncoding());
				if (logger.isLoggable(Level.FINER)){
					logger.finer("isGZIP=" + isGZIP + "; connection.getContentEncoding()=" + connection.getContentEncoding());
				}				
				if (isGZIP){
					if (logger.isLoggable(Level.FINE)){
						logger.fine("Using GZIPInputStream");
					}
					inputStreamFromHost = new GZIPInputStream(inputStreamFromHost);
				}
				if (responseCode >= 500 && !requestFromBrowser.isAjax()){
					if (logger.isLoggable(Level.FINE)){
						logger.fine("Returning get5xxResponse");
					}
					response = getWrappedResponse(get5xxResponse(responseCode, inputStreamFromHost));
				} else if (responseCode == 401){
					if (logger.isLoggable(Level.FINE)){
						logger.fine("Returning process401");
					}
					response = process401(connection, inputStreamFromHost);
				}else {
					response = getResponse(inputStreamFromHost, connection);
					TrafficLogger.storeResponseHeader(response.headers().toString().getBytes(), "unmodified");
					TrafficLogger.storeResponseBody(response.data(), "unmodified");
				}
				
				if (requestFromBrowser.isAjax() && responseCode > 300 && responseCode < 306){
					String redirectedTo = response.getLastSetValueOfHeader("Location");
					if (redirectedTo != null) requestFromBrowser.session().addAjaxRedirect(redirectedTo);
					
				}
				
				if (isGZIP) {
					response.removeHeader("Content-Encoding", "gzip");
					if (response instanceof StreamingHttpResponse) response.setContentLength(-1);
				}
				
			}catch(IOException uhe){
				if (uhe instanceof SSLHandshakeException)
					uhe.printStackTrace();
				if (logger.isLoggable(Level.WARNING) && !urlStr.contains("DesignerOutput")){
					logger.warning("Returning CannotConnectResponse for: " + urlStr);
					logger.fine(Utils.getStackTraceString(uhe));
				}
				response = getWrappedResponse(getCannotConnectResponse(uhe));
			}
			if (responseCode != 204 && responseCode != 304){
				String contentTypeHeader = response.contentTypeHeader();
				boolean downloadContentType = isDownloadContentType(contentTypeHeader);
				boolean attachment = response.isAttachment();
				boolean downloadURL = isDownloadURL(urlStr);
				if (logger.isLoggable(Level.FINER)){
					logger.finer("downloadURL = " + downloadURL);
					logger.finer("response.isAttachment() = " + attachment);
					logger.finer("contentTypeHeader = " + contentTypeHeader);
					logger.finer("downloadContentType = " + downloadContentType);
					logger.finer("Content-Disposition=" + response.getLastSetValueOfHeader("Content-Disposition"));
				}
				if (responseCode == 200
						&& (downloadContentType || 
								attachment || 
								downloadURL)) {
					if (logger.isLoggable(Level.INFO)){
						logger.info("Calling downloadFile");
						logger.info(requestFromBrowser.url());
						logger.info("downloadURL = " + downloadURL);
						logger.info("response.isAttachment() = " + attachment);
						logger.info("contentTypeHeader = " + contentTypeHeader);
						logger.info("downloadContentType = " + downloadContentType);
						logger.info("Content-Disposition=" + response.getLastSetValueOfHeader("Content-Disposition"));
					}
					downloadFile(requestFromBrowser, response);
					return new NoContentResponse();
				}
				response = addFilters(requestFromBrowser, modify, response, responseCode);
			}
			if (responseCode == 204){
				requestFromBrowser.session().set204(true);
			}
//			Can be used to induce a delay in playback for testing Sahi
//			Thread.sleep(10000);
            return response;
    	} catch (Exception e) {
    		if (logger.isLoggable(Level.WARNING)){
				logger.warning(Utils.getStackTraceString(e));
			}
			return null;
		}
    }

	private HttpResponse getResponse(InputStream inputStreamFromHost, HttpURLConnection connection) throws IOException {
		final String contentType = connection.getContentType();
		if (useStreaming || (contentType != null && (contentType.contains("video")))){
			logger.info("Using streaming response for contentType: " + contentType);
			return new StreamingHttpResponse(inputStreamFromHost, connection);
		}
		return new HttpResponse(inputStreamFromHost, connection);
	}

    private HttpResponse getWrappedResponse(HttpResponse response){
    	if (useStreaming) return new StreamingHttpResponse(response);
    	return response;
    }
    
	private HttpResponse addFilters(HttpRequest requestFromBrowser, boolean modify, HttpResponse response, int responseCode) {
		if (response instanceof StreamingHttpResponse) {
			StreamingHttpResponse streamingResponse = (StreamingHttpResponse) response;
//			if (modify && !requestFromBrowser.isExcluded() && !requestFromBrowser.session().isAjaxRedirect(requestFromBrowser.url())) {
//				if (logger.isLoggable(Level.FINER)){
//					logger.finer("Modifying response with HttpModifiedResponse");
//				}
//				streamingResponse = new HttpModifiedResponse(streamingResponse, requestFromBrowser.isSSL(), requestFromBrowser
//						.fileExtension(), responseCode);
//			}
//			streamingResponse.addFilter(new TrafficLoggerFilter(TrafficLogger.getLoggerForThread()));
			streamingResponse.addFilter(new ChunkedFilter());
			return streamingResponse;
		} else {
			if (modify && !requestFromBrowser.isExcluded() && !requestFromBrowser.session().isAjaxRedirect(requestFromBrowser.url())) {
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
		if (logger.isLoggable(Level.INFO)){
			logger.info("wwwAuthenticate: " + wwwAuthenticate);
		}
		if (wwwAuthenticate != null){
			String scheme = getScheme(wwwAuthenticate);
			String realm = getRealm(wwwAuthenticate);
			if (logger.isLoggable(Level.INFO)){
				logger.info("scheme=" + scheme + "; realm=" + realm);
			}
			Properties props = new Properties();
			props.put("realm", ""+realm);
			props.put("scheme", ""+scheme);
			props.put("authKey", "\"" + Utils.makeString((realm != null) ? realm : scheme) + "\"");
			
			if (!"ntlm".equals(scheme)) {
				String message = "";
				try{
					message = new String(Utils.getBytes(inputStreamFromHost));
				} catch(Exception e) {
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
		int ixComma = wwwAuthenticate.indexOf(',', ix+1);
		if (ixComma == -1) ixComma = wwwAuthenticate.length();
		wwwAuthenticate = wwwAuthenticate.substring(ix, ixComma).trim();
		if (wwwAuthenticate.startsWith("\"")) wwwAuthenticate = wwwAuthenticate.substring(1);
		if (wwwAuthenticate.endsWith("\"")) wwwAuthenticate = wwwAuthenticate.substring(0, wwwAuthenticate.length()-1);
		return wwwAuthenticate;
	}

	String getScheme(String wwwAuthenticate) {
		int ix = wwwAuthenticate.indexOf(" ");
		if (ix == -1) return wwwAuthenticate.toLowerCase(); 
		return wwwAuthenticate.substring(0, ix).trim().toLowerCase();
	}

	private void downloadFile(HttpRequest requestFromBrowser, HttpResponse response) {
		String fileName = requestFromBrowser.fileName();
		Session session = requestFromBrowser.session();
		save(response, requestFromBrowser.session().id() + "__" + fileName);
		if (logger.isLoggable(Level.INFO)){
			logger.info("Setting download_lastFile = " + fileName);
			logger.info("Session Id: " + session.id());
		}
		session.setVariable("download_lastFile", fileName);
		session.set204(true);
	}


    public void save(HttpResponse response, final String fileName) {
    	if (logger.isLoggable(Level.INFO)){
    		logger.info("Downloading " + fileName + " to temp directory: " + Configuration.tempDownloadDir());
    	}
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
        	if (logger.isLoggable(Level.WARNING)){
        		logger.warning("Could not write to file");
        		logger.warning(Utils.getStackTraceString(e));
        	}
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
        if (contentType == null || contentType.equals("")) {
            return false;
        }
        contentType = contentType.toLowerCase();
//        System.out.println(Configuration.getDownloadContentTypesRegExp());
        Pattern p = Configuration.getDownloadContentTypesRegExp();
        return p.matcher(contentType).matches();
    }

    private HttpResponse get5xxResponse(int responseCode, InputStream inputStreamFromHost) {
		Properties props = new Properties();
		props.put("responseCode", "" + responseCode);
		props.put("time", "" + (new Date()));
		
		String message = "";
		try{
			message = new String(Utils.getBytes(inputStreamFromHost));
		} catch(Exception e) {
			logger.info("Caught: " + Utils.getStackTraceString(e));
			message = "";
		}
		props.put("message", message);
		
		return new HttpFileResponse(
				Configuration.getHtdocsRoot() + "spr/5xx.htm",
				props, false, true);
	}

	private String getReqHeaders(HttpURLConnection connection) {
		StringBuilder sb = new StringBuilder();
		Map<String, List<String>> requestProperties = connection.getRequestProperties();
		Iterator<String> iterator = requestProperties.keySet().iterator();
		while (iterator.hasNext()){
			String key = iterator.next();
			sb.append(key +" = " + requestProperties.get(key) + "\n");
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
		while (iterator.hasNext()){
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
			logger.warning("Could not send getCannotConnectResponse");
			logger.warning(Utils.getStackTraceString(e1));
			return new SimpleHttpResponse("");
		}
    }
}
