package net.sf.sahi.ant;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import net.sf.sahi.test.TestRunner;

public class RunSahiTask extends Task {
	private String suite;
	private String scriptsDir;
	private String logDir = "";
	private String browser;
	private String baseURL;
	private String sahiHost;
	private String sahiPort;
	private String failureProperty;
	private String haltOnFailure;
	private String stop;
	private String threads = "1";

	public String getBaseURL() {
		return baseURL;
	}
	public void setBaseURL(String baseURL) {
		this.baseURL = baseURL;
	}
	public String getBrowser() {
		return browser;
	}
	public void setBrowser(String browser) {
		this.browser = browser;
	}
	public String getSuite() {
		return suite;
	}
	public void setSuite(String suite) {
		this.suite = suite;
	}
	public String getSahiPort() {
		return sahiPort;
	}
	public void setSahiPort(String port) {
		this.sahiPort = port;
	}
	public String getSahiHost() {
		return sahiHost;
	}
	public void setSahiHost(String sahiHost) {
		this.sahiHost = sahiHost;
	}

	public void execute() throws BuildException {
		if (stop != null) {
			stopServer();
			return;
		}
		startServer();
	}

	private void stopServer() {
			try {
				URL url = new URL("http://"+sahiHost+":"+sahiPort+"/_s_/dyn/stopserver");
				InputStream s = url.openStream();
				s.close();
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
			}
	}

	private void startServer() {
		boolean isFailure = false;
		String status = "FAILURE";
		try {
			TestRunner testRunner = new TestRunner(suite, browser, baseURL, logDir, sahiHost, sahiPort, threads);
			status = testRunner.execute();
			if (!"SUCCESS".equals(status)) {
				isFailure = true;
			}
		} catch (Exception e) {
			isFailure = true;
			e.printStackTrace();
		}
		if (isFailure) {
			System.out.println("STATUS:"+status);
			if (failureProperty != null) {
				getProject().setProperty(failureProperty, "true");
			}
			if ("true".equalsIgnoreCase(haltOnFailure)) {
				throw new BuildException(status);
			}
		}
	}
	public String getFailureProperty() {
		return failureProperty;
	}
	public void setFailureProperty(String failureProperty) {
		this.failureProperty = failureProperty;
	}
	public String getHaltOnFailure() {
		return haltOnFailure;
	}
	public void setHaltOnFailure(String haltOnFailure) {
		this.haltOnFailure = haltOnFailure;
	}
	public void setStop(String stop) {
		this.stop = stop;
	}
	public String getThreads() {
		return threads;
	}
	public void setThreads(String threads) {
		this.threads = threads;
	}

    public String getScriptsDir() {
        return scriptsDir;
    }

    public void setScriptsDir(String scriptsDir) {
        this.scriptsDir = scriptsDir;
    }

    public String getLogDir() {
        return logDir;
    }

    public void setLogDir(String logDir) {
        this.logDir = logDir;
    }
}
