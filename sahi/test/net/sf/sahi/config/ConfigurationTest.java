package net.sf.sahi.config;

import junit.framework.TestCase;

public class ConfigurationTest extends TestCase {
    public void testSplit(){
        assertEquals("a", "a\nb\nc".split("\n")[0]);
        assertEquals("b", "a\nb\nc".split("\n")[1]);
        assertEquals("c", "a\nb\nc".split("\n")[2]);
    }
}
