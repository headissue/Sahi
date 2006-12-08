package net.sf.sahi.report;

import net.sf.sahi.config.Configuration;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;

/**
 * User: dlewis
 * Date: Dec 8, 2006
 * Time: 12:17:44 PM
 */
public class LogViewer {
    public static String getLogsList() {
        File[] fileList = new File(Configuration.getPlayBackLogsRoot())
                .listFiles();
        Arrays.sort(fileList, new Comparator() {
            public int compare(Object file1, Object file2) {
                //Sorts by last modified
                long diff = (((File) file1).lastModified() - ((File) file2)
                        .lastModified());
                return diff == 0 ? 0 : diff < 0 ? 1 : -1;
            }
        });
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < fileList.length; i++) {
            String fileName = fileList[i].getName();
            if (fileName.endsWith(".htm") || fileList[i].isDirectory()) {
                sb.append("<a href='/_s_/dyn/Log_viewLogs/");
                sb.append(getSummaryFile(fileList[i]));
                sb.append("'>");
                sb.append(fileName);
                sb.append("</a><br>");
            }
        }
        sb.append("\n\n\n");
        return sb.toString();
    }

    private static String getSummaryFile(File file) {
        if (file.isDirectory()) {
            return file.getName() + "/index.htm";
        }
        return file.getName();
    }

    public static String highlight(String data, int lineNumber) {
        StringBuffer sb = new StringBuffer();
        sb.append("<html><body><style>b{color:brown}</style><pre>").append(highlightLine(data, lineNumber)).append("</pre></body></html>");
        return sb.toString();
    }

    private static String highlightLine(String data, int lineNumber) {
        data = data.replaceAll("<", "&lt;");
        data = data.replaceAll(">", "&gt;");
        int startIx = 0;
        int endIx = -1;
        int len = data.length();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < lineNumber; i++) {
            startIx = endIx + 1;
            endIx = data.indexOf("\n", startIx);
            if (endIx == -1) break;
        }
        if (endIx == -1) endIx = len;
        sb.append(data.substring(0, startIx));
        sb.append("<b>");
        sb.append(data.substring(startIx, endIx).replace('\r', ' '));
        sb.append("</b>");
        sb.append(data.substring(endIx, len));
        return sb.toString();
    }
}
