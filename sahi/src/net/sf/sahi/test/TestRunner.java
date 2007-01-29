/**
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

package net.sf.sahi.test;

import net.sf.sahi.ant.CreateIssue;
import net.sf.sahi.ant.Report;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TestRunner {
    private final String suiteName;

    private final String browser;

    private final String base;

    private final String sahiHost;

    private final String port;

    private final String threads;

    private String sessionId = null;

    private String browserOption;
    private CreateIssue createIssue;
    private List listReport;

    public static void main(String[] args) {
        try {
            if (args.length == 0) {
                System.out
                        .println("Usage: java TestRunner <suite_name> <start_url> <browser_executable> <log_dir> <sahi_host> <sahi_port> <number_of_threads>  optional<browser_option>");
                System.out
                        .println("Set log_dir to \"default\" if you want to log to the default log dir");
                System.out
                        .println("Set number_of_threads to a number less than 5 for Internet Explorer");
                System.out.println("Set number_of_threads to 1 for FireFox");
                System.out.println("Set browser_option to the profile dir to open FireFox with the given profile ");
                System.exit(-1);
            }
            String suiteName = args[0];
            String base = getBase(args);
            String browser = getBrowser(args);
            String logDir = args[3];
            if ("default".equalsIgnoreCase(logDir))
                logDir = "";
            String sahiHost = args[4];
            String port = args[5];
            String threads = args[6];
            String browserOption = "";
            if (args.length == 9)
                browserOption = args[8];
            TestRunner testRunner = new TestRunner(suiteName, browser, base, sahiHost, port, threads, browserOption);
            testRunner.addReport(new Report("html", logDir));                 
            String status = testRunner.execute();
            System.out.println("Status:" + status);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addReport(Report report) {
        if (listReport == null) {
            listReport = new ArrayList();
        }
        listReport.add(report);
    }

    public TestRunner(String suiteName, String browser, String base, String sahiHost, String port,
                      String threads, String browserOption) {
        this.suiteName = suiteName;
        this.browser = browser;
        this.base = base;
        this.sahiHost = sahiHost;
        this.port = port;
        this.threads = threads;
        this.browserOption = browserOption;
    }

    public TestRunner(String suiteName, String browser, String base,
                      String sahiHost, String port,
                      String threads, String browserOption, List listReporter, CreateIssue createIssue) {
        this(suiteName, browser, base, sahiHost, port, threads, browserOption);
        this.listReport = listReporter;
        this.createIssue = createIssue;
    }

    public String execute() throws IOException, InterruptedException {
        this.sessionId = "sahi_" + System.currentTimeMillis();
        StringBuffer urlStr = new StringBuffer(200).append("http://")
                .append(sahiHost).append(":").append(port).append(
                "/_s_/dyn/Suite_start?suite=")
                .append(encode(suiteName)).append("&base=")
                .append(encode(base))
                .append("&browser=").append(encode(browser))
                .append("&threads=").append(encode(threads))
                .append("&sahisid=").append(encode(this.sessionId));
        if (listReport == null || listReport.size() == 0) {
            addReport(new Report("html", null));
        }
        for (Iterator iterator = listReport.iterator(); iterator.hasNext();) {
            Report report = (Report) iterator.next();
            urlStr.append("&").append(encode(report.getType())).append("=").append(report.getLogDir()!=null? encode(report.getLogDir()) : "");
        }
        if (createIssue != null) {
            urlStr.append("&").append(encode(createIssue.getTool())).append("=");
            if (createIssue.getPropertiesFile()!=null) {
                urlStr.append(encode(createIssue.getPropertiesFile()));
            }
        }

        if (browserOption != null) {
            urlStr.append("&browserOption=").append(encode(browserOption));
        }

        URL url = new URL(urlStr.toString());
        InputStream in = url.openStream();
        in.close();
        String status;
        int retries = 0;
        while (true) {
            Thread.sleep(2000);
            status = getSuiteStatus(sessionId);
            if ("SUCCESS".equals(status) || "FAILURE".equals(status)) {
                break;
            } else if ("RETRY".equals(status)) {
                if (retries++ == 10) {
                    status = "FAILURE";
                    break;
                }
            }
        }
        return status;
    }

    private String getSuiteStatus(String sessionId) {
        String status;
        String urlStr;
        try {
            urlStr = "http://" + sahiHost + ":" + port
                    + "/_s_/dyn/Suite_status?s" + "&sahisid="
                    + encode(sessionId);
            URL url = new URL(urlStr);
            InputStream in = url.openStream();
            StringBuffer sb = new StringBuffer();
            int c = ' ';
            while ((c = in.read()) != -1) {
                sb.append((char) c);
            }
            status = sb.toString();
            in.close();
        } catch (Exception e) {
            System.out
                    .println("Exception while connecting to Sahi proxy to check status. Retrying ...");
            status = "RETRY";
        }
        return status;
    }

    private static String encode(String s) throws UnsupportedEncodingException {
        return URLEncoder.encode(s, "UTF8");
    }

    private static String getBase(String[] args) {
        String base = "";
        try {
            base = args[2];
        } catch (Exception e) {
        }
        return base;
    }

    private static String getBrowser(String[] args) {
        String browser = "";
        try {
            browser = args[1];
        } catch (Exception e) {
        }
        return browser;
    }
}
