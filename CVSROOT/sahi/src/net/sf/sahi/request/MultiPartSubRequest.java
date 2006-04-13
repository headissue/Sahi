package com.sahi.request;

import com.sahi.StreamHandler;

import java.io.IOException;
import java.io.InputStream;

/**
 * User: nraman
 * Date: May 18, 2005
 * Time: 8:42:08 PM
 */
public class MultiPartSubRequest extends StreamHandler {

    public MultiPartSubRequest(InputStream in) throws IOException {
        populateHeaders(in, false);
        populateData(in);
    }
}
