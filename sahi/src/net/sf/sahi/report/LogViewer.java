package net.sf.sahi.report;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;

/**
 * User: dlewis
 * Date: Dec 8, 2006
 * Time: 12:17:44 PM
 */
public class LogViewer {
    public static String getLogsList(String logsRootDir) {
        File[] fileList = new File(logsRootDir)
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
        sb.append("<html><body><style>b{background:brown;color:white;}\nspan{background:lightgrey;}</style><pre>").append(highlightLine(data, lineNumber)).append("</pre></body></html>");
        return sb.toString();
    }

    static String highlightLine(String data, int lineNumber) {
        String[] lines = data.split("\n");
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            if (i+1 == lineNumber) {
                line = "<b>" + line + "</b>";
            }
            line = "<span>" + (i+1) + "</span> " + line + "\n";
            sb.append(line);
        }
        return sb.toString();
    }
}
