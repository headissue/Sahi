package net.sf.sahi.response;

import junit.framework.TestCase;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Properties;
import java.util.TimeZone;

import net.sf.sahi.util.Utils;

/**
 * User: nraman Date: May 15, 2005 Time: 10:14:34 PM
 */
public class HttpFileResponseTest extends TestCase {
    public void testSubstitute() {
        Properties props = new Properties();
        props.setProperty("isRecording", "true");
        props.setProperty("isPlaying", "false");
        props.setProperty("sessionId", "sahi_1281210");
        String template = " var isRecording=$isRecording;\n var isPlaying=$isPlaying;\n setCookie('$sessionId')";
        assertEquals(
                " var isRecording=true;\n var isPlaying=false;\n setCookie('sahi_1281210')",
                Utils.substitute(template, props));
    }

    public void testSubstituteWorksWhenTheReplacedTextHasDollarInIt() {
        Properties props = new Properties();
        props.setProperty("sessionId", "$sahi_1281210");
        String template = "setCookie('$sessionId')";
        assertEquals(
                "setCookie('$sahi_1281210')",
                Utils.substitute(template, props));
    }

    public void testFormatForExpiresHeader() {
        Date date = new GregorianCalendar(2001, 4, 5).getTime();

        assertEquals("Sat, 05 May 2001 12:00:00 " + getTimeZone(date), HttpFileResponse.formatForExpiresHeader(date));
    }

    private String getTimeZone(Date date) {
        return TimeZone.getDefault().getDisplayName(TimeZone.getDefault().inDaylightTime(date), TimeZone.SHORT);
    }
}
