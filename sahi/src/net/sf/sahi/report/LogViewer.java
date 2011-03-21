/**
 * User: dlewis
 * Date: Dec 8, 2006
 * Time: 12:17:44 PM
 */
package net.sf.sahi.report;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

//import net.sf.sahi.util.TimeDiff;
import net.sf.sahi.util.Utils;

/**
 * Sahi - Web Automation and Test Tool
 * 
 * Copyright  2006  V Narayan Raman
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


public class LogViewer {
    public static String getLogsList2(final String logsRootDir) {
//    	TimeDiff.start("listFiles");

    	File[] fileList = new File(logsRootDir).listFiles();
        
//    	TimeDiff.diffAndRemove("listFiles");

//    	TimeDiff.start("sortFiles");

    	int length = fileList.length;
		Map<Long, File> map = new HashMap<Long, File>(length);
        Long[] times = new Long[length];
        for (int i = 0; i < length; i++) {
        	File file = fileList[i];
        	Long modifiedTime = new Long(file.lastModified());
			map.put(modifiedTime, file);
			times[i] = modifiedTime;
		}
        
//        TimeDiff.start("sorting");
        Arrays.sort(times);
//        TimeDiff.diffAndRemove("sorting");
        
//    	TimeDiff.diffAndRemove("sortFiles");
        
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < length; i++) {
            File file = (File) map.get(times[i]);
			String fileName = file.getName();
            if (fileName.endsWith(".htm") || file.isDirectory()) {
                sb.append("<a href='/_s_/dyn/Log_viewLogs/");
                sb.append(getSummaryFile(file));
                sb.append("'>");
                sb.append(fileName);
                sb.append("</a><br>");
            }
        }
        return sb.toString();    	
    }
    
    public static String getLogsList(final String logsRootDir) {
//    	TimeDiff.start("listFiles");
        File[] fileList = new File(logsRootDir).listFiles();
//        TimeDiff.diffAndRemove("listFiles");
        Comparator<File> comparator = new LastModifiedComparator();
//        TimeDiff.start("sort");
        Arrays.sort(fileList, comparator);
//        TimeDiff.diffAndRemove("sort");
//        TimeDiff.start("page creation");
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
//        TimeDiff.diffAndRemove("page creation");
        return sb.toString();
    }

    private static String getSummaryFile(final File file) {
        if (file.isDirectory()) {
            return file.getName() + "/index.htm";
        }
        return file.getName();
    }

    public static String highlight(final String data, final int lineNumber) {
        StringBuffer sb = new StringBuffer();
        sb.append("<html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\" /></head><body><style>b{background:brown;color:white;}\nspan{background:lightgrey;}</style><pre>").append(highlightLine(data, lineNumber)).append("</pre></body></html>");
        return sb.toString();
    }

    public static String addLineNumbers(String orig, String scriptPath){
		orig = orig.replaceAll("\\r", "");
		if ("".equals(orig)) return orig;
        String[] lines = orig.split("\n");
        StringBuffer sb2 = new StringBuffer();
        int len = lines.length;
        for (int i = 0; i < len; i++) {
        	String line = lines[i];
        	if ("".equals(line.trim())) continue;
			sb2.append("<a style=\"text-decoration:none;color:inherit;\" href='/_s_/dyn/Log_highlight?href="+ Utils.escapeDoubleQuotesAndBackSlashes(scriptPath)+"&n="+(i+1)+"#selected'>"+line+"</a>\n");
        }
		return sb2.toString();
	}

    static String highlightLine(String data, final int lineNumber) {
    	data = data.replaceAll("\\r", "");
        String[] lines = data.split("\n");
        StringBuffer sb = new StringBuffer();
        int len = lines.length;
        StringBuffer tmpBuf = new StringBuffer();
        for (int i = 0; i < len; i++) {
            tmpBuf.append(lines[i]);    // load the buffer with data
            if ((i + 1) == lineNumber) {
                tmpBuf.insert(0, "<a name='selected'><b>").append("</b></a>");   // add HTML if this is what we want
            }

            // Add stuff to the front of the string
            tmpBuf.insert(0, "</span> ");
            tmpBuf.insert(0, (i + 1));
            tmpBuf.insert(0, "<span>");
            // and glue on the stuff at the back of the string
            tmpBuf.append("\n");

            sb.append(tmpBuf.toString());
            tmpBuf.setLength(0);    // truncate the buffer so we don't have to create it again.
        }
        return sb.toString();
    }
}

class LastModifiedComparator implements Comparator<File> {
	Map<File, Long> times = new HashMap<File, Long>();
    public int compare(File file1, File file2) {
        long diff = getTime(file1) - getTime(file2);
        return diff == 0 ? 0 : diff < 0 ? 1 : -1;
    }
    public long getTime(File file){
    	Long time  = times.get(file);
    	if (time == null){
//    		time = new Long(file.lastModified());
    		Date date = Utils.getDateFromFileName(file.getName());
			time = (date != null) ? new Long(date.getTime()) : new Long(file.lastModified());
//			time = (date != null) ? new Long(date.getTime()) : new Long(0);
    		times.put(file, time);
    	}
    	return time.longValue();
    }
}