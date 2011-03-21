package net.sf.sahi.stream.filter;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import net.sf.sahi.config.Configuration;
import net.sf.sahi.response.HttpResponse;
import net.sf.sahi.util.Utils;

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


public class HTMLModifierFilter extends StreamFilter {

	private String charset;
	private boolean isXHTML;
	private final boolean isSSL;
	private boolean injected;
    private static String INJECT_TOP_SSL = null;
    private static String INJECT_BOTTOM_SSL = null;
    private static String INJECT_TOP;
    private static String INJECT_BOTTOM;
    private String leftOver = "";

    static {
        initializeInjects();
    }

	private static void initializeInjects() {
		INJECT_TOP = new String(Utils.readFile(Configuration.getInjectTop())).replace("$commonDomain", Configuration.getCommonDomain());
        INJECT_BOTTOM = new String(Utils.readFile(Configuration.getInjectBottom()));
        INJECT_TOP_SSL = makeHTTPS(INJECT_TOP);
        INJECT_BOTTOM_SSL = makeHTTPS(INJECT_BOTTOM);
	}

    private static String makeHTTPS(String content) {
        return content.replaceAll("http", "https");
    }

	public HTMLModifierFilter(String charset, boolean isXHTML, boolean isSSL){
		this.charset = charset;
		this.isXHTML = isXHTML;
		this.isSSL = isSSL;
	}

	public byte[] modify(byte[] data) throws IOException {
		return injectSahiFiles(data, getInjectAtTop());
	}

	public String modify(String dataStr) {
		return injectSahiFiles(dataStr, getInjectAtTop());
	}

	private byte[] getBytesInCharset(String str) {
		try {
			return str.getBytes(charset);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return str.getBytes();
		}
	}

	protected String getInjectAtTop() {
//		initializeInjects();
		return isSSL ? INJECT_TOP_SSL : INJECT_TOP;
	}

	protected String getInjectAtBottom() {
//		initializeInjects();
		return isSSL ? INJECT_BOTTOM_SSL : INJECT_BOTTOM;
	}

	public String injectSahiFiles(String dataStr, String toInject) {
		String sample = dataStr;
		int ix = getTagEndIndex(sample.toLowerCase());
	    return inject(sample, ix+1, toInject);
	}
	
	private byte[] injectSahiFiles(byte[] data, String toInject) throws UnsupportedEncodingException {
		if (injected) return data;
		String sample = leftOver + new String(data, charset);
		int ix = -1;
        if (isXHTML) {
            ix = getTagEndIndex(sample.toLowerCase());
        }
        if (ix == -1){
        	leftOver = sample;
        	return new byte[0];
        } else {
	        injected = true;
	        leftOver = "";
	        return getBytesInCharset(inject(sample, ix+1, toInject)); // Have to check for ix==-1 to make sure we wait for the html/head tags
        }
	}
	
	private String inject(String origStr, int insertAfterIx, String toInject) {
		StringBuilder sb = new StringBuilder();
        sb.append(origStr.substring(0, insertAfterIx));
		sb.append(toInject);
		sb.append(origStr.substring(insertAfterIx));
        return sb.toString();
	}

	public void modifyHeaders(HttpResponse response) {
		response.removeHeader("ETag");
		response.removeHeader("Last-Modified");
		response.removeHeader("WWW-Authenticate");
		response.removeHeader("Proxy-Authenticate");
		response.setHeader("Cache-Control", "no-cache");
		response.setHeader("Pragma", "no-cache");
	}

    private int getTagEndIndex(String s) {
        int htmlIx = getTagEndIndex(s, "<html");
        int headIx = getTagEndIndex(s, "<head");
        int ix = (isXHTML &&  headIx != -1) ? headIx : htmlIx;
        return ix;
    }

	private int getTagEndIndex(String s, String tag) {
		int ix = s.indexOf(tag);
        if (ix != -1) ix = s.indexOf(">", ix);
		return ix;
	}

    public byte[] getRemaining(){
    	if (injected)
    		return getBytesInCharset(isXHTML ? "" : getInjectAtBottom());
    	else{
    		StringBuilder sb = new StringBuilder();
            sb.append(getInjectAtTop());
			sb.append(leftOver);
	        if (!isXHTML) sb.append(getInjectAtBottom());
            return getBytesInCharset(sb.toString());

    	}
    }
}
