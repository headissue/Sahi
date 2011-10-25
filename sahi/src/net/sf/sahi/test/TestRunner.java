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

package net.sf.sahi.test;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import net.sf.sahi.ant.CreateIssue;
import net.sf.sahi.ant.Report;
import net.sf.sahi.util.Utils;

/**
 * The TestRunner class invokes Sahi scripts from Java.
 * 
 * Sample usage
 * 
 * <p><blockquote><pre>
 * String browser = "C:\\Program Files\\Mozilla Firefox\\firefox.exe";
 * String base = "http://gramam/demo/";
 * String sahiHost = "localhost";
 * String port = "9999";
 * String threads = "1";
 * String browserOption = "-profile $userDir/browser/ff/profiles/sahi$threadNo -no-remote";
 * String browserProcessName = "firefox.exe";
 * String logDir = "D:/temp/logs/"; // relative paths will be resolved relative to userdata dir.

 * TestRunner testRunner = 
 * 	new TestRunner(suiteName, browser, base, sahiHost, 
 * 			port, threads, browserOption, browserProcessName);
 * testRunner.addReport(new Report("html", logDir));
 * String status = testRunner.execute();
 * </pre></blockquote>
 * 
 * @author narayan
 *
 */
public class TestRunner {
    private final String suiteName;

    private final String browser;

    private final String base;

    protected final String sahiHost;

    protected final String port;

    private final String threads;

    protected String sessionId = null;

    private String browserOption;
    private CreateIssue createIssue;
    private List<Report> listReport;

	private String browserProcessName;

	private String extraInfo;

	private String initJS;

	private String browserType;

	private boolean isSingleSession;

    public static void main(String[] args) {
        try {
            if (args.length == 0) {
                help();
            }
            if (mainCLIParams(args)) return;
            String suiteName = args[0];
            String base = getBase(args);
            String browser = getBrowser(args);
            String logDir = args[3];
            if ("default".equalsIgnoreCase(logDir)) {
                logDir = "";
            }
            String sahiHost = args[4];
            String port = args[5];
            String threads = args[6];
            String browserOption = "";
            String browserProcessName = args[7];
            if (args.length == 9) {
                browserOption = args[8];
            }
            TestRunner testRunner = new TestRunner(suiteName, browser, base, sahiHost, port, threads, browserOption, browserProcessName);
            testRunner.addReport(new Report("html", logDir));
            String status = testRunner.execute();
            System.out.println("Status:" + status);
        } catch (ConnectException ce) {
            System.err.println(ce.getMessage());
            System.err.println("Could not connect to Sahi Proxy.\nVerify that the Sahi Proxy is running on the specified host and port.");
            help();
        } catch (Exception e) {
            e.printStackTrace();
            help();
        }
    }
    
	public static boolean mainCLIParams(String[] args) throws IOException, InterruptedException {
		final HashMap<String, String> map = Utils.parseCLInput(args);
		String testName = getTestName(map);
		if (testName == null) return false;
		String host = map.get("host");
		String port2 = map.get("port");
		if (host == null) host = "localhost";
		if (port2 == null) port2 = "9999";
		final String browserType2 = map.get("browserType");
		final TestRunner testRunner;
		if (browserType2 != null){
			testRunner = new TestRunner(testName, browserType2, map.get("baseURL"), 
					host, port2, map.get("threads"));
		} else {
			testRunner = new TestRunner(testName, map.get("browser"), map.get("baseURL"), 
					host, port2, map.get("threads"), map.get("browserOption"), map.get("browserProcessName"));
		}
		if ("true".equals(map.get("htmlLog"))) {
			String logDir =  map.get("htmlLogDir");
			if (logDir == null || "default".equals(logDir)) logDir = "";
			testRunner.addReport(new Report("html", logDir));
		}
		if ("true".equals(map.get("tm6Log"))) {
			String logDir =  map.get("tm6LogDir");
			if (logDir == null || "default".equals(logDir)) logDir = "";
			testRunner.addReport(new Report("tm6", logDir));
		}
		if ("true".equals(map.get("junitLog"))) {
			String logDir =  map.get("junitLogDir");
			if (logDir == null || "default".equals(logDir)) logDir = "";
			testRunner.addReport(new Report("junit", logDir));
		}
		if (map.get("initJS") != null) {
			testRunner.setInitJS(map.get("initJS"));
		}
		if (map.get("extraInfo") != null) {
			testRunner.setExtraInfo(map.get("extraInfo"));
		}
		if (map.get("useSingleSession") != null) {
			testRunner.setIsSingleSession("true".equals(map.get("useSingleSession")));
		}
		System.out.println(testRunner.execute());
		return true;
	}

	public void setIsSingleSession(boolean isSingleSession) {
		this.isSingleSession = isSingleSession;
	}

	private static String getTestName(final HashMap<String, String> map) {
		String testName = map.get("test");
		if (testName == null) testName = map.get("suite");
		return testName;
	}
    private static String guessBrowserProcessName(String browser2) {
    	browser2 = browser2.replace('\\', '/');
    	int lastIx = browser2.lastIndexOf('/');
    	String executable = "";
    	if (lastIx >= 0){
			executable = browser2.substring(lastIx + 1);
    	}
		return executable;
	}

	protected static void help() {
        System.out.println("------------------------");
        System.out.println("New Usage 1: java -cp /path/to/ant-sahi.jar net.sf.sahi.test.TestRunner -test <test_or_suite_name> -browserType <browser_type> -baseURL <start_url> -threads <number_of_threads>");
        System.out.println("--- More options ---");
        System.out.println(" -test \t\t\tpath to test or suite");
        System.out.println(" -baseURL \t\tbaseURL for all tests");
        System.out.println(" -threads \t\tno. of browser instances to run in parallel");
        System.out.println(" -browserType \t\tbrowserType as specified in sahi/userdata/config/browser_types.xml");
        System.out.println(" -browser \t\tfull browser to browser. Ignored if browserType specified.");
        System.out.println(" -browserProcessName \tbrowser process name used to find the pid when using ps or tasklist commands. Ignored if browserType specified.");
        System.out.println(" -browserOption \tOptions to be passed to browser. Ignored if browserType specified.");
        System.out.println(" -junitLog \t\ttrue or false. Enable or disable junit logs");
        System.out.println(" -junitLogDir \t\tpath to junit log dir. If not specified, uses default location in userdata/logs");
        System.out.println(" -htmlLog \t\ttrue or false. Enable or disable html logs");
        System.out.println(" -htmlLogDir \t\tpath to html log dir. If not specified, uses default location in userdata/logs");
        System.out.println(" -initJS \t\tAny javascript which would be executed before every script");
        System.out.println(" -useSingSession \t\ttrue or false. Execute all scripts sequentially in a single browser session. Default is false.");
        System.out.println(" -extraInfo \t\tAny extra info that may be accessed using _extraInfo()");
        
        System.out.println("--- OR ---");
        System.out.println("Usage: java -cp /path/to/ant-sahi.jar net.sf.sahi.test.TestRunner <test_or_suite_name> <browser_executable> <start_url> <log_dir> <sahi_host> <sahi_port> <number_of_threads> <browser_executable> [<browser_option>]");
        System.out.println("Set log_dir to \"default\" to log to the default log dir");
        System.out.println("Set number_of_threads to a number which is compatible with your machine CPU and RAM.");
        System.out.println("Look at http://sahi.co.in/w/Running+multiple+tests+in+batch+mode for details on browser options for various browsers.");
        System.out.println("------------------------");
        System.exit(-1);
    }

    public void addReport(Report report) {
        if (listReport == null) {
            listReport = new ArrayList<Report>();
        }
        listReport.add(report);
    }

    /**
     * TestRunner constructor which takes in a browser, browserExecutable and browserOptions
     * This does not use sahi/userdata/config/browser_types.xml
     * 
     * @param suiteName
     * @param browser specifies the full path to browser executable
     * @param base
     * @param sahiHost
     * @param port
     * @param threads
     * @param browserOption specifies options that may be passed to the browser
     * @param browserProcessName specifies the name that appears when tasklist or ps is run. eg. firefox.exe
     */
    public TestRunner(String suiteName, String browser, String base, String sahiHost, String port,
            String threads, String browserOption, String browserProcessName) {
        this.suiteName = suiteName;
        this.browser = browser;
        this.base = base;
        this.sahiHost = sahiHost;
        this.port = port;
        this.threads = threads;
        this.browserOption = browserOption;
        this.browserProcessName = browserProcessName;
        if (browserProcessName == null){
        	this.browserProcessName = guessBrowserProcessName(browser);
        }
        System.out.println(this.toString());
    }

    /**
     * TestRunner constructor which takes in a browserType.
     * Browser Types are defined in sahi/userdata/config/browser_types.xml
     * Also assumes sahiHost=localhost and port=9999
     * 
     * @param suiteName
     * @param browserType
     * @param base
     * @param threads
     */
    public TestRunner(String suiteName, String browserType, String base, String threads) {
        this(suiteName, browserType, base, "localhost", "9999", threads);
    }
    /**
     * TestRunner constructor which takes in a browserType.
     * Browser Types are defined in sahi/userdata/config/browser_types.xml
     * 
     * 
     * @param suiteName
     * @param browserType
     * @param base
     * @param sahiHost
     * @param port
     * @param threads
     */
    public TestRunner(String suiteName, String browserType, String base, String sahiHost, String port, String threads) {
        this.suiteName = suiteName;
        this.browser = null;
        this.browserType = browserType;
        this.base = Utils.replaceLocalhostWithMachineName(base);
        this.sahiHost = sahiHost;
        this.port = port;
        this.threads = threads;
        System.out.println(this.toString());
    }
    
    public TestRunner(String suiteName, String browser, String base,
                      String sahiHost, String port,
                      String threads, String browserOption, String browserProcessName, List<Report> listReporter, CreateIssue createIssue) {
        this(suiteName, browser, base, sahiHost, port, threads, browserOption, browserProcessName);
        this.listReport = listReporter;
        this.createIssue = createIssue;
    }
   
    
	public TestRunner(String suiteName, String browserType, String base, String sahiHost, String port, String threads, List<Report> listReporter, CreateIssue createIssue) {
		this(suiteName, browserType, base, sahiHost, port, threads);
        this.listReport = listReporter;
        this.createIssue = createIssue;
	}

	public TestRunner(String suiteName, String browserType, String base, String sahiHost, String port, String threads, String initJS) {
		this(suiteName, browserType, base, sahiHost, port, threads);
		this.initJS = initJS;
	}

    public String execute() throws IOException, InterruptedException {
    	return execute("start");
    }
	
    public String execute(String command) throws IOException, InterruptedException {
        if (this.sessionId == null)
        	this.sessionId = Utils.generateId();
        String urlStr = buildURL(command);


        try {
            Thread thread = new Thread(new ShutDownHook(sahiHost, port, sessionId));
            Runtime.getRuntime().addShutdownHook(thread);
            System.out.println("Added shutdown hook.");
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(urlStr);
        URL url = new URL(urlStr);
        InputStream in = url.openStream();
        in.close();
        return getStatus();
    }

	protected String getStatus() throws InterruptedException {
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

	protected String buildURL(String command) throws UnsupportedEncodingException {
		StringBuffer urlStr = new StringBuffer(200).append("http://").append(sahiHost).append(":").append(port).append(
                "/_s_/dyn/Suite_" + command + "?suite=").append(encode(suiteName))
                .append("&base=").append(encode(base))
                .append("&threads=").append(encode(threads))
                .append("&sahisid=").append(encode(this.sessionId));

        
        if (browserType != null) {
        	urlStr.append("&browserType=").append(encode(browserType));
        } else {
        	urlStr.append("&browser=").append(encode(browser));
            urlStr.append("&browserProcessName=").append(encode(browserProcessName));
            if (browserOption != null) {
                urlStr.append("&browserOption=").append(encode(browserOption));
            }
        }
        
        if(this.extraInfo != null){
        	urlStr.append("&extraInfo=").append(encode(this.extraInfo));
        }
        if(this.initJS != null){
        	urlStr.append("&initJS=").append(encode(this.initJS));
        }
        System.out.println("this.isSingleSession == " + this.isSingleSession);
        if(this.isSingleSession){
        	urlStr.append("&useSingleSession=").append(this.isSingleSession);
        }        
        if (listReport == null || listReport.size() == 0) {
            addReport(new Report("html", null));
        }
        for (Iterator<Report> iterator = listReport.iterator(); iterator.hasNext();) {
            Report report = iterator.next();
            urlStr.append("&").append(encode(report.getType())).append("=").append(report.getLogDir() != null ? encode(report.getLogDir()) : "");
        }
        if (createIssue != null) {
            urlStr.append("&").append(encode(createIssue.getTool())).append("=");
            if (createIssue.getPropertiesFile() != null) {
                urlStr.append(encode(createIssue.getPropertiesFile()));
            }
        }
		return urlStr.toString();
	}

    private String getSuiteStatus(String sessionId) {
        String status;
        String urlStr;
        try {
            urlStr = "http://" + sahiHost + ":" + port + "/_s_/dyn/Suite_status?s" + "&sahisid=" + encode(sessionId);
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
        	e.printStackTrace();
            System.out.println("Exception while connecting to Sahi proxy to check status. Retrying ...");
            status = "RETRY";
        }
        return status;
    }

    protected static String encode(String s) throws UnsupportedEncodingException {
        return URLEncoder.encode(s, "UTF8");
    }

    protected static String getBase(String[] args) {
        String base = "";
        try {
            base = args[2];
        } catch (Exception e) {
        }
        return base;
    }

    protected static String getBrowser(String[] args) {
        String browser = "";
        try {
            browser = args[1];
        } catch (Exception e) {
        }
        return browser;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("\nsuiteName = " + suiteName);
        sb.append("\nbase = " + base);
        sb.append("\nsahiHost = " + sahiHost);
        sb.append("\nport = " + port);
        sb.append("\nthreads = " + threads);
        if (browserType != null){
        	sb.append("\nbrowserType = " + browserType);
        } else {
	        sb.append("\nbrowser = " + browser);
	        sb.append("\nbrowserOption = " + browserOption);
	        sb.append("\nbrowserProcessName = " + browserProcessName);
        }
        return sb.toString();

    }

	public void setInitJS(String initJS) {
		this.initJS = initJS;
	}
	
	public void setInitJS(HashMap<String, Object> variableHashMap) {
		StringBuilder sb = new StringBuilder();
		for (Iterator<String> iterator = variableHashMap.keySet().iterator(); iterator.hasNext();) {
			String key = iterator.next();
			sb.append("var " + key).append(" = ").append(getJSValue(variableHashMap.get(key))).append(";");
		}
		System.out.println(sb.toString());
		this.initJS = sb.toString();
	}
	
	private Object getJSValue(Object object) {
		if (object instanceof String) return "\"" + Utils.escapeDoubleQuotesAndBackSlashes((String)object) + "\"";
		return object.toString();
	}

	public void setExtraInfo(String extraInfo) {
		this.extraInfo = extraInfo;
	}
}