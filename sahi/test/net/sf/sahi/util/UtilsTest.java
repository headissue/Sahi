package net.sf.sahi.util;

import junit.framework.TestCase;

public class UtilsTest extends TestCase {
    public UtilsTest(String name) {
        super(name);
    }

    public void testBlankLinesDoNotCauseProblemsInNumbering() {
        String s = "1\na\n\nbx\ny";
        assertEquals(5, Utils.getTokens(s).size());
        s = "1\na\n\nbx\ny\n";
        assertEquals(5, Utils.getTokens(s).size());
        s = "\n\n\n";
        assertEquals(3, Utils.getTokens(s).size());
        s = "";
        assertEquals(1, Utils.getTokens(s).size());
        s = "\n";
        assertEquals(1, Utils.getTokens(s).size());
    }

    public void testConvertStringToASCII() {
        assertEquals("Elephant", Utils.convertStringToASCII("Éléphant"));
    }

    public void testMakeString() {
        assertEquals("a\\nb", Utils.makeString("a\nb"));
    }

    public void testStripChildSessionId() {
        assertEquals("2371283", Utils.stripChildSessionId("2371283sahix34x"));
        assertEquals("2371283sahixx", Utils.stripChildSessionId("2371283sahixx"));
        assertEquals("2371283sahix", Utils.stripChildSessionId("2371283sahix"));
    }

    public void xtestConcatPaths() {
        assertEqualPaths("D:/my/sahi/certs/a.txt", Utils.concatPaths("../certs", "a.txt"));
    }

    private void assertEqualPaths(String expected, String actual){
        assertEquals(Utils.makePathOSIndependent(expected), Utils.makePathOSIndependent(actual));
    }

    public void testMakeOSInedpendent() {
        if (Utils.isWindows())
            assertEquals("D:\\my\\sahi\\certs\\a.txt", Utils.makePathOSIndependent("D:/my/sahi/certs/a.txt"));
    }
}
