package net.sf.sahi.util;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import net.sf.sahi.config.Configuration;
import net.sf.sahi.request.HttpRequest;
import net.sf.sahi.util.Utils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class BrowserTypesLoader {
	private static Map<String, BrowserType> browserTypes = new HashMap<String, BrowserType>();
	
	static {
    	loadBrowserTypes();
    }
	
    public static void loadBrowserTypes() {
		DocumentBuilder parser;
		try {
			parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document document = parser.parse(new File(getBrowserTypesFilePath()));
			final Element root = document.getDocumentElement();
			final NodeList childNodes = root.getElementsByTagName("browserType");
			for(int i = 0 ; i < childNodes.getLength();i++) {
				Element el = (Element)childNodes.item(i);
				BrowserType browserType = new BrowserType(el);
				addBrowserType(browserType);
			}
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
	}

	private static String getBrowserTypesFilePath() {
		final String path = Configuration.getBrowserTypesPath();
		if (new File(path).exists())
			return path;
		else {
			final String xmlPath = Utils.concatPaths(Configuration.getConfigPath(), "browser_types/" + Utils.getOSFamily() + ".xml");
			try {
				FileUtils.copyFile(xmlPath, path);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return path;
	}
    
	public static void addBrowserType(final BrowserType browserType){
		browserTypes.put(browserType.name(), browserType);
	}
	
	public BrowserType getBrowserType(final HttpRequest request) {
		String browserTypeParam = request.getParameter("browserType");
		return getBrowserType(browserTypeParam);
	}

	public static BrowserType getBrowserType(String name) {
		if (Utils.isBlankOrNull(name)) return null;
		return browserTypes.get(name);
	}
	
	public static Map<String, BrowserType> getBrowserTypes(){
		return browserTypes;
	}	
	
	public static Map<String, BrowserType> getAvailableBrowserTypes(boolean printMessage) {
		if (printMessage) {
			System.out.println("-----");
			System.out.println("Reading browser types from: " + getBrowserTypesFilePath());
		}
		Map<String, BrowserType> availableBrowserTypes = new HashMap<String, BrowserType>();
		for (Iterator<String> iterator = browserTypes.keySet().iterator(); iterator.hasNext();) {
			BrowserType browserType = browserTypes.get(iterator.next());
			final String expanded = Utils.expandSystemProperties(browserType.path());
			File browser = new File(expanded);
			if (!browser.exists()) {
				if (printMessage) {
					System.out.println(browserType.displayName()
							+ " was not found at " + expanded);
				}
			} else {
				availableBrowserTypes.put(browserType.name(), browserType);
			}
		}
		if (printMessage) System.out.println("-----");
		return availableBrowserTypes;
	}
}
