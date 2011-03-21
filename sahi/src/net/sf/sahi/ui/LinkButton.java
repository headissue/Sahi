package net.sf.sahi.ui;

import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JLabel;
import javax.swing.JOptionPane;

import net.sf.sahi.util.Utils;

public class LinkButton extends JLabel {
	private static final long serialVersionUID = 8273875024682878518L;

	public LinkButton(final String text) {
		this(text, null);
	}
	public LinkButton(final String text, final String uri) {
		super();
		setText(text);
		if (uri != null) setToolTipText(uri.toString());
		addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				open(uri);
			}

			public void mouseEntered(MouseEvent e) {
				setCursor(new Cursor(Cursor.HAND_CURSOR));  
				setText(text, true);
			}

			public void mouseExited(MouseEvent e) {
				setCursor(new Cursor(Cursor.DEFAULT_CURSOR));  
				setText(text, false);
			}
		});
	}

	@Override
	public void setText(String text) {
		setText(text, false);
	}

	public void setText(String text, boolean highlight) {
		text = "<u>" + text + "</u>";
		super.setText("<html><span style=\"color: " + (highlight ? "#FF0000" : "#000099;") + "\">" + text + "</span></html>");
	}
	
	protected void open(String url) {
		String osName = System.getProperty("os.name");
		String cmd = null;
		if (osName.startsWith("Mac OS")){
			cmd = "open ";
		} else if (osName.startsWith("Windows")) {
			 cmd = "cmd.exe /C start ";
		} else {
			 cmd = "xdg-open ";
		} 
		cmd += url.toString();
		try {
			Utils.executeAndGetProcess(Utils.getCommandTokens(cmd));
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null,
					"Please navigate to http://localost:9999/_s_/ControllerUI to configure browsers for Sahi",
					"Cannot Launch Link", JOptionPane.WARNING_MESSAGE);
		}
	}
}