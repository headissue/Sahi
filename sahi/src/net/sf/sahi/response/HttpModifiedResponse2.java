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
package net.sf.sahi.response;

import java.io.UnsupportedEncodingException;

import net.sf.sahi.config.Configuration;
import net.sf.sahi.stream.filter.HTMLModifierFilter;
import net.sf.sahi.stream.filter.JSModifierFilter;

/**
 * User: nraman Date: May 14, 2005 Time: 1:43:05 AM
 */
public class HttpModifiedResponse2 extends HttpResponse {

    boolean isSSL = false;
    private String fileExtension;
	private String charset;
	private String dataString; 

    public HttpModifiedResponse2(final HttpResponse response, final boolean isSSL, String fileExtension, int responseCode) {
        this.fileExtension = fileExtension;
        copyFrom(response);
        /*
        removing cache headers again for login problem.
        test on IE sahi.co.in login/logout
        */
        this.isSSL = isSSL;
        if (responseCode < 300 || responseCode >= 400) { // Response code other than 3xx
            boolean html = isHTML();
            boolean js = isJs();
            if (html || js){
            	charset(); // set it
            	final byte[] data = data();
            	if (data == null) {
            		dataString = "";
            	} else {
					try {
						dataString = new String(data, charset);
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
						dataString = new String(data);
					}
            	}
	            if (html) {
					HTMLModifierFilter htmlModifierFilter = new HTMLModifierFilter(charset, isXHTML(), isSSL);
					htmlModifierFilter.modifyHeaders(response);
					dataString = htmlModifierFilter.modify(dataString);
	            }
				JSModifierFilter jsModifierFilter = new JSModifierFilter(charset);
				dataString = jsModifierFilter.modify(dataString);
				
				try {
					setData(dataString.getBytes(charset));
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				if (html) {
					new NoCacheFilter().modifyHeaders(response);
				}
	        }
        }
    }

    public HttpModifiedResponse2(HttpResponse response, boolean isSSL, String fileExtension) {
    	this (response, isSSL, fileExtension, 200);
	}

	private boolean isJs() {
        String contentType = contentTypeHeader();
        if (contentType != null && contentType.toLowerCase().indexOf("javascript") != -1) return true;
        String fileExtensionLC = fileExtension.toLowerCase();
        if ("js".equals(fileExtensionLC) || 
        		"js.dsp".equals(fileExtensionLC) ||
        		fileExtensionLC.endsWith(".js") || 
        		fileExtensionLC.endsWith("js.dsp")) return true;
        return false;
    }

    private boolean isXHTML() {
        String s = getSampleContent();
        return s.indexOf("<?xml") != -1 || s.indexOf("<!doctype") != -1;
    }

    private boolean isHTML() {
        if (isJs()) {
            return false;
        }
        String contentType = contentTypeHeader();
        if (contentType != null && contentType.toLowerCase().indexOf("text/html") != -1) {
            return true;
        }
        if (contentType == null || contentType.toLowerCase().indexOf("text/plain") != -1 || contentType.toLowerCase().indexOf("text/xml") != -1) {
            return hasHtmlContent();
        }
        return false;
    }

    private boolean hasHtmlContent() {
        String s = getSampleContent();
        return s.indexOf("<html") != -1 || s.indexOf("<body") != -1 || s.indexOf("<table") != -1 || s.indexOf("<script") != -1 || s.indexOf("<form") != -1;
    }

    public String charset() {
    	if (charset == null){
	        charset = "iso-8859-1";
	        String lookIn = contentTypeHeader();
	        if (lookIn == null) lookIn = getSampleContent("iso-8859-1");
	        String charsetEqTo = "charset=";
	        int ix = lookIn.indexOf(charsetEqTo);
	        if (ix != -1) {
	            int endIx = lookIn.indexOf('"', ix);
	            if (endIx == -1)
	                endIx = lookIn.length();
	            charset = lookIn.substring(ix + charsetEqTo.length(), endIx).trim();
	        }
	        if (charset.endsWith(";")) charset = charset.substring(0, charset.length()-1);
	        try {
	            new String(new byte[]{}, charset);
	        } catch (UnsupportedEncodingException e) {
//	            e.printStackTrace();
	            System.out.println("Defaulting to charset iso-8859-1");
	            charset = "iso-8859-1";
	        }
    	}
        return charset;
    }

    String getSampleContent() {
    	return getSampleContent(charset());
    }

    String getSampleContent(String charset) {
        String sampleContent = null;
        try {
        	int limit = Configuration.sampleLength();
        	if (contentLength() < limit) limit = contentLength();
			final byte[] data = data();
			sampleContent = data == null ? "" : new String(data, 0, limit, charset).toLowerCase();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
        return sampleContent;
    }
}
