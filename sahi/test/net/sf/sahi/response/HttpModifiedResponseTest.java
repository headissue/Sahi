package net.sf.sahi.response;

import net.sf.sahi.config.Configuration;
import junit.framework.TestCase;


public class HttpModifiedResponseTest extends TestCase {
    public void testActiveXIsSubstituted(){
        assertTrue(Configuration.modifyActiveX());
        assertEquals("new_ActiveXObject", new String(HttpModifiedResponse.substituteIEActiveX("new ActiveXObject".getBytes())));
        assertEquals("new_ActiveXObject", new String(HttpModifiedResponse.substituteIEActiveX("new          ActiveXObject".getBytes())));
        assertEquals("new_ActiveXObject", new String(HttpModifiedResponse.substituteIEActiveX("new\tActiveXObject".getBytes())));
        assertEquals("new_ActiveXObject", new String(HttpModifiedResponse.substituteIEActiveX("new\t    \t\t\tActiveXObject".getBytes())));
        String input = "obj = new ActiveXObject('Microsoft.XMLHTTP');";
        String expected = "obj = new_ActiveXObject('Microsoft.XMLHTTP');";
        String actual = new String(HttpModifiedResponse.substituteIEActiveX(input.getBytes()));
        assertEquals(expected, actual);

    }
}
