/**
 * User: dlewis
 * Date: Dec 8, 2006
 * Time: 3:04:34 PM
 */
package net.sf.sahi.test;

import net.sf.sahi.config.Configuration;
import net.sf.sahi.util.Utils;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

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


public class SuiteLoader {

    private String suitePath;
    private String base;
    private List<TestLauncher> listTest = new ArrayList<TestLauncher>();

    public SuiteLoader(final String suitePath, final String base) {
        this.suitePath = suitePath;
        this.base = base;
        loadScripts();
    }

    private void loadScripts() {
        File suite = new File(Configuration.getAbsoluteUserPath(suitePath));
        if (suite.isDirectory()) {
            processSuiteDir(suite);
        } else {
        	if (Utils.isSahiTestFile(suitePath)){
        		addTest(suitePath, base);
        	}else{
        		processSuiteFile();
        	}
        }
    }

    private void processSuiteDir(final File suite) {
        File[] fileNames = suite.listFiles();
        for (int i = 0; i < fileNames.length; i++) {
            File file = fileNames[i];
            if (file.isDirectory()) {
                processSuiteDir(file);
            } else {
                String testName = Utils.getAbsolutePath(file);
                if (Utils.isSahiTestFile(testName)) {
                    addTest(testName, base);
                }
            }
        }
    }


    private void processSuiteFile() {
        String contents = new String(Utils.readFile(Configuration.getAbsoluteUserPath(suitePath)));
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
        addTest(Utils.concatPaths(suitePath, testName, true), startURL);
    }

    public void addTest(final String testName, final String startURL) {
        TestLauncher sahiTest = new TestLauncher(testName, startURL);
        listTest.add(sahiTest);
    }

    public List<TestLauncher> getListTest() {
        return listTest;
    }
}
