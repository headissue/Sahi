/**
 * Sahi - Web Automation and Test Tool
 *
 * Copyright  2006  V Narayan Raman
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.sf.sahi.request;

import net.sf.sahi.StreamHandler;
import net.sf.sahi.config.Configuration;
import net.sf.sahi.session.Session;
import net.sf.sahi.util.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.util.*;
import java.util.logging.Logger;

/**
 * User: nraman Date: May 13, 2005 Time: 10:01:13 PM
 */
public class HttpRequest extends StreamHandler {
    private String host;
    private int port;
    private String uri;
    private String queryString = "";
    private Map params = new HashMap();
    private Map cookies = null;
    private static final Logger logger = Logger.getLogger("net.sf.sahi.request.HttpRequest");
    private boolean isSSLSocket;
    private boolean isAjax;
    private String fileExtension;
    private String hostWithPort;

    HttpRequest() {
    }

    public HttpRequest(InputStream in) throws IOException {
        this(in, false);
    }

    public HttpRequest(InputStream in, boolean isSSLSocket) throws IOException {
        this.isSSLSocket = isSSLSocket;
        populateHeaders(in, true);
        isAjax = headers().containsKey("Sahi-IsXHR");
        if (isPost())
            populateData(in);
        if (isPost() || isGet() || isConnect()) {
            setHostAndPort();
            setUri();
            setQueryString();
        }
        logger.fine("\nFirst line:" + firstLine());
        logger.fine("isSSL=" + isSSL());
    }

    public String host() {
        return host;
    }

    public int port() {
        return port;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public boolean isPost() {
        return "post".equalsIgnoreCase(method());
    }

    public boolean isGet() {
        return "get".equalsIgnoreCase(method());
    }

    public boolean isConnect() {
        return "connect".equalsIgnoreCase(method());
    }

    public boolean isSSL() {
        return isSSLSocket || isConnect();
    }

    public String method() {
        if (firstLine() == null) return null;
        return firstLine().substring(0, firstLine().indexOf(" "));
    }

    void setUri() {
        String withHost = firstLine().substring(firstLine().indexOf(" "),
                firstLine().lastIndexOf(" ")).trim();
        uri = stripHostName(withHost, host, isSSL());
    }

    String stripHostName(String withHost, String hostName, boolean ssl) {
        String stripped = withHost;
        if (withHost.startsWith("http://") || withHost.startsWith("https://")) {
            int indexOfSlash = withHost.indexOf("/", withHost.indexOf(hostName));
            stripped = withHost.substring(indexOfSlash);
        }
        return stripped;
    }

    public String uri() {
        return uri;
    }

    public String protocol() {
        return firstLine().substring(firstLine().lastIndexOf(" "));
    }

    private void setHostAndPort() {
        hostWithPort = getLastSetValueOfHeader("Host");
        host = hostWithPort;
        port = 80;
        if (isSSL())
            port = 443;
        int indexOfColon = hostWithPort.indexOf(":");
        if (indexOfColon != -1) {
            host = hostWithPort.substring(0, indexOfColon);
            try {
                port = Integer.parseInt(hostWithPort.substring(indexOfColon + 1).trim());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        host = Utils.stripChildSessionId(host);
    }

    private void setQueryString() {
        if (uri == null)
            return;

        int qIx = uri.indexOf("?");
        String uriWithoutQueryString = uri;
        if (qIx != -1 && qIx + 1 < uri.length()) {
            uriWithoutQueryString = uri.substring(0, qIx);
            queryString = uri.substring(qIx + 1);
        }

        fileExtension = "";
        int dotIx = uriWithoutQueryString.indexOf(".");
        if (dotIx != -1) {
            fileExtension = uriWithoutQueryString.substring(dotIx + 1);
        }
    }

    private void setGetParameters() {
        String str = isGet() ? queryString() : new String(data());
        StringTokenizer tokenizer = new StringTokenizer(str, "&");
        while (tokenizer.hasMoreTokens()) {
            String keyVal = tokenizer.nextToken();
            int eqIx = keyVal.indexOf('=');
            if (eqIx != -1) {
                String key = keyVal.substring(0, eqIx);
                String value = "";
                if (eqIx + 1 <= keyVal.length())
                    value = keyVal.substring(eqIx + 1);
                try {
                    params.put(key, URLDecoder.decode(value, "UTF-8"));
                } catch (Exception e) {
                    params.put(key, value);
                }
            }
        }
    }

    public String queryString() {
        return queryString;
    }

    public String getParameter(String key) {
        if (params.size() == 0) {
            setGetParameters();
        }
        return (String) params.get(key);
    }

    private void setCookies() {
        cookies = new LinkedHashMap();
        String cookieString = getLastSetValueOfHeader("Cookie");
        if (cookieString == null)
            return;
        StringTokenizer tokenizer = new StringTokenizer(cookieString, ";");
        while (tokenizer.hasMoreTokens()) {
            String keyVal = tokenizer.nextToken();
            int eqIx = keyVal.indexOf('=');
            if (eqIx != -1) {
                String key = keyVal.substring(0, eqIx).trim();
                String value = "";
                if (eqIx + 1 <= keyVal.length())
                    value = keyVal.substring(eqIx + 1).trim();
                cookies.put(key, value);
            }
        }
    }

    public String getCookie(String key) {
        if (cookies == null) {
            setCookies();
        }
        return (String) cookies.get(key);
    }

    String rebuildCookies() {
        return rebuildCookies(cookies);
    }

    static String rebuildCookies(Map cookies2) {
        StringBuffer sb = new StringBuffer();
        if (cookies2.size() == 0)
            return "";
        Iterator keys = cookies2.keySet().iterator();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            String value = (String) cookies2.get(key);
            sb.append(" ").append(key).append("=").append(value).append(";");
        }
        String cookieStr = sb.toString().trim();
        if (cookieStr.endsWith(";")) {
            cookieStr = cookieStr.substring(0, cookieStr.length() - 1);
        }
        return cookieStr;
    }

    public Map cookies() {
        if (cookies == null) {
            setCookies();
        }
        return cookies;
    }

    public HttpRequest modifyForFetch() {
        if (Configuration.isExternalProxyEnabled()) {
            setFirstLine(firstLine().replaceAll("HTTP/1.1", "HTTP/1.0"));
        } else {
            setFirstLine(method() + " " + uri() + " HTTP/1.0");
        }
        removeHeader("Proxy-Connection");
        removeHeader("Accept-Encoding");
        removeHeader("Keep-Alive");
        removeHeader("Sahi-IsXHR");
        // removeHeader("ETag");
        // removeHeader("If-Modified-Since");
        // removeHeader("If-None-Match");
        setHeader("Connection", "close");
        cookies().remove("sahisid");
        setHeader("Cookie", rebuildCookies());
        setRawHeaders(getRebuiltHeaderBytes());
        logger.fine(firstLine());
        logger.fine("\n------------\n\nRequest Headers:\n" + headers());
        return this;
    }

    public Session session() {
        String sessionId;
        sessionId = getParameter("sahisid");
//        System.out.println("1:" + sessionId);
        if (Utils.isBlankOrNull(sessionId))
            sessionId = getCookie("sahisid");
        if (Utils.isBlankOrNull(sessionId))
            sessionId = "sahi_" + System.currentTimeMillis();
//        System.out.println("2:" + sessionId);
        return Session.getInstance(sessionId);
    }

    public String fileExtension() {
        return fileExtension;
    }

    public String url() {
        return (isSSL() ? "https" : "http") + "://" + hostWithPort + (uri == null ? "" : uri);
    }

    public boolean isMultipart() {
        String contentType = getLastSetValueOfHeader("Content-Type");
        return contentType != null && contentType.startsWith("multipart/form-data");
    }

    public void setSSL(boolean isSSL) {
        this.isSSLSocket = isSSL;
    }

    public boolean isIE() {
        String agent = getLastSetValueOfHeader("User-Agent");
        return (agent == null || agent.indexOf("MSIE") != -1);
    }

    public boolean isExcluded() {
        if (isAjax) return true;
        String url = url();
        String[] exclusionList = Configuration.getExclusionList();
        for (int i = 0; i < exclusionList.length; i++) {
            String pattern = exclusionList[i];
            if (url.matches(pattern.trim())) {
                return true;
            }
        }
        return false;
    }

}
