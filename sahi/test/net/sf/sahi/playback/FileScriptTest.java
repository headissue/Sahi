package net.sf.sahi.playback;

import junit.framework.TestCase;

import java.io.File;

public class FileScriptTest extends TestCase {
    private static final long serialVersionUID = -1687495110846340213L;
    public void testFQN() {
        FileScript fileScript = new MockFileScript("c:/abc/def/a.sah");
        assertEquals(new File("c:/abc/def/b.sah"), new File(fileScript.getFQN("b.sah")));
    }

    private class MockFileScript extends FileScript{
        public MockFileScript(String fileName) {
            super(fileName);
        }

        protected void loadScript(String fileName) {}
    }

    public void testDirCreation(){
        File file = new File(new File("D:\\my\\sahi\\logs\\"), "D:\\my");
//        file = new File("D:\\my");
        boolean b = file.isAbsolute();
        assertTrue(b);
    }
}
