package net.sf.sahi.response;

import java.util.Properties;

import junit.framework.TestCase;

/**
 * User: nraman
 * Date: May 15, 2005
 * Time: 10:14:34 PM
 */
public class HttpFileResponseTest extends TestCase {
    public void testSubstitute(){
        Properties props = new Properties();
        props.setProperty("isRecording", "true");
        props.setProperty("isPlaying", "false");
        props.setProperty("sessionId", "sahi_1281210");
        String template = " var isRecording=$isRecording;\n var isPlaying=$isPlaying;\n setCookie('$sessionId')";
        assertEquals(" var isRecording=true;\n var isPlaying=false;\n setCookie('sahi_1281210')",
                HttpFileResponse.substitute(template, props));
    }
}

