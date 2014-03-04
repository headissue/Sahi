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
package net.sf.sahi.request;

import net.sf.sahi.StreamHandler;

import java.io.IOException;
import java.io.InputStream;
import java.util.StringTokenizer;

/**
 * User: nraman
 * Date: May 18, 2005
 * Time: 8:42:08 PM
 */
public class MultiPartSubRequest extends StreamHandler {

    private String name;
    private String fileName;

    // For Test. Yuck!
    public MultiPartSubRequest() {
    }

    public MultiPartSubRequest(final InputStream in) throws IOException {
        populateHeaders(in, false);
        populateData(in);
        setNameAndFileName(getLastSetValueOfHeader("Content-Disposition"));
		removeHeader("Content-Length");
        // System.out.println(new String(rawHeaders()));
	}

    void setNameAndFileName(final String s) {
        StringTokenizer tokenizer = new StringTokenizer(s, ";");
        tokenizer.nextToken();
        name = getValue(tokenizer.nextToken());
        if (tokenizer.hasMoreTokens()) {
            fileName = getValue(tokenizer.nextToken());
        }
    }

    static String getValue(final String s) {
        return s.substring(s.indexOf("\"") + 1, s.lastIndexOf("\""));
    }

    public String name() {
        return name;
    }

    public String fileName() {
        return fileName;
    }

    public void setFileName(final String fileName) {
        this.fileName = fileName;
        setHeader("Content-Disposition", "form-data; name=\"" + name + "\"; filename=\"" + this.fileName + "\"");
        resetRawHeaders();
    }
}
