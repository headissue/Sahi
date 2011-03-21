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

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import net.sf.sahi.config.Configuration;
import net.sf.sahi.stream.filter.CharacterFilter;
import net.sf.sahi.stream.filter.HTMLModifierFilter;
import net.sf.sahi.stream.filter.JSModifierFilter;
import net.sf.sahi.util.Utils;

/**
 * User: nraman Date: May 14, 2005 Time: 1:43:05 AM
 */
public class HttpModifiedResponse extends StreamingHttpResponse {

    boolean isSSL = false;
    private String fileExtension;
	private String charset;
	private byte[] sampleBytes;

    public HttpModifiedResponse(final StreamingHttpResponse response, final boolean isSSL, String fileExtension, int responseCode) {
//    	long start = System.currentTimeMillis();
        this.fileExtension = fileExtension;
        copyFrom(response);
        /*
        removing cache headers again for login problem.
        test on IE sahi.co.in login/logout
        */
        this.isSSL = isSSL;
        if (responseCode < 300 || responseCode >= 400) { // Response code other than 3xx
            boolean html = isHTML();
            charset(); // set it
            if (html) {
            	if (Configuration.addCharacterFilter()) addFilter(new CharacterFilter(charset));
            	if (Configuration.addJSModifierFilter()) addFilter(new JSModifierFilter(charset));
            	if (Configuration.addHTMLModifierFilter()) addFilter(new HTMLModifierFilter(charset, isXHTML(), isSSL));
            	addFilter(new NoCacheFilter());
            } else if (isJs()) {
            	if (Configuration.addCharacterFilter()) addFilter(new CharacterFilter(charset));
            	if (Configuration.addJSModifierFilter()) addFilter(new JSModifierFilter(charset));
//            	Removing NoCacheFilter because it slows down pages a lot. 
//            	Test on http://sourceforge.net main page. min.js should be fetched from cache.            	
//            	addFilter(new NoCacheFilter());  
            }
        }
    }

    public HttpModifiedResponse(final HttpResponse response, final boolean isSSL, String fileExtension, int responseCode) {
    	this (new StreamingHttpResponse(response), isSSL, fileExtension, responseCode);
    }

    public HttpModifiedResponse(HttpResponse response, boolean isSSL, String fileExtension) {
    	this (response, isSSL, fileExtension, 200);
	}

	private boolean isJs() {
        String contentType = contentTypeHeader();
        if (contentType != null && contentType.toLowerCase().indexOf("javascript") != -1) return true;
        if ("js".equalsIgnoreCase(fileExtension) || "js.dsp".equalsIgnoreCase(fileExtension)) return true;
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

    byte[] sampleBytes(){
        if (sampleBytes == null) {
            int length = Configuration.sampleLength();
	        try {
//	        	System.out.println("in.markSupported()="+in.markSupported());
	        	in.mark(2000);
				sampleBytes = Utils.getBytes(in, length);
				in.reset();
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("If you get 'java.io.IOException: Resetting to invalid mark' errors,");
				System.out.println("change response.sample_length in sahi.properties to a nearby value (450), restart Sahi and check.");
				try{
					in.reset();
				}catch (Exception e2) {
					e2.printStackTrace();
				}
			}
        }
        if (sampleBytes == null) sampleBytes = new byte[0];
        return sampleBytes;
	}

    String getSampleContent(String charset) {
        String sampleContent = null;
        try {
			sampleContent = new String(sampleBytes(), charset).toLowerCase();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
        return sampleContent;
    }
}
