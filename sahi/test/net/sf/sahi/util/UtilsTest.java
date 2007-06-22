package net.sf.sahi.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    public void testSplit(){
        assertEquals("aa", "aa\nbb\r\ncc".replaceAll("\r", "").split("[\n]")[0]);
        assertEquals("bb", "aa\nbb\r\ncc".replaceAll("\r", "").split("[\n]")[1]);
        assertEquals("cc", "aa\nbb\r\ncc".replaceAll("\r", "").split("[\n]")[2]);
    }

    public void testInclude(){
        String inputStr = "_click(_link($linkText));\r\n_include(\"includes/sahi_demo_include.sah\");\r\n//_include(\"http://localhost:9999/_s_/scripts/sahi_demo_include.sah\");";
        String patternStr = "[^\"']*[.]sah";
        Pattern pattern = Pattern.compile(patternStr);
        Matcher matcher = pattern.matcher(inputStr);
        while (matcher.find()) {
            String includeStatement = matcher.group(0);
            System.out.println(includeStatement);
        }
    }

}


