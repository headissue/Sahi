package net.sf.sahi.util;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class BrowserType {

	protected String name;
	protected String path;
	protected String options;
	protected String processName;
	protected int capacity;
	protected String displayName;
	protected String icon;
	protected boolean useSystemProxy;
	
	public BrowserType(String browserName, String browserPath,
			String browserOptions, String browserProcessToKill, int capacity, 
			String displayName, String icon, boolean useSystemProxy) {
				populateFields(browserName, browserPath, browserOptions,
						browserProcessToKill, capacity, displayName, icon,
						useSystemProxy);
	}

	private void populateFields(String browserName, String browserPath,
			String browserOptions, String browserProcessToKill, int capacity, 
			String displayName, String icon, boolean useSystemProxy) {
				this.name = browserName;
				this.path = browserPath;
				this.options = browserOptions;
				this.processName = browserProcessToKill;
				this.capacity = capacity;
				this.displayName = displayName;
				this.icon = icon;
				this.useSystemProxy = useSystemProxy;
	}
	
	public BrowserType(Element xmlEl) {
		String name = getTextFromXMLNode("name", xmlEl);
		String path = getTextFromXMLNode("path", xmlEl);
		String options = getTextFromXMLNode("options", xmlEl);
		String processName = getTextFromXMLNode("processName", xmlEl);
		
		int capacity1 = 1;
		try {
			capacity1 = Integer.parseInt(getTextFromXMLNode("capacity", xmlEl));
		}catch(Exception e){
		}
		
		if((path.contains("firefox") || processName.contains("firefox")) && !options.contains("\"")){
			options = options.replace("-profile ", "-profile \"").replace("$threadNo", "$threadNo\"");
		}
		 
		populateFields(name, path, options, processName, capacity1,
				getTextFromXMLNode("displayName", xmlEl), getTextFromXMLNode("icon", xmlEl), 
				"true".equals(getTextFromXMLNode("useSystemProxy", xmlEl)));	
	}

	
	/**
	 * Gets name of BrowserType. Can be any name. Examples: "firefox", "ff3", "ie8" etc.
	 * @return name of BrowserType
	 */
	public String name() {
		return name;
	}	
	/**
	 * Gets path of browser executable
	 * @return path
	 */
	public String path() {
		return path;
	}	
	/**
	 * Gets browser options string. 
	 * @return options
	 */
	public String options() {
		return options;
	}	
	/**
	 * Gets processName. This processName is used to look up PID in os specific commands like tasklist or ps -ef
	 * @return processName
	 */
	public String processName() {
		return processName;
	}
	/**
	 * Gets max number of browser instances supported on this instance of Sahi.
	 * @return capacity
	 */
	public int capacity() {
		return capacity;
	}
	/**
	 * Gets displayName. Used in UI.
	 * @return name as String
	 */
	public String displayName() {
		return Utils.isBlankOrNull(displayName) ? name : displayName; 
	}
	/**
	 * Returns icon's file name. icons are located in net.sf.sahi.resources package. Used in UI.
	 * @return icon filename as String
	 */
	public String icon() {
		return icon;
	}
	
	private static String getTextFromXMLNode(String name, Element xmlEl) {
		final NodeList byTagName = xmlEl.getElementsByTagName(name);
		if (byTagName.getLength() == 0)return null;
		try{
			return byTagName.item(0).getFirstChild().getNodeValue();
		} catch (Exception e){
			return "";
		}
	}

	public boolean useSystemProxy() {
		return this.useSystemProxy;
	}
}

