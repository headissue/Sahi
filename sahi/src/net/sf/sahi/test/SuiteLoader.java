package net.sf.sahi.test;

import net.sf.sahi.util.Utils;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

/**
 * User: dlewis
 * Date: Dec 8, 2006
 * Time: 3:04:34 PM
 */
public class SuiteLoader {
    private String suitePath;
    private String base;
    private List listTest = new ArrayList();

    public SuiteLoader(String suitePath, String base) {
        this.suitePath = suitePath;
        this.base = base;
    }

    public List loadScripts() {
        File suite = new File(suitePath);
        if (suite.isDirectory()) {
            processSuiteDir(suite);
        } else {
            processSuiteFile();
        }
        return listTest;
    }

    private void processSuiteDir(File suite) {
        File[] fileNames = suite.listFiles();
        Arrays.sort(fileNames, new Comparator() {
            public int compare(Object o1, Object o2) {
                return ((File) o1).getName().compareToIgnoreCase(
                        ((File) o1).getName());
            }
        });
        for (int i = 0; i < fileNames.length; i++) {
            File file = fileNames[i];
            if (!file.isDirectory()) {
                String testName = file.getAbsolutePath();
                if (testName.endsWith(".sah") || testName.endsWith(".sahi")) {
                    addTest(testName, base);
                }
            }
        }
        for (int i = 0; i < fileNames.length; i++) {
            File file = fileNames[i];
            if (file.isDirectory()) {
                processSuiteDir(file);
            }
        }
    }

    private void processSuiteFile() {
        String contents = new String(Utils.readFile(suitePath));
        StringTokenizer tokens = new StringTokenizer(contents, "\n");
        while (tokens.hasMoreTokens()) {
            String line = tokens.nextToken();
            try {
                processLine(line.trim());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
    }

    private void processLine(String line) throws MalformedURLException {
        if (line.startsWith("#") || line.startsWith("//")
                || line.trim().equals(""))
            return;
        int ix = line.indexOf(' ');
        if (ix == -1)
            ix = line.indexOf('\t');
        String testName;
        String startURL;
        if (ix != -1) {
            testName = line.substring(0, ix).trim();
            startURL = line.substring(ix).trim();
        } else {
            testName = line;
            startURL = "";
        }
        if (!(startURL.startsWith("http://") || startURL.startsWith("https://"))) {
            startURL = new URL(new URL(base), startURL).toString();
        }
        addTest(Utils.concatPaths(suitePath, testName), startURL);
    }

    private void addTest(String testName, String startURL) {
        TestLauncher sahiTest = new TestLauncher(testName, startURL);
        listTest.add(sahiTest);
    }
}
