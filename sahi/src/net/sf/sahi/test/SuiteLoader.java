/**
 * User: dlewis
 * Date: Dec 8, 2006
 * Time: 3:04:34 PM
 */
package net.sf.sahi.test;

import net.sf.sahi.util.Utils;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class SuiteLoader {

    private String suitePath;
    private String base;
    private List listTest = new ArrayList();

    public SuiteLoader(final String suitePath, final String base) {
        this.suitePath = suitePath;
        this.base = base;
        loadScripts();
    }

    private void loadScripts() {
        File suite = new File(suitePath);
        if (suite.isDirectory()) {
            processSuiteDir(suite);
        } else {
            processSuiteFile();
        }
    }

    private void processSuiteDir(final File suite) {
        File[] fileNames = suite.listFiles();
        for (int i = 0; i < fileNames.length; i++) {
            File file = fileNames[i];
            if (file.isDirectory()) {
                processSuiteDir(file);
            } else {
                String testName = file.getAbsolutePath();
                if (testName.endsWith(".sah") || testName.endsWith(".sahi")) {
                    addTest(testName, base);
                }
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

    private void processLine(final String line) throws MalformedURLException {
        if (line.startsWith("#") || line.startsWith("//") || line.trim().equals("")) {
            return;
        }
        int ix = line.indexOf(' ');
        if (ix == -1) {
            ix = line.indexOf('\t');
        }
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

    private void addTest(final String testName, final String startURL) {
        TestLauncher sahiTest = new TestLauncher(testName, startURL);
        listTest.add(sahiTest);
    }

    public List getListTest() {
        return listTest;
    }
}
