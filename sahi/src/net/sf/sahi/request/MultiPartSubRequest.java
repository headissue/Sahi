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
    public MultiPartSubRequest(){
    }

    public MultiPartSubRequest(InputStream in) throws IOException {
        populateHeaders(in, false);
        populateData(in);
        setNameAndFileName(getLastSetValueOfHeader("Content-Disposition"));
    }

    void setNameAndFileName(String s) {
        StringTokenizer tokenizer = new StringTokenizer(s, ";");
        tokenizer.nextToken();
        name = getValue(tokenizer.nextToken());
        if (tokenizer.hasMoreTokens()) fileName = getValue(tokenizer.nextToken());

    }

    static String getValue(String s) {
        return s.substring(s.indexOf("\"") + 1, s.lastIndexOf("\""));
    }

    public String name() {
        return name;
    }

    public String fileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
        setHeader("Content-Disposition", "form-data; name=\""+name +"\"; filename=\""+this.fileName +"\"");
        resetRawHeaders();
    }
}
