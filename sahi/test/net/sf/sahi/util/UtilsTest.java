package net.sf.sahi.util;

import junit.framework.TestCase;

public class UtilsTest extends TestCase {
	public UtilsTest(String name) {
		super(name);
	}

	public void xtestConcatPaths (){
		assertEquals("/a/b", Utils.concatPaths("/a", "b"));
		assertEquals("/a/b", Utils.concatPaths("/a/", "b"));
		assertEquals("/a/b", Utils.concatPaths("/a/", "/b"));
		assertEquals("/a/b", Utils.concatPaths("/a", "/b"));
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

    public void testConvertStringToASCII(){
        assertEquals("Elephant", Utils.convertStringToASCII("Éléphant"));
    }

    public void testMakeString(){
        assertEquals("a\\nb", Utils.makeString("a\nb"));
    }
}
