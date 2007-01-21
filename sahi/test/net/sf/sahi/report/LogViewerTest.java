package net.sf.sahi.report;

import junit.framework.TestCase;
import net.sf.sahi.config.Configuration;
import net.sf.sahi.util.Utils;

import java.io.File;
import java.io.IOException;

/**
 * User: dlewis
 * Date: Dec 8, 2006
 * Time: 5:02:01 PM
 */
public class LogViewerTest extends TestCase {
    private File dir = new File(Configuration.getPlayBackLogsRoot() + System.getProperty("file.separator") + "junit");


    protected void setUp() throws Exception {
        super.setUp();
        Utils.deleteDir(dir);
        dir.mkdirs();
    }

    public void testGetLogsList() throws IOException, InterruptedException {
        new File(dir, "log3.htm").createNewFile();
        new File(dir, "log2.htm").createNewFile();
        new File(dir, "dummy.txt").createNewFile();
        Thread.sleep(100);
        File subDir = new File(dir, "subDir");
        subDir.mkdirs();
        new File(subDir, "blah.htm").createNewFile();

        String expected = "<a href='/_s_/dyn/Log_viewLogs/subDir/index.htm'>subDir</a><br><a href='/_s_/dyn/Log_viewLogs/log2.htm'>log2.htm</a><br><a href='/_s_/dyn/Log_viewLogs/log3.htm'>log3.htm</a><br>";
        assertEquals(expected, LogViewer.getLogsList(dir.getAbsolutePath()));
    }

    public void testHighlightLine() {
        assertEquals("<span>1</span> <b>one</b>\n<span>2</span> two\n<span>3</span> three\n<span>4</span> four\n", LogViewer.highlightLine("one\ntwo\nthree\nfour", 1));
        assertEquals("<span>1</span> one\n<span>2</span> <b>two</b>\n<span>3</span> three\n<span>4</span> four\n", LogViewer.highlightLine("one\ntwo\nthree\nfour", 2));
        assertEquals("<span>1</span> one\n<span>2</span> two\n<span>3</span> three\n<span>4</span> <b>four</b>\n", LogViewer.highlightLine("one\ntwo\nthree\nfour", 4));
        assertEquals("<span>1</span> one\n<span>2</span> two\n<span>3</span> three\n<span>4</span> four\n", LogViewer.highlightLine("one\ntwo\nthree\nfour", -1));
        assertEquals("<span>1</span> one\n<span>2</span> two\n<span>3</span> three\n<span>4</span> four\n", LogViewer.highlightLine("one\ntwo\nthree\nfour", 0));
    }

    public void testHighlight() {
        String data = "test";
        assertEquals("<html><body><style>b{background:brown;color:white;}\nspan{background:lightgrey;}</style><pre><span>1</span> test\n</pre></body></html>", LogViewer.highlight(data, -1));
    }
}
