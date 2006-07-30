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
        fileName = getValue(tokenizer.nextToken());

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
        setHeader("Content-Disposition", "form-data; fileName=\""+name +"\"; filename=\""+this.fileName +"\"");
        resetRawHeaders();
    }
}
