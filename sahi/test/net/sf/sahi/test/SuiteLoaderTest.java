package net.sf.sahi.test;

import junit.framework.TestCase;
import net.sf.sahi.config.Configuration;
import net.sf.sahi.util.Utils;

import java.io.File;
import java.io.IOException;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.util.List;

/**
 * User: dlewis
 * Date: Dec 8, 2006
 * Time: 3:35:44 PM
 */
public class SuiteLoaderTest extends TestCase {
    private File dir = new File(Configuration.getScriptRoots()[0] + System.getProperty("file.separator") + "junit");

    protected void setUp() throws Exception {
        super.setUp();
        Utils.deleteDir(dir);
        dir.mkdirs();
    }

    public void testProcessSuiteDir() throws IOException {
        new File(dir, "script3.sah").createNewFile();
        new File(dir, "script2.sahi").createNewFile();
        new File(dir, "dummy.txt").createNewFile();
        File subDir = new File(dir, "subDir");
        subDir.mkdirs();
        new File(subDir, "script1.sah").createNewFile();

        List listTest = new SuiteLoader(dir.getAbsolutePath(),"testBase").getListTest();
        assertEquals(3,listTest.size());
        TestLauncher test = (TestLauncher)listTest.get(0);
        assertEquals("testBase",test.getStartURL());
    }

    public void testProcessSuiteFile() throws IOException {
        File suiteFile = createSuiteFile();
        List listTest = new SuiteLoader(suiteFile.getAbsolutePath(),"http://testBase").getListTest();
        assertEquals(3,listTest.size());
        TestLauncher test = (TestLauncher)listTest.get(0);
        assertEquals("http://testBase",test.getStartURL());
        test = (TestLauncher)listTest.get(1);
        assertEquals("http://testBase/index.htm",test.getStartURL());
        test = (TestLauncher)listTest.get(2);
        assertEquals("https://test2",test.getStartURL());
    }

    private File createSuiteFile() throws IOException {
        File suiteFile =new File(dir, "test.suite");
        BufferedWriter buf = new BufferedWriter(new FileWriter(suiteFile));
        buf.write("script3.sah");
        buf.newLine();
        buf.write("//commented1.sah");
        buf.newLine();
        buf.write("script1.sah index.htm");
        buf.newLine();
        buf.write("#commented2.sah");
        buf.newLine();
        buf.write("script2.sah https://test2");
        buf.flush();
        buf.close();

        return suiteFile;
    }
}
