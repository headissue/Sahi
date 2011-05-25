/**
 * 
 */
package net.sf.sahi.test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import net.sf.sahi.util.Utils;

/**
 * @author narayan
 *
 */
public class SingleSessionTestRunner extends TestRunner {
	private String initJS;
	
	public SingleSessionTestRunner(String sessionName, String browserType, String base) {
		super(sessionName, browserType, Utils.replaceLocalhostWithMachineName(base), "1");
		setIsSingleSession(true);
		this.sessionId = Utils.generateId();
	}

	public void start() throws IOException, InterruptedException {
		String urlStr = buildURL("startSingleSession");
		System.out.println(urlStr);
		Utils.readURL(urlStr);
	}

	public String stop() throws UnsupportedEncodingException, InterruptedException {
        StringBuffer urlStr = new StringBuffer(200).append("http://").append(sahiHost).append(":").append(port).append(
                "/_s_/dyn/Suite_stopSingleSession?sahisid=").append(encode(this.sessionId));
        Utils.readURL(urlStr.toString());
        return getStatus();
	}

	public String executeSingleTest(String testName) throws UnsupportedEncodingException, InterruptedException {
        StringBuffer urlStr = new StringBuffer(200).append("http://").append(sahiHost).append(":").append(port)
        	.append("/_s_/dyn/Suite_executeTestInSingleSession?sahisid=").append(encode(this.sessionId))
        	.append("&testName=").append(encode(testName))
        	.append("&startURL=").append(encode(""))
        	.append("&initJS=").append((initJS == null) ? "" : encode(initJS));
            
        return new String(Utils.readURL(urlStr.toString()));
	}
	
	public void setInitJS(String initJS){
		this.initJS = initJS;
	}
	
	public String getInitJS(){
		return this.initJS;
	}

}
