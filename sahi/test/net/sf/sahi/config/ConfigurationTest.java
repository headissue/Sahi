package net.sf.sahi.config;

import junit.framework.TestCase;

public class ConfigurationTest extends TestCase {
    public void testSplit(){
        assertEquals("a", "a\nb\nc".split("\n")[0]);
        assertEquals("b", "a\nb\nc".split("\n")[1]);
        assertEquals("c", "a\nb\nc".split("\n")[2]);
    }

    public void testGetRenderableContentTypes(){
    	assertEquals("a\nb", "a\r\nb".replaceAll("\\\r", ""));
    }

    public void testGetNonBlankLines(){
        assertEquals("a", Configuration.getNonBlankLines(" \r\n a \r\n")[0]);
    }
}
