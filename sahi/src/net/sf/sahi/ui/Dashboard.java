package net.sf.sahi.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EtchedBorder;

import net.sf.sahi.Proxy;
import net.sf.sahi.config.Configuration;
import net.sf.sahi.util.BrowserType;
import net.sf.sahi.util.BrowserTypesLoader;
import net.sf.sahi.util.ProxySwitcher;
import net.sf.sahi.util.Utils;

/**
 * Dashboard exposes various Sahi components from a convenient place.
 */

public class Dashboard extends JFrame {
	// Useful link: http://download.oracle.com/javase/tutorial/uiswing/layout/box.html#filler
	
	
	private static final long serialVersionUID = 8348788972744726483L;

	private Proxy currentInstance;

	private JPanel masterPanel;

	private int browserTypesHeight;

	public Dashboard() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		setTitle("Sahi Dashboard");
        startProxy();
		buildUI();
		addOnExit();
		final Dimension dashboardSize = new Dimension(200, 225 + 35 + browserTypesHeight);
		setSize(dashboardSize);
		setPreferredSize(dashboardSize);
		setVisible(true);
	}

	private void startProxy() {
		final Proxy proxy = new Proxy(Configuration.getPort());
        currentInstance = proxy;
    	proxy.start(true);
	}

	private void buildUI() {
		masterPanel = new JPanel();
		masterPanel.setBackground(new Color(255, 255, 255));
		masterPanel.add(Box.createRigidArea(new Dimension(120, 5)));
		masterPanel.add(getBrowserButtons());
		masterPanel.add(Box.createRigidArea(new Dimension(120, 15)));
		masterPanel.add(getLogo());
		masterPanel.add(Box.createRigidArea(new Dimension(120, 15)));
		masterPanel.add(getLinksPanel1());
		masterPanel.add(Box.createRigidArea(new Dimension(120, 2)));
		masterPanel.add(getLinksPanel2());
//		masterPanel.add(Box.createRigidArea(new Dimension(120, 15)));
//		masterPanel.add(getSahiButtons());
		add(masterPanel);
		
	}

	private Component getLinksPanel1() {
		LinkButton link = new LinkButton("Configure", "http://localhost:9999/_s_/dyn/ConfigureUI");
		String scriptsPath = Configuration.getScriptRoots()[0];
		if (scriptsPath.endsWith("/") || scriptsPath.endsWith("\\")) 
			scriptsPath = scriptsPath.substring(0, scriptsPath.length() - 1);
		LinkButton link2;
		if (System.getProperty("os.name").startsWith("Windows")){
			link2 = new LinkButton("Scripts", "explorer /e,\"" + scriptsPath + "\"");
		} else {
			link2 = new LinkButton("Scripts", "\"" + scriptsPath + "\"");
		}
		
		JPanel buttonPane = new JPanel();
		buttonPane.setBackground(new Color(255, 255, 255));
		buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
		buttonPane.add(link);
		buttonPane.add(Box.createRigidArea(new Dimension(10, 0)));
		buttonPane.add(link2);
		return buttonPane;
	}

	private Component getLinksPanel2() {
		LinkButton link3 = new LinkButton("Logs", "http://localhost:9999/logs/");
		LinkButton link4 = new LinkButton("DB Logs", "http://localhost:9999//_s_/dyn/pro/DBReports");
		
		JPanel buttonPane = new JPanel();
		buttonPane.setBackground(new Color(255, 255, 255));
		buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
		buttonPane.add(link3);		
		buttonPane.add(Box.createRigidArea(new Dimension(10, 0)));
		buttonPane.add(link4);	
		
		if (System.getProperty("os.name").startsWith("Windows")){
			LinkButton link5 = new LinkButton("Bin", "cmd.exe /K cd " + Configuration.getAbsoluteUserPath("bin"));
			buttonPane.add(Box.createRigidArea(new Dimension(10, 0)));
			buttonPane.add(link5);		
		}
		
		return buttonPane;
	}

	private Component getLogo() {
		JLabel picLabel = new JLabel();
		picLabel.setIcon(new ImageIcon(getImageBytes("sahi_os_logo2.png"), ""));
		return picLabel;
	}

	private JPanel getBrowserButtons() {
		JPanel panel = new JPanel();
		final GridLayout layout = new GridLayout(0, 1, 0, 10);
		panel.setLayout(layout);
		final Border innerBorder = BorderFactory.createEmptyBorder(0, 10, 10, 10);
		final Border outerBorder = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
		final CompoundBorder compoundBorder = BorderFactory.createCompoundBorder(outerBorder, innerBorder);
		final Border titleBorder = BorderFactory.createTitledBorder(compoundBorder, "Launch Browser");
		panel.setBorder(titleBorder);
		add(panel);
		final Map<String, BrowserType> browserTypes = BrowserTypesLoader.getAvailableBrowserTypes(false);
		if (browserTypes.size() == 0) {
			final JLabel label = new JLabel("<html><b>No usable browsers<br>found in<br>browser_types.xml</b>.<br><br> Click on the<br><u>Configure</u> link below<br>to add/edit browsers.</html>");
			label.setSize(120, 100);
			panel.add(label);
			browserTypesHeight = 100;
		} else {
			for (Iterator<String> iterator = browserTypes.keySet().iterator(); iterator.hasNext();) {
				String name = iterator.next();
				BrowserType browserType = browserTypes.get(name);
				addButton(browserType, panel);
			}
			browserTypesHeight = browserTypes.size() * 50;
		}
		panel.setBackground(new Color(255, 255, 255));
		return panel;
	}

	private void setIcon(AbstractButton button, String iconFile) {
		if (iconFile == null) return;
		button.setIcon(new ImageIcon(getImageBytes(iconFile), ""));
		button.setHorizontalAlignment(SwingConstants.LEFT);
	}

	private byte[] getImageBytes(String iconFile) {
		try {
			final InputStream resourceAsStream = Dashboard.class.getResourceAsStream("/net/sf/sahi/resources/" + iconFile);
			return Utils.getBytes(resourceAsStream);
		} catch (IOException e) {
			e.printStackTrace();
			return new byte[0];
		}
	}

	private void addOnExit() {
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent we) {
				stopProxy();
				ProxySwitcher.revertSystemProxy(true);
				System.exit(0);
			}
		});
	}

	private void addButton(final BrowserType browserType, JPanel panel) {
		final JButton button = new JButton();
		button.setText(browserType.displayName());
		setIcon(button, browserType.icon());
		Dimension dimension = new Dimension(120, 40);
		button.setSize(dimension);
		button.setPreferredSize(dimension);
		button.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
			    new Thread() {
	
			        @Override
			        public void run() {
			            if (browserType.useSystemProxy()) toggleProxy(true);
			        	String browserExeCmd = browserType.path();
			        	String browserOption = browserType.options();
			        	if (browserOption != null) browserExeCmd = browserExeCmd + " " + browserOption;
			        	browserExeCmd = browserExeCmd.replace("$userDir", Configuration.getUserDataDir());
			        	browserExeCmd = browserExeCmd.replace("$threadNo", "0");
			        	browserExeCmd = browserExeCmd + " http://sahi.example.com/_s_/dyn/Driver_initialized";
			        	browserExeCmd = Utils.expandSystemProperties(browserExeCmd);
			        	System.out.println(browserExeCmd);
						execCommand(browserExeCmd);
						if (browserType.useSystemProxy()) ProxySwitcher.revertSystemProxy();
			        }

			    }.start();
			}
		});
		panel.add(button);
		pack();
	}

	private void execCommand(String cmd) {
        try {
            Utils.executeCommand(Utils.getCommandTokens(cmd));
        } catch (Exception ex) {
            Logger.getLogger(Dashboard.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
	

	private void stopProxy() {
		currentInstance.stop();
	}

	private void toggleProxy(boolean selected) {
		if (selected){
			ProxySwitcher.setSahiAsProxy();
		} else {
			ProxySwitcher.revertSystemProxy();
		}
	}

	public static void main(String args[]) {
    	if (args.length > 0){
    		Configuration.init(args[0], args[1]);
    	}else{
    		Configuration.init();
    	}
    	new Dashboard();
	}
}
