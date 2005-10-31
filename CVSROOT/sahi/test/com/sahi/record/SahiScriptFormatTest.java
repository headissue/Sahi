package com.sahi.record;

import junit.framework.TestCase;

/**
 * User: nraman
 * Date: Jun 3, 2005
 * Time: 12:33:33 AM
 */
public class SahiScriptFormatTest extends TestCase {
    public void testEscape() {
        assertEquals("aa\\$bb", new SahiScriptFormat().escape("aa$bb"));
    }
	public void testSahiQuoteIfString() {
		assertEquals("123", new SahiScriptFormat().sahiQuoteIfString("123"));
		assertEquals("\"abc\"", new SahiScriptFormat().sahiQuoteIfString("abc"));
	}    
}
