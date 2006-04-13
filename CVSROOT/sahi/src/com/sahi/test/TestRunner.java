package com.sahi.test;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

public class TestRunner {
	private final String suiteName;
	private final String browser;
	private final String base;
	private final String sahiHost;
	private final String port;
	private final String threads;

	public static void main(String[] args) {
		try {
			if (args.length == 0) {
				System.out.println("Usage: java TestRunner <test_script> <start_url> <browser executable path> <sahi_host> <sahi_port>");
				System.exit(-1);
			}
			String suiteName = args[0];
			String base = getBase(args);
			String browser = getBrowser(args);
			String sahiHost = args[3];
			String port = args[4];
			String threads = args[5];
			TestRunner testRunner = new TestRunner(suiteName, base, browser, sahiHost, port, threads);
			String status = testRunner.execute();
			System.out.println("Status:" + status);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public TestRunner(String suiteName, String browser, String base, String sahiHost, String port, String threads) {
		this.suiteName = suiteName;
		this.browser = browser;
		this.base = base;
		this.sahiHost = sahiHost;
		this.port = port;
		this.threads = threads;
	}

	public String execute() throws UnsupportedEncodingException, MalformedURLException, IOException, InterruptedException {
		String sessionId = "sahi_" + System.currentTimeMillis();
		String urlStr = "http://" + sahiHost + ":" + port + "/_s_/dyn/Suite_start?suite=" + encode(suiteName) + "&base=" + encode(base) + "&browser="
				+ encode(base) + "&browser=" + encode(browser) + "&threads=" + encode(threads) + "&sahisid=" + encode(sessionId);
		// System.out.println("urlStr=" + urlStr);
		URL url = new URL(urlStr);
		InputStream in = url.openStream();
		in.close();
		String status = "NONE";
		while (true) {
			Thread.sleep(1000);
			status = getSuiteStatus(sessionId);
			if (!"RUNNING".equals(status)) {
				break;
			}
		}
		return status;
	}

	private String getSuiteStatus(String sessionId) {
		String status = "NONE";
		String urlStr = "";
		try {
			urlStr = "http://" + sahiHost + ":" + port + "/_s_/dyn/Suite_status?s" + "&sahisid=" + encode(sessionId);
			URL url = new URL(urlStr);
			InputStream in = url.openStream();
			StringBuffer sb = new StringBuffer();
			int c = ' ';
			while ((c = (int) (in.read())) != -1) {
				sb.append((char) c);
			}
			status = sb.toString();
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
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
