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

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import net.sf.sahi.config.Configuration;
import net.sf.sahi.util.Utils;

/**
 * User: nraman Date: May 14, 2005 Time: 1:43:05 AM
 */
public class HttpModifiedResponse extends HttpResponse {

    boolean isSSL = false;
    private String fileExtension;
    private static byte[] INJECT_TOP_SSL = null;
    private static byte[] INJECT_BOTTOM_SSL = null;
    private static byte[] INJECT_TOP;
    private static byte[] INJECT_BOTTOM;
    

    static {
        INJECT_TOP = Utils.readFile("../config/inject_top.txt");
        INJECT_BOTTOM = Utils.readFile("../config/inject_bottom.txt");
        INJECT_TOP_SSL = makeHTTPS(INJECT_TOP);
        INJECT_BOTTOM_SSL = makeHTTPS(INJECT_BOTTOM);
    }

    private static byte[] makeHTTPS(byte[] content) {
        return new String(content).replaceAll("http", "https").getBytes();
    }

    public HttpModifiedResponse(final HttpResponse response, final boolean isSSL, String fileExtension)
            throws IOException {
        this.fileExtension = fileExtension;
        copyFrom(response);
        this.isSSL = isSSL;
        if (firstLine().indexOf("30") == -1) { // Response code other than
            boolean html = isHTML();
            if (html) {
                removeHeader("Transfer-Encoding");
                removeHeader("ETag");
                removeHeader("Last-Modified");
                setData(getModifiedData());
            } else if (isJs()) {
                setData(substituteIEActiveX(data()));
            }
            resetRawHeaders();
        }
    }

    private boolean isJs() {
        return "js".equalsIgnoreCase(fileExtension);
    }

    private byte[] getModifiedData() throws IOException {
        int ix = 0;
        if (isXHTML()) {
            ix = getHTMLTagIndex();
        }
        return inject(substituteIEActiveX(data()), ix, isSSL);
    }

    private static byte[] inject(byte[] orig, int ix, boolean isSSL) throws IOException {
        ByteArrayOutputStream s = new ByteArrayOutputStream();
        s.write(orig, 0, ix);
        s.write(isSSL ? INJECT_TOP_SSL : INJECT_TOP);
        s.write(orig, ix, orig.length - ix);
        s.write(isSSL ? INJECT_BOTTOM_SSL : INJECT_BOTTOM);
        s.flush();
        return s.toByteArray();
    }

    private int getHTMLTagIndex() {
        String s = getSampleContent();
        final int ix = s.indexOf("<html");
        return ix == -1 ? 0 : ix;
    }

    private boolean isXHTML() {
        String s = getSampleContent();
        return s.indexOf("<?xml") != -1 || s.indexOf("<!doctype") != -1;

    }

    static byte[] substituteModals(final byte[] content) {
        // long start = System.currentTimeMillis();
        String s = new String(content);
        s = s.replaceAll("(\\W+)((alert)|(confirm)|(prompt))\\s*\\(", "$1sahi_$2(");
        // System.out.println("substituteModals took ms
        // "+(System.currentTimeMillis() - start));
        return s.getBytes();
    }

    private boolean isHTML() {
        if (isJs()) {
            return false;
        }
        String contentType = contentType();
        if (contentType != null && contentType.toLowerCase().indexOf("text/html") != -1) {
            return true;
        }
        if (contentType == null || contentType.toLowerCase().indexOf("text/plain") != -1) {
            return hasHtmlContent();
        }
        return false;
    }

    private boolean hasHtmlContent() {
        String s = getSampleContent();
        return s.indexOf("<html") != -1 || s.indexOf("<body") != -1 || s.indexOf("<table") != -1 || s.indexOf("<script") != -1 || s.indexOf("<form") != -1;
    }
    private String sampleContent = null;

    private String getSampleContent() {
        if (sampleContent == null) {
            int length = 500;
            if (data().length < length) {
                length = data().length;
            }
            sampleContent = new String(data(), 0, length).toLowerCase();
        }
        return sampleContent;
    }

    public static HttpResponse modify(final HttpResponse response) throws IOException {
        response.setData(inject(substituteIEActiveX(response.data()), 0, false));
        return response;
    }

    static byte[] substituteIEActiveX(final byte[] bs) {
        if (!Configuration.modifyActiveX()) {
            return bs;
        }
        return new String(bs).replaceAll("new[\\s]*ActiveXObject", "new_ActiveXObject").getBytes();
    }
}
