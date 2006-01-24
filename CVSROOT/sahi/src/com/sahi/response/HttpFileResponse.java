package com.sahi.response;

import java.util.Date;
import java.util.Enumeration;
import java.util.Properties;

import com.sahi.util.Utils;

/**
 * User: nraman
 * Date: May 15, 2005
 * Time: 10:14:34 PM
 */
public class HttpFileResponse extends HttpResponse {
    private String fileName;
//    private Properties substitutions;

    public HttpFileResponse(String fileName, Properties substitutions) {
        this.fileName = fileName;
    	data(Utils.readFile(fileName));
        if (substitutions != null) {
            data(substitute(new String(data()), substitutions).getBytes());
        }
        setHeaders();
    }

    static String substitute(String content, Properties substitutions) {
        Enumeration keys = substitutions.keys();
        while (keys.hasMoreElements()) {
            String key = (String) keys.nextElement();
            content = content.replaceAll("\\$" + key, substitutions.getProperty(key));
        }
        return content;
    }

    public HttpFileResponse(String fileName) {
        this(fileName, null);
    }

    private void setHeaders() {
        setFirstLine("HTTP/1.1 200 OK");
        setHeader("Content-Type", getContentType(fileName));
        setHeader("Expires", new Date(System.currentTimeMillis()+3*60*1000).toString());        
        setHeader("Connection", "close");
        setHeader("Content-Length", "" + data().length);
        setRawHeaders(getRebuiltHeaderBytes());
    }


    private String getContentType(String fileName) {
        if (fileName.endsWith("htm") || fileName.endsWith("html")) {
            return "text/html";
        } else if (fileName.endsWith("class")) {
            return "text/plain";
//            return "application-x/octetst";
        }else if (fileName.endsWith("ico")) {
        	return "image/bmp";
        }
        return "text/plain";
    }
}

