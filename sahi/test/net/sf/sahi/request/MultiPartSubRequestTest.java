package net.sf.sahi.request;

import junit.framework.TestCase;

/**
 * User: nraman
 * Date: May 18, 2005
 * Time: 8:42:08 PM
 */
public class MultiPartSubRequestTest extends TestCase {
    public void testParse() {
        String s = "form-data; name=\"f1\"; filename=\"test.txt\"";
        MultiPartSubRequest multiPartSubRequest = new MultiPartSubRequest();
        multiPartSubRequest.setNameAndFileName(s);
        assertEquals("f1", multiPartSubRequest.name());
        assertEquals("test.txt", multiPartSubRequest.fileName());

    }

    public void testGetValue(){
        assertEquals("f1", MultiPartSubRequest.getValue("name=\"f1\""));
        assertEquals("test.txt", MultiPartSubRequest.getValue("filename=\"test.txt\""));
    }


}
